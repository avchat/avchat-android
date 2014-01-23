package com.ccpony.avchat.peerconnection;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ccpony.avchat.player.VideoPlayer;
import com.ccpony.avchat.view.VideoStreamsView;

public class PCWrapper {
	private WebView js_runtime = null;
	private Activity activity = null;
	private static final String TAG = "PCWrapper";
	private Toast logToast;
	private VideoStreamsView vsv;
	private PeerConnectionFactory factory;
	private PeerConnection pc;
	private final PCObserver pcObserver = new PCObserver();
	private final SDPObserver sdpObserver = new SDPObserver();
	private final Boolean[] quit = new Boolean[] { false };
	private VideoSource videoSource;
	public MediaConstraints pcConstraints;
	public MediaConstraints videoConstraints;
	List<PeerConnection.IceServer> iceServers = null;
	
	PCManager pcManager = null;
	int pc_id = 0;
	JSONObject param = null;
	MediaStream lms;

	public PCWrapper(WebView js_runtime, Activity activity) {
		this.js_runtime = js_runtime;
		this.activity = activity;

		Point displaySize = new Point();
		activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
		vsv = new VideoStreamsView(activity, displaySize);
		activity.setContentView(vsv);
	}

	// Log |msg| and Toast about it.
	private void logAndToast(String msg) {
		Log.d(TAG, msg);
		if (logToast != null) {
			logToast.cancel();
		}
		logToast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
		logToast.show();
	}

	// Put a |key|->|value| mapping in |json|.
	private static void jsonPut(JSONObject json, String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}	

	// Poor-man's assert(): die with |msg| unless |condition| is true.
	private static void abortUnless(boolean condition, String msg) {
		if (!condition) {
			throw new RuntimeException(msg);
		}
	}

	
	// Implementation detail: observe ICE & stream changes and react
	// accordingly.
	private class PCObserver implements PeerConnection.Observer {
		@Override
		public void onIceCandidate(final IceCandidate candidate) {
			JSONObject json = new JSONObject();
			jsonPut(json, "type", "candidate");
			jsonPut(json, "label", candidate.sdpMLineIndex);
			jsonPut(json, "id", candidate.sdpMid);
			jsonPut(json, "candidate", candidate.sdp);
			
			pcManager.cb_method("onIceCandidate", pc_id, param);
		}

		@Override
		public void onError() {
			pcManager.cb_method("onError", pc_id, param);
		}

		@Override
		public void onSignalingChange(PeerConnection.SignalingState newState) {
			pcManager.cb_method("onSignalingChange", pc_id, param);
		}

		@Override
		public void onIceConnectionChange(
				PeerConnection.IceConnectionState newState) {
			pcManager.cb_method("onIceConnectionChange", pc_id, param);
		}

		@Override
		public void onIceGatheringChange(
				PeerConnection.IceGatheringState newState) {
			pcManager.cb_method("onIceGatheringChange", pc_id, param);
		}

		@Override
		public void onAddStream(final MediaStream stream) {

			abortUnless(
					stream.audioTracks.size() <= 1
							&& stream.videoTracks.size() <= 1,
					"Weird-looking stream: " + stream);
			if (stream.videoTracks.size() == 1) {
//				stream.videoTracks.get(0).addRenderer(
//						new VideoRenderer(new VideoCallbacks(vsv,
//								VideoStreamsView.Endpoint.REMOTE)));
			}
			
			pcManager.cb_method("onAddStream", pc_id, param);
		}

		@Override
		public void onRemoveStream(final MediaStream stream) {

			stream.videoTracks.get(0).dispose();
			
			pcManager.cb_method("onRemoveStream", pc_id, param);
		}

		@Override
		public void onDataChannel(final DataChannel dc) {

			pcManager.cb_method("onDataChannel", pc_id, param);
			
			throw new RuntimeException(
					"AppRTC doesn't use data channels, but got: " + dc.label()
							+ " anyway!");
		}
	}

