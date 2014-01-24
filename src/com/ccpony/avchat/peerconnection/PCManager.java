package com.ccpony.avchat.peerconnection;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoCapturer;

import com.ccpony.avchat.player.VideoPlayer;
import com.ccpony.avchat.view.VideoStreamsView;

import android.app.Activity;
import android.graphics.Point;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class PCManager {
	private WebView js_runtime = null;
	private Activity activity = null;
	private HashMap<Integer, PCWrapper> pc_map = new HashMap<Integer, PCWrapper>();
	LinearLayout line_layout = null;	
	HashMap<String, VideoStreamsView> view_map = new HashMap<String, VideoStreamsView>();	
	HashMap<String, VideoPlayer> player_map = new HashMap<String, VideoPlayer>();
	
	
	public PCManager(WebView webView, Activity activity) {
		this.js_runtime = webView;
		this.activity = activity;
		
		line_layout = new LinearLayout(activity);
	}

	@JavascriptInterface
	public String call_method(int method, int pc_id, String param_str) {
		JSONObject res = null;
		PCWrapper pc_wrapper = pc_map.get(pc_id);
		JSONObject param = null;
		try {
			param = new JSONObject(param_str);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		switch(method) {
		case 1: // pc_new
			pc_wrapper = new PCWrapper(activity, this);
			pc_map.put(pc_id, pc_wrapper);
			break;
		case 2: // addStream
			res = pc_wrapper.addStream(param);	 
			break;
		case 3: // removeStream
			res = pc_wrapper.removeStream(param);
			break;
		case 4: // close
			res = pc_wrapper.close(param);
			break;
		case 5: // createAnswer
			res = pc_wrapper.createAnswer(param);
			break;
		case 6: // createOffer
			res = pc_wrapper.createOffer(param);
			break;
		case 7: // createDataChannel
			res = pc_wrapper.createDataChannel(param);
			break;
		case 8: // setLocalDescription
			res = pc_wrapper.setLocalDescription(param);
			break;
		case 9: // setRemoteDescription
			res = pc_wrapper.setRemoteDescription(param);
			break;
		case 10: // updateIce
			res = pc_wrapper.updateIce(param);
			break;
		case 11: // addIceCandidate
			res = pc_wrapper.addIceCandidate(param);
			break;
		case 12: // getStats
			res = pc_wrapper.getStats(param);
			break;
		case 13: // mediastream_stop
			res = pc_wrapper.mediastream_stop(param);
			break;
		case 14: // player_new
			res = pc_wrapper.player_new(param);
			break;
		case 15: // player_delete
			res = pc_wrapper.player_delete(param);
			break;
		case 16: // view_new 
			res = pc_wrapper.view_new(param);
			break;
		case 17: // view_delete
			res = pc_wrapper.view_delete(param);
			break;
		case 18: // get_user_media
			res = pc_wrapper.get_user_media(param);
			break;
		}
		return res.toString();
	}
	
	public void cb_method(String method, int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcManagerJS."+ method + "," + pc_id + "," + param.toString() + ")");
	}
	
	/**
	 * 创建新的视频播放器
	 * @param param
	 * @return
	 */
	public JSONObject player_new(JSONObject param) {
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
	public JSONObject player_delete(JSONObject param) {
		JSONObject res = new JSONObject();
		player_map.get(null);

		return res;
	}
	
	/**
	 * 创建新的UI视图
	 * @param param
	 * @return
	 */
	public JSONObject view_new(JSONObject param) {
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
	public JSONObject view_delete(JSONObject param) {
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
			videoCapturer = getVideoCapturer();
			videoSource = factory.createVideoSource(videoCapturer, videoConstraints);
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
