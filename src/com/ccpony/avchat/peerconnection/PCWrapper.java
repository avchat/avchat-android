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

import android.app.Activity;

public class PCWrapper {
	private PCManager pcManager = null;
	private PeerConnectionFactory factory = null;
	
	private String pc_id = "";		
	private PeerConnection pc = null;	
	private MediaConstraints pcConstraints = null;
	private MediaConstraints videoConstraints = null;	
	private List<PeerConnection.IceServer> iceServers = null;
	private final PCObserver pcObserver = new PCObserver();
	private final SDPObserver sdpObserver = new SDPObserver();
	
	/**
	 * 构造函数
	 * @param activity
	 * @param pcManager
	 */
	public PCWrapper(PCManager pcManager, String pc_id) {
		this.pcManager = pcManager;
		this.pc_id = pc_id;
		
		factory = pcManager.get_pc_factory();
		
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
	public JSONObject addStream(JSONObject param, MediaStream localMediaStream) {
		JSONObject res = new JSONObject();
		pc.addStream(localMediaStream, new MediaConstraints());
		return res;
	}
	
	/**
	 * 移除本地流
	 * @param param
	 * @return
	 */
	public JSONObject removeStream(JSONObject param, MediaStream localMediaStream) {
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
}
