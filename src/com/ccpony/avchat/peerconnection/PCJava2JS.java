package com.ccpony.avchat.peerconnection;

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
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoTrack;

import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.ccpony.avchat.view.VideoStreamsView;

public class PCJava2JS {
	private WebView js_runtime = null;
	private Activity activity = null;
	private static final String TAG = "PCJava2JS";
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

	public PCJava2JS(WebView js_runtime, Activity activity) {
		this.js_runtime = js_runtime;
		this.activity = activity;

		Point displaySize = new Point();
		activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
		vsv = new VideoStreamsView(activity, displaySize);
		activity.setContentView(vsv);
	}

	public void onicecandidate(String param1) {
		js_runtime.loadUrl("javascript:peerconnection.onicecandidate(" + param1
				+ ")");
	}

	public void cb_createOffer() {
		js_runtime.loadUrl("javascript:peerconnection.cb_createOffer()");
	}

	public void cb_createAnswer() {
		js_runtime.loadUrl("javascript:peerconnection.cb_createAnswer()");
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

	// Poor-man's assert(): die with |msg| unless |condition| is true.
	private static void abortUnless(boolean condition, String msg) {
		if (!condition) {
			throw new RuntimeException(msg);
		}
	}

	// Send |json| to the underlying AppEngine Channel.
	private void sendMessage(JSONObject json) {

	}

	// Implementation detail: bridge the VideoRenderer.Callbacks interface to
	// the
	// VideoStreamsView implementation.
	private class VideoCallbacks implements VideoRenderer.Callbacks {
		private final VideoStreamsView view;
		private final VideoStreamsView.Endpoint stream;

		public VideoCallbacks(VideoStreamsView view,
				VideoStreamsView.Endpoint stream) {
			this.view = view;
			this.stream = stream;
		}

		@Override
		public void setSize(final int width, final int height) {
			view.queueEvent(new Runnable() {
				public void run() {
					view.setSize(stream, width, height);
				}
			});
		}

		@Override
		public void renderFrame(I420Frame frame) {
			view.queueFrame(stream, frame);
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
			sendMessage(json);
		}

		@Override
		public void onError() {
			throw new RuntimeException("PeerConnection error!");
		}

		@Override
		public void onSignalingChange(PeerConnection.SignalingState newState) {
		}

		@Override
		public void onIceConnectionChange(
				PeerConnection.IceConnectionState newState) {
			js_runtime
					.loadUrl("javascript:peerconnection.oniceconnectionstatechange()");
		}

		@Override
		public void onIceGatheringChange(
				PeerConnection.IceGatheringState newState) {
		}

		@Override
		public void onAddStream(final MediaStream stream) {
			js_runtime.loadUrl("javascript:peerconnection.onaddstream()");
			abortUnless(
					stream.audioTracks.size() <= 1
							&& stream.videoTracks.size() <= 1,
					"Weird-looking stream: " + stream);
			if (stream.videoTracks.size() == 1) {
				stream.videoTracks.get(0).addRenderer(
						new VideoRenderer(new VideoCallbacks(vsv,
								VideoStreamsView.Endpoint.REMOTE)));
			}
		}

		@Override
		public void onRemoveStream(final MediaStream stream) {
			js_runtime.loadUrl("javascript:peerconnection.onremovestream()");
			stream.videoTracks.get(0).dispose();
		}

		@Override
		public void onDataChannel(final DataChannel dc) {
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

		
		pc = factory
				.createPeerConnection(iceServers, pcConstraints, pcObserver);

		final PeerConnection finalPC = pc;
		final Runnable repeatedStatsLogger = new Runnable() {
			public void run() {
				synchronized (quit[0]) {
					if (quit[0]) {
						return;
					}
					final Runnable runnableThis = this;
					boolean success = finalPC.getStats(new StatsObserver() {
						public void onComplete(StatsReport[] reports) {
							for (StatsReport report : reports) {
								Log.d(TAG, "Stats: " + report.toString());
							}
							vsv.postDelayed(runnableThis, 10000);
						}
					}, null);
					if (!success) {
						throw new RuntimeException(
								"getStats() return false!");
					}
				}
			}
		};
		vsv.postDelayed(repeatedStatsLogger, 10000);		
	}
	
	public void getusermedia() {
		logAndToast("Creating local video source...");
		MediaStream lMS = factory.createLocalMediaStream("ARDAMS");
		VideoCapturer capturer = getVideoCapturer();
		videoSource = factory.createVideoSource(capturer, videoConstraints);
		VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0",
				videoSource);
		videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(vsv,
				VideoStreamsView.Endpoint.LOCAL)));
		lMS.addTrack(videoTrack);
		lMS.addTrack(factory.createAudioTrack("ARDAMSa0"));
		pc.addStream(lMS, new MediaConstraints());
	}
	
	public void create_player() {
		
	}

	@JavascriptInterface
	public void addIceCandidate() {

	}

	@JavascriptInterface
	public void addStream() {
	}

	@JavascriptInterface
	public void close() {
	}

	@JavascriptInterface
	public void createAnswer() {
	}

	@JavascriptInterface
	public void createDataChannel() {
	}

	@JavascriptInterface
	public void createOffer() {
	}

	@JavascriptInterface
	public void getLocalStreams() {
	}

	@JavascriptInterface
	public void getRemoteStreams() {
	}

	@JavascriptInterface
	public void getStats() {
	}

	@JavascriptInterface
	public void getStreamById() {
	}

	@JavascriptInterface
	public void removeStream() {
	}

	@JavascriptInterface
	public void setLocalDescription() {
	}

	@JavascriptInterface
	public void setRemoteDescription() {
	}

	@JavascriptInterface
	public void updateIce() {
	}
}
