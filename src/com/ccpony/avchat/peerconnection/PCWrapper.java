package com.ccpony.avchat.peerconnection;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
import org.webrtc.SessionDescription.Type;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.graphics.Point;

import com.ccpony.avchat.player.VideoPlayer;
import com.ccpony.avchat.view.VideoStreamsView;

public class PCWrapper {
	private PCManager pcManager = null;
	private PeerConnectionFactory factory = null;
	
	private String pc_id = "";		
	private PeerConnection pc = null;	
	private MediaConstraints pcConstraints = new MediaConstraints();
	private List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
	
	private final PCObserver pcObserver = new PCObserver();
	private final SDPObserver sdpObserver = new SDPObserver();
	
	public MediaStream media_stream_local = null;
	public MediaStream media_stream_remote = null;
	
	/**
	 * 构造函数
	 * @param activity
	 * @param pcManager
	 */
	public PCWrapper(String pc_id, PCManager pcManager, JSONObject param) {
		this.pc_id = pc_id;
		this.pcManager = pcManager;		
		
		factory = new PeerConnectionFactory();
		
		//pcConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "false"));
		pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
		
		PeerConnection.IceServer ice = new PeerConnection.IceServer("stun:stun.l.google.com:19302");
		this.iceServers.add(ice);
		
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
			try {
				param.put("state", newState.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			pcManager.cb_method("onSignalingChange", pc_id, param);
		}

		@Override
		public void onIceConnectionChange(
				PeerConnection.IceConnectionState newState) {
			JSONObject param = new JSONObject();
			try {
				param.put("state", newState.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			pcManager.cb_method("onIceConnectionChange", pc_id, param);
		}

		@Override
		public void onIceGatheringChange(
				PeerConnection.IceGatheringState newState) {
			JSONObject param = new JSONObject();
			try {
				param.put("state", newState.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			pcManager.cb_method("onIceGatheringChange", pc_id, param);
		}

		@Override
		public void onAddStream(final MediaStream stream) {
			media_stream_remote = stream;
			
			JSONObject param = new JSONObject();
			try {
				param.put("pc_id", pc_id);
				param.put("stream_type", "remote");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			pcManager.cb_method("onAddStream", pc_id, param);
		}

		@Override
		public void onRemoveStream(final MediaStream stream) {
			media_stream_remote = null;
			
			stream.videoTracks.get(0).dispose();
			JSONObject param = new JSONObject();
			try {
				param.put("pc_id", pc_id);
				param.put("stream_type", "remote");
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
			
			if (origSdp.type.canonicalForm().equals("offer")) {
				pcManager.cb_method("cb_createOffer", pc_id, cb_param);
			} else if (origSdp.type.canonicalForm().equals("answer")) {
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
	 * 私有方法，主要打开本地camera设备
	 * @return
	 */
	private VideoCapturer get_video_capturer() {
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
	
	/**
	 * 增加本地流
	 * @param param
	 * @return
	 */
	public void addStream() {
		// media
		MediaConstraints Constraints = new MediaConstraints();
		this.media_stream_local = factory.createLocalMediaStream("ARDAMS");
		VideoCapturer capturer = get_video_capturer();
		VideoSource videoSource = factory.createVideoSource(capturer,Constraints);
		VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0",
				videoSource);
		
		media_stream_local.addTrack(videoTrack);
		media_stream_local.addTrack(factory.createAudioTrack("ARDAMSa0"));
//		
//		Point displaySize = new Point(320, 240);
//		
//		// 新建视图
//		VideoStreamsView vsv = new VideoStreamsView("view_local_0", pcManager.room_context, displaySize);
//		pcManager.room_context.add_view(pcManager.get_line_layout(), vsv);
//		VideoPlayer player = new VideoPlayer("local_stream_0", vsv, videoTrack);
		
		pc.addStream(media_stream_local, new MediaConstraints());
	}
	
	/**
	 * 移除本地流
	 * @param param
	 * @return
	 */
	public void removeStream() {		
		pc.removeStream(media_stream_local);
		
		this.media_stream_local = null;
	}

	/**
	 * 关闭peerconneciton
	 * @param param
	 * @return
	 */
	public void close() {
		pc.close();
	}

	/**
	 * 创建Offer SDP
	 * @param param
	 * @return
	 */
	public void createOffer(JSONObject param) {
		MediaConstraints videoConstraints = new MediaConstraints();
		try {
			JSONObject mandatoryJSON = param.optJSONObject("mandatory");
			
	        if (mandatoryJSON != null) {
	          JSONArray mandatoryKeys = mandatoryJSON.names();
	          if (mandatoryKeys != null) {
	            for (int i = 0; i < mandatoryKeys.length(); ++i) {
	              String key = mandatoryKeys.getString(i);
	              String value = mandatoryJSON.getString(key);
	              videoConstraints.mandatory.add(
	                  new MediaConstraints.KeyValuePair(key, value));
	            }
	          }
	        }
			
			pc.createOffer(sdpObserver, videoConstraints);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建Answer SDP
	 * @param param
	 * @return
	 */
	public void createAnswer(JSONObject param) {
		MediaConstraints videoConstraints = new MediaConstraints();
		try {
			JSONObject mandatoryJSON = param.optJSONObject("mandatory");
			
	        if (mandatoryJSON != null) {
	          JSONArray mandatoryKeys = mandatoryJSON.names();
	          if (mandatoryKeys != null) {
	            for (int i = 0; i < mandatoryKeys.length(); ++i) {
	              String key = mandatoryKeys.getString(i);
	              String value = mandatoryJSON.getString(key);
	              videoConstraints.mandatory.add(
	                  new MediaConstraints.KeyValuePair(key, value));
	            }
	          }
	        }
			
			pc.createAnswer(sdpObserver, videoConstraints);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建数据通道(有调用，但暂不实现数据部分)
	 * @param param
	 * @return
	 */
	public void createDataChannel(JSONObject param) {
		pc.createDataChannel(null, null);
	}

	/**
	 * 设置本地描述信息
	 * @param param
	 * @return
	 */
	public void setLocalDescription(JSONObject param) {
		SessionDescription sd = null;
		Type type = SessionDescription.Type.OFFER;
		try {
			String type_str = param.getString("type");
			if(type_str.equals("offer")) {
				type = SessionDescription.Type.OFFER;
			} else if(type_str.equals("answer")) {
				type = SessionDescription.Type.ANSWER;
			}
			sd = new SessionDescription(type, param.getString("sdp"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		pc.setLocalDescription(sdpObserver, sd);
	}

	/**
	 * 设置远端描述信息
	 * @param param
	 * @return
	 */
	public void setRemoteDescription(JSONObject param) {
		SessionDescription sd = null;
		Type type = SessionDescription.Type.OFFER;
		try {
			String type_str = param.getString("type");
			if(type_str.equals("offer")) {
				type = SessionDescription.Type.OFFER;
			} else if(type_str.equals("answer")) {
				type = SessionDescription.Type.ANSWER;
			}
			sd = new SessionDescription(type, param.getString("sdp"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		pc.setRemoteDescription(sdpObserver, sd);
	}

	/**
	 * 更新ICE server列表(没调用，暂不实现)
	 * @param param
	 * @return
	 */
	public void updateIce(JSONObject param) {
		//pc.updateIce(iceServers, videoConstraints);
	}
	
	/**
	 * 增加新的ICE候选点(没调用，暂不实现)
	 * @param param
	 * @return
	 */
	public void addIceCandidate(JSONObject param) {
		IceCandidate ice = null;
		pc.addIceCandidate(ice);
	}
}
