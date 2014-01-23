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
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import com.ccpony.avchat.player.VideoPlayer;
import com.ccpony.avchat.view.VideoStreamsView;

public class PCWrapper {
	private PCManager pcManager = null;
	private Activity activity = null;
	
	private int pc_id = 0;	
	private PeerConnectionFactory factory = null;
	private PeerConnection pc = null;	
	private MediaConstraints pcConstraints = null;
	private MediaConstraints videoConstraints = null;	
	private List<PeerConnection.IceServer> iceServers = null;
	private final PCObserver pcObserver = new PCObserver();
	private final SDPObserver sdpObserver = new SDPObserver();
	
	private MediaStream localMediaStream = null;
	private VideoTrack videoTrack = null;
	private VideoSource videoSource = null;
	private VideoCapturer capturer = null;
	private VideoStreamsView vsv = null;
	

	public PCWrapper(Activity activity) {
		this.activity = activity;

		Point displaySize = new Point();
		activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
		vsv = new VideoStreamsView(activity, displaySize);
		activity.setContentView(vsv);
	}

	private class PCObserver implements PeerConnection.Observer {
		@Override
		public void onIceCandidate(final IceCandidate candidate) {
			// make param
			JSONObject param = new JSONObject();
			try {
				param.put("type", "candidate");
				param.put("label", candidate.sdpMLineIndex);
				param.put("id", candidate.sdpMid);
				param.put("candidate", candidate.sdp);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// call cb_method
			pcManager.cb_method("onIceCandidate", pc_id, param);
		}

		@Override
		public void onError() {
			JSONObject param = new JSONObject();
			pcManager.cb_method("onError", pc_id, param);
		}

		@Override
		public void onSignalingChange(PeerConnection.SignalingState newState) {
			JSONObject param = new JSONObject();
			pcManager.cb_method("onSignalingChange", pc_id, param);
		}

		@Override
		public void onIceConnectionChange(
				PeerConnection.IceConnectionState newState) {
			JSONObject param = new JSONObject();
			pcManager.cb_method("onIceConnectionChange", pc_id, param);
		}

		@Override
		public void onIceGatheringChange(
				PeerConnection.IceGatheringState newState) {
			JSONObject param = new JSONObject();
			pcManager.cb_method("onIceGatheringChange", pc_id, param);
		}

		@Override
		public void onAddStream(final MediaStream stream) {
			JSONObject param = new JSONObject();
			pcManager.cb_method("onAddStream", pc_id, param);
		}

		@Override
		public void onRemoveStream(final MediaStream stream) {
			stream.videoTracks.get(0).dispose();
			JSONObject param = new JSONObject();
			pcManager.cb_method("onRemoveStream", pc_id, param);
		}

		@Override
		public void onDataChannel(final DataChannel dc) {
			JSONObject param = new JSONObject();
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
			JSONObject cb_param = new JSONObject();			
			try {
				cb_param.put("type", origSdp.type.canonicalForm());
				cb_param.put("sdp", origSdp.description);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if (origSdp.type.canonicalForm() == "offer") {
				pcManager.cb_method("cb_createOffer", pc_id, cb_param);
			} else if (origSdp.type.canonicalForm() == "answer") {
				pcManager.cb_method("cb_createAnswer", pc_id, cb_param);
			}
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
	}

	public void create_pc() {
		factory = new PeerConnectionFactory();
		
		pcConstraints.optional.add(new MediaConstraints.KeyValuePair(
				"RtpDataChannels", "true"));

		pc = factory.createPeerConnection(iceServers, pcConstraints, pcObserver);				
	}

	public JSONObject addStream(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.addStream(localMediaStream, new MediaConstraints());
		return res;
	}
	
	public JSONObject removeStream(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.removeStream(localMediaStream);
		
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
	@JavascriptInterface
	public JSONObject get_user_media(JSONObject param) {		
		JSONObject res = new JSONObject();
		if(localMediaStream == null) {
			localMediaStream = factory.createLocalMediaStream("ARDAMS");
			capturer = getVideoCapturer();
			videoSource = factory.createVideoSource(capturer, videoConstraints);
			videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
			
			localMediaStream.addTrack(videoTrack);
			localMediaStream.addTrack(factory.createAudioTrack("ARDAMSa0"));
		}
		
		JSONObject cb_param = new JSONObject();
		pcManager.cb_method("cb_getUserMedia", 0, cb_param);

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
						return capturer;
					}
				}
			}
		}
		
		return null;
	}	
}
