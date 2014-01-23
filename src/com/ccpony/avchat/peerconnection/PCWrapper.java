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

	public PCWrapper(Activity activity) {
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

	public JSONObject addStream(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.addStream(lms, new MediaConstraints());
		return res;
	}
	
	public JSONObject removeStream(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.removeStream(lms);
		
		return res;
	}

	public JSONObject close(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.close();
		
		return res;
	}

	public JSONObject createOffer(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.createOffer(sdpObserver, videoConstraints);

		return res;
	}
	
	public JSONObject createAnswer(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.createAnswer(sdpObserver, videoConstraints);

		return res;
	}

	public JSONObject createDataChannel(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.createDataChannel(null, null);

		return res;
	}

	public JSONObject setLocalDescription(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.setLocalDescription(sdpObserver, null);

		return res;
	}

	public JSONObject setRemoteDescription(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.setRemoteDescription(sdpObserver, null);

		return res;
	}

	public JSONObject updateIce(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.updateIce(iceServers, videoConstraints);

		return res;
	}
	
	public JSONObject addIceCandidate(JSONObject param) {
		JSONObject res = new JSONObject();
		IceCandidate ice = null;
		pc.addIceCandidate(ice);

		return res;
	}
	
	public JSONObject getStats(JSONObject param) {
		JSONObject res = new JSONObject();

		return res;
	}	
	
	// stream functions
	@JavascriptInterface 
	public JSONObject mediastream_stop(JSONObject param) {
		JSONObject res = new JSONObject();

		return res;
	}
	
	// player functions 
	HashMap<String, VideoPlayer> player_map = new HashMap<String, VideoPlayer>();
	
	@JavascriptInterface
	public JSONObject new_player(JSONObject param) {
		JSONObject res = new JSONObject();
		VideoPlayer view_player = new VideoPlayer(null);
		player_map.put(null, view_player);

		return res;
	}
	
	@JavascriptInterface
	public JSONObject delete_player(JSONObject param) {
		JSONObject res = new JSONObject();
		player_map.get(null);

		return res;
	}
	
	// view functions
	HashMap<String, VideoStreamsView> view_map = new HashMap<String, VideoStreamsView>();
	LinearLayout line_layout = new LinearLayout(activity);	
	
	@JavascriptInterface
	public JSONObject new_view(JSONObject param) {
		JSONObject res = new JSONObject();
		String view_id = null;
		int width = 0;
		int height = 0;
		Point displaySize = new Point(width, height);		
		VideoStreamsView vsv = new VideoStreamsView(activity, displaySize);
		view_map.put(view_id, vsv);
		line_layout.addView(vsv);

		return res;
	}

	@JavascriptInterface
	public JSONObject delete_view(JSONObject param) {
		JSONObject res = new JSONObject();
		String view_id = null;
		VideoStreamsView vsv = view_map.get(view_id);
		line_layout.removeView(vsv);

		return res;
	}
	
	// av device functions
	public VideoTrack videoTrack;
	
	@JavascriptInterface
	public JSONObject get_user_media(JSONObject param) {		
		JSONObject res = new JSONObject();
		if(lms != null) {
			logAndToast("Creating local video source...");
			lms = factory.createLocalMediaStream("ARDAMS");
			VideoCapturer capturer = getVideoCapturer();
			videoSource = factory.createVideoSource(capturer, videoConstraints);
			videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
			
			lms.addTrack(videoTrack);
			lms.addTrack(factory.createAudioTrack("ARDAMSa0"));
		}
		pcManager.cb_method("cb_getUserMedia", 0, param);

		return res;
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