	// Implementation detail: handle offer creation/signaling and answer
	// setting,
	// as well as adding remote ICE candidates once the answer SDP is set.
	private class SDPObserver implements SdpObserver {
		@Override
		public void onCreateSuccess(final SessionDescription origSdp) {
			// pcManager.cb_method("cb_createOffer", pc_id, param);
			// pcManager.cb_method("cb_createAnswer", pc_id, param);
		}

		@Override
		public void onSetSuccess() {
		}

		@Override
		public void onCreateFailure(final String error) {
			throw new RuntimeException("createSDP error: " + error);
		}

		@Override
		public void onSetFailure(final String error) {
			throw new RuntimeException("setSDP error: " + error);
		}

		private void drainRemoteCandidates() {
		}
	}

	public void create_pc() {
		factory = new PeerConnectionFactory();

		MediaConstraints pcConstraints = null;
		pcConstraints.optional.add(new MediaConstraints.KeyValuePair(
				"RtpDataChannels", "true"));

		pc = factory.createPeerConnection(iceServers, pcConstraints, pcObserver);				
	}

	public void addStream() {
		pc.addStream(lms, new MediaConstraints());
	}
	
	public void removeStream() {
		pc.removeStream(lms);
	}

	public void close() {
		pc.close();
	}

	public void createOffer() {
		pc.createOffer(sdpObserver, videoConstraints);
	}
	
	public void createAnswer() {
		pc.createAnswer(sdpObserver, videoConstraints);
	}

	public void createDataChannel() {
		pc.createDataChannel(null, null);
	}

	public void setLocalDescription() {
		pc.setLocalDescription(sdpObserver, null);
	}

	public void setRemoteDescription() {
		pc.setRemoteDescription(sdpObserver, null);
	}

	public void updateIce() {
		pc.updateIce(iceServers, videoConstraints);
	}
	
	public void addIceCandidate(IceCandidate ice) {
		pc.addIceCandidate(ice);
	}
	
	// stream functions
	@JavascriptInterface 
	public void mediastream_stop(int pc_id) {
		
	}
	
	// player functions 
	HashMap<String, VideoPlayer> player_map = new HashMap<String, VideoPlayer>();
	
	@JavascriptInterface
	public void new_player() {
		VideoPlayer view_player = new VideoPlayer(null);
		player_map.put(null, view_player);
	}
	
	@JavascriptInterface
	public void delete_player() {		
		player_map.get(null);
	}
	
	// view functions
	HashMap<String, VideoStreamsView> view_map = new HashMap<String, VideoStreamsView>();
	LinearLayout line_layout = new LinearLayout(activity);	
	
	@JavascriptInterface
	public void new_view(String view_id, int width, int height) {
		Point displaySize = new Point(width, height);		
		VideoStreamsView vsv = new VideoStreamsView(activity, displaySize);
		view_map.put(view_id, vsv);
		line_layout.addView(vsv);
	}

	@JavascriptInterface
	public void delete_view(String view_id) {
		VideoStreamsView vsv = view_map.get(view_id);
		line_layout.removeView(vsv);
	}
	
	// av device functions
	public VideoTrack videoTrack;
	
	@JavascriptInterface
	public void get_user_media() {
		logAndToast("Creating local video source...");
		MediaStream lMS = factory.createLocalMediaStream("ARDAMS");
		VideoCapturer capturer = getVideoCapturer();
		videoSource = factory.createVideoSource(capturer, videoConstraints);
		videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
		
		lMS.addTrack(videoTrack);
		lMS.addTrack(factory.createAudioTrack("ARDAMSa0"));
		
		// result return
		// if ok;
		js_runtime.loadUrl("javascript:avDeviceManager.callback()");
		// else
		js_runtime.loadUrl("javascript:avDeviceManager.error()");
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
						logAndToast("Using camera: " + name);
						return capturer;
					}
				}
			}
		}
		throw new RuntimeException("Failed to open capturer");
	}	
}
