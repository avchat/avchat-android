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
	
	LinearLayout line_layout = new LinearLayout(activity);	
	HashMap<String, VideoStreamsView> view_map = new HashMap<String, VideoStreamsView>();	
	HashMap<String, VideoPlayer> player_map = new HashMap<String, VideoPlayer>();

	/**
	 * 构造函数
	 * @param activity
	 * @param pcManager
	 */
	public PCWrapper(Activity activity, PCManager pcManager) {
		this.activity = activity;
		this.pcManager = pcManager;
		
		factory = new PeerConnectionFactory();
		
		pcConstraints.optional.add(new MediaConstraints.KeyValuePair(
				"RtpDataChannels", "true"));

		pc = factory.createPeerConnection(iceServers, pcConstraints, pcObserver);	
	}

	/**
	 * PCObserver是peerconnection的事件回调类
	 * @author pony
	 *
	 */
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
	  
	/**
	 * SDPObserver是set{Local,Remote}Description()和create{Offer,Answer}()的事件回调类
	 * @author pony
	 *
	 */
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

	/**
	 * 增加本地流
	 * @param param
	 * @return
	 */
	public JSONObject addStream(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.addStream(localMediaStream, new MediaConstraints());
		return res;
	}
	
	/**
	 * 移除本地流
	 * @param param
	 * @return
	 */
	public JSONObject removeStream(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.removeStream(localMediaStream);
		
		return res;
	}

	/**
	 * 关闭peerconneciton
	 * @param param
	 * @return
	 */
	public JSONObject close(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.close();
		
		return res;
	}

	/**
	 * 创建Offer SDP
	 * @param param
	 * @return
	 */
	public JSONObject createOffer(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.createOffer(sdpObserver, videoConstraints);

		return res;
	}
	
	/**
	 * 创建Answer SDP
	 * @param param
	 * @return
	 */
	public JSONObject createAnswer(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.createAnswer(sdpObserver, videoConstraints);

		return res;
	}

	/**
	 * 创建数据通道
	 * @param param
	 * @return
	 */
	public JSONObject createDataChannel(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.createDataChannel(null, null);

		return res;
	}

	/**
	 * 设置本地描述信息
	 * @param param
	 * @return
	 */
	public JSONObject setLocalDescription(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.setLocalDescription(sdpObserver, null);

		return res;
	}

	/**
	 * 设置远端描述信息
	 * @param param
	 * @return
	 */
	public JSONObject setRemoteDescription(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.setRemoteDescription(sdpObserver, null);

		return res;
	}

	/**
	 * 更新ICE server列表
	 * @param param
	 * @return
	 */
	public JSONObject updateIce(JSONObject param) {
		JSONObject res = new JSONObject();
		pc.updateIce(iceServers, videoConstraints);

		return res;
	}
	
	/**
	 * 增加新的ICE候选点
	 * @param param
	 * @return
	 */
	public JSONObject addIceCandidate(JSONObject param) {
		JSONObject res = new JSONObject();
		IceCandidate ice = null;
		pc.addIceCandidate(ice);

		return res;
	}
	
	/**
	 * 获取统计信息
	 * @param param
	 * @return
	 */
	public JSONObject getStats(JSONObject param) {
		JSONObject res = new JSONObject();

		return res;
	}	
	
	/**
	 * 停止本地媒体流
	 * @param param
	 * @return
	 */
	public JSONObject mediastream_stop(JSONObject param) {
		JSONObject res = new JSONObject();

		return res;
	}
	
	/**
	 * 创建新的视频播放器
	 * @param param
	 * @return
	 */
	public JSONObject new_player(JSONObject param) {
		JSONObject res = new JSONObject();
		try {
			String play_id = param.getString("play_id");
			int view_id = param.getInt("view_id");
			
			VideoStreamsView vsv = view_map.get(view_id);
			VideoPlayer view_player = new VideoPlayer(vsv);
			player_map.put(play_id, view_player);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return res;
	}
	
	/**
	 * 删除视频播放器
	 * @param param
	 * @return
	 */
	public JSONObject delete_player(JSONObject param) {
		JSONObject res = new JSONObject();
		player_map.get(null);

		return res;
	}
	
	/**
	 * 创建新的UI视图
	 * @param param
	 * @return
	 */
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

	/**
	 * 删除一个UI视图
	 * @param param
	 * @return
	 */
	public JSONObject delete_view(JSONObject param) {
		JSONObject res = new JSONObject();
		String view_id = null;
		VideoStreamsView vsv = view_map.get(view_id);
		line_layout.removeView(vsv);

		return res;
	}
	
	/**
	 * 创建本地流
	 * @param param
	 * @return
	 */
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
	
	/**
	 * 私有方法，主要打开本地camera设备
	 * @return
	 */
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
