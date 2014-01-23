package com.ccpony.avchat.device;

import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ccpony.avchat.view.VideoStreamsView;

public class AVDeviceManager {
	private WebView js_runtime = null;
	private PeerConnectionFactory factory;
	private VideoSource videoSource;
	public MediaConstraints pcConstraints;
	public MediaConstraints videoConstraints;
	public VideoTrack videoTrack;
	
	class Optional {
		public Boolean video;
		public Boolean audio;
		public Boolean fake;
	}
	
	public AVDeviceManager(WebView js_runtime) {
		this.js_runtime = js_runtime;
	}
	
	private VideoCapturer getVideoCapturer() {
		String[] cameraFacing = { "front", "back" };
		int[] cameraIndex = { 0, 1 };
		int[] cameraOrientation = { 0, 90, 180, 270 };

		for (String facing : cameraFacing) {
			for (int index : cameraIndex) {
				for (int orientation : cameraOrientation) {
					String name = "Camera " + index + ", Facing " + facing
							+ ", Orientation " + orientation;
					VideoCapturer capturer = VideoCapturer.create(name);
					if (capturer != null) {
						//logAndToast("Using camera: " + name);
						return capturer;
					}
				}
			}
		}
		throw new RuntimeException("Failed to open capturer");
	}
	
	public void get_user_media() {
		//logAndToast("Creating local video source...");
		MediaStream lMS = factory.createLocalMediaStream("ARDAMS");
		VideoCapturer capturer = getVideoCapturer();
		videoSource = factory.createVideoSource(capturer, videoConstraints);
		videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
		
		lMS.addTrack(videoTrack);
		lMS.addTrack(factory.createAudioTrack("ARDAMSa0"));
		
	}
	
	@JavascriptInterface
	public void get_user_media(Optional config) {	
		// get media logic
		
		// result return
		// if ok;
		js_runtime.loadUrl("javascript:avDeviceManager.callback()");
		// else
		js_runtime.loadUrl("javascript:avDeviceManager.error()");
	}	
}
