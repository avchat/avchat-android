package com.ccpony.avchat.peerconnection;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.app.Activity;
import android.graphics.Point;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.ccpony.avchat.player.VideoPlayer;
import com.ccpony.avchat.view.VideoStreamsView;

public class PCManager {
	private WebView js_runtime = null;
	private Activity activity = null;
	private HashMap<String, PCWrapper> pc_map = new HashMap<String, PCWrapper>();
	private HashMap<String, VideoPlayer> player_map = new HashMap<String, VideoPlayer>();
	private HashMap<String, VideoStreamsView> view_map = new HashMap<String, VideoStreamsView>();	
	private LinearLayout line_layout = null;	
	
	private MediaStream localMediaStream = null;
	private VideoTrack videoTrack = null;
	private VideoSource videoSource = null;
	private VideoCapturer videoCapturer = null;
	private PeerConnectionFactory factory = null;
	private MediaConstraints videoConstraints = null;	
	
	
	public PCManager(WebView js_runtime, Activity activity) {
		this.js_runtime = js_runtime;
		this.activity = activity;
		
		this.line_layout = new LinearLayout(activity);
		this.factory = new PeerConnectionFactory();
	}
	
	public PeerConnectionFactory get_pc_factory() {
		return factory;
	}

	@JavascriptInterface
	public void call_method(String method, String pc_id, String param_str) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
		JSONObject param = null;
		try {
			param = new JSONObject(param_str);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(method == "pc_new") {
			pc_wrapper = new PCWrapper(this, pc_id);
			pc_map.put(pc_id, pc_wrapper);
		} else if(method == "addStream") {
			pc_wrapper.addStream(localMediaStream);	 
		} else if(method == "removeStream") {
			pc_wrapper.removeStream(localMediaStream);
		} else if(method == "close") {
			pc_wrapper.close();
		} else if(method == "createAnswer") {
			pc_wrapper.createAnswer(param);
		} else if(method == "createOffer") {
			pc_wrapper.createOffer(param);
		} else if(method == "createDataChannel") {
			pc_wrapper.createDataChannel(param);
		} else if(method == "setLocalDescription") {
			pc_wrapper.setLocalDescription(param);
		} else if(method == "setRemoteDescription") {
			pc_wrapper.setRemoteDescription(param);
		} else if(method == "updateIce") {
			pc_wrapper.updateIce(param);
		} else if(method == "addIceCandidate") {
			pc_wrapper.addIceCandidate(param);
		} else if(method == "getStats") {
			/*pc_wrapper.getStats(param);*/
		} else if(method == "mediastream_stop") {
			this.mediastream_stop(param);
		} else if(method == "player_new") {
			this.player_new(pc_id, param);
		} else if(method == "player_delete") {
			this.player_delete(pc_id, param);
		} else if(method == "view_new") {
			this.view_new(param);
		} else if(method == "view_delete") {
			this.view_delete(param);
		} else if(method == "get_user_media") {
			this.get_user_media(param);
		}
	}
	
	public void cb_method(String method, String pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcManagerJS.cb_method("+ method + "," + pc_id + "," + param.toString() + ")");
	}
	
	/**
	 * 创建新的视频播放器
	 * @param param
	 * @return
	 */
	public void player_new(String pc_id, JSONObject param) {
		try {
			String play_id = param.getString("play_id");
			int view_id = param.getInt("view_id");
			
			VideoStreamsView vsv = view_map.get(view_id);
			VideoPlayer view_player = new VideoPlayer(vsv);
			player_map.put(play_id, view_player);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除视频播放器
	 * @param param
	 * @return
	 */
	public void player_delete(String pc_id, JSONObject param) {
		try {
			String play_id = param.getString("play_id");
			
			player_map.remove(play_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建新的UI视图
	 * @param param
	 * @return
	 */
	public void view_new(JSONObject param) {
		try {
			String view_id = param.getString("view_id");
			int width = param.getInt("width");
			int height = param.getInt("height");
			
			Point displaySize = new Point(width, height);		
			VideoStreamsView vsv = new VideoStreamsView(activity, displaySize);
			view_map.put(view_id, vsv);
			line_layout.addView(vsv);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 删除一个UI视图
	 * @param param
	 * @return
	 */
	public void view_delete(JSONObject param) {
		try {
			String view_id = param.getString("view_id");
			
			VideoStreamsView vsv = view_map.remove(view_id);
			line_layout.removeView(vsv);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 停止本地媒体流
	 * @param param
	 * @return
	 */
	public void mediastream_stop(JSONObject param) {
		localMediaStream.dispose();
	}
	
	/**
	 * 创建本地流
	 * @param param
	 * @return
	 */
	public void get_user_media(JSONObject param) {		
		if(localMediaStream == null) {
			localMediaStream = factory.createLocalMediaStream("ARDAMS");
			videoCapturer = getVideoCapturer();
			videoSource = factory.createVideoSource(videoCapturer, videoConstraints);
			videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
			
			localMediaStream.addTrack(videoTrack);
			localMediaStream.addTrack(factory.createAudioTrack("ARDAMSa0"));
		}
		
		JSONObject cb_param = new JSONObject();
		try {
			cb_param.put("pc_id", "0");
			cb_param.put("stream_type", "local");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.cb_method("cb_getUserMedia", "0", cb_param);
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
