package com.ccpony.avchat.peerconnection;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.content.Context;
import android.graphics.Point;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.ccpony.avchat.MainActivity;
import com.ccpony.avchat.player.VideoPlayer;
import com.ccpony.avchat.view.VideoStreamsView;

public class PCManager {
	public WebView js_runtime = null;
	private MainActivity room_context = null;	
	private PeerConnectionFactory pc_factory = null;
	
	private HashMap<String, PCWrapper> map_pc = new HashMap<String, PCWrapper>();
	private HashMap<String, VideoPlayer> map_player = new HashMap<String, VideoPlayer>();
	private HashMap<String, VideoStreamsView> map_view = new HashMap<String, VideoStreamsView>();	
	private LinearLayout layout_line = null;	
	
	private MediaStream media_stream_local = null;
		
	/**
	 * PCManager构造函数
	 * @param js_runtime
	 * @param room_context
	 */
	public PCManager(WebView js_runtime, MainActivity room_context, LinearLayout line) {
		this.js_runtime = js_runtime;
		this.room_context = room_context;
		
		//this.layout_line = new LinearLayout(room_context);
		this.layout_line = line;
		this.pc_factory = new PeerConnectionFactory();
	}
	
	/**
	 * 获取PC工厂
	 * @return
	 */
	public PeerConnectionFactory get_pc_factory() {
		return pc_factory;
	}
	
	/**
	 * 获取布局
	 * @return
	 */
	public LinearLayout get_line_layout() {
		return layout_line;
	}
	
	/**
	 * javascript代理调用此方法，然后进行调用分发
	 * @param method
	 * @param pc_id
	 * @param param_str
	 */
	@JavascriptInterface
	public void call_method(String method, String pc_id, String param_str) {
		// 获取PCWrapper对象
		PCWrapper pc_wrapper = map_pc.get(pc_id);
		
		// 将JSON字符串解析成JSON对象
		JSONObject param = null;
		try {
			param = new JSONObject(param_str);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// 对调用进行分发处理
		if(method.equals("pc_new")) {
			// 创建新的pc封装对象
			pc_wrapper = new PCWrapper(pc_id, this, param);
			
			// 放入map_pc容器
			map_pc.put(pc_id, pc_wrapper);
			
		} else if(method.equals("addStream")) {
			// 添加本地流
			pc_wrapper.addStream(media_stream_local);
			
		} else if(method.equals("removeStream")) {
			// 移除本地流
			pc_wrapper.removeStream(media_stream_local);
			
		} else if(method.equals("close")) {
			// 关闭pc连接
			pc_wrapper.close();
			
		} else if(method.equals("createAnswer")) {
			// 创建answer包
			pc_wrapper.createAnswer(param);
			
		} else if(method.equals("createOffer")) {
			// 创建offer包
			pc_wrapper.createOffer(param);
			
		} else if(method.equals("createDataChannel")) {
			// 数据功能暂不实现
			pc_wrapper.createDataChannel(param);
			
		} else if(method.equals("setLocalDescription")) {
			// 设置本地描述
			pc_wrapper.setLocalDescription(param);
			
		} else if(method.equals("setRemoteDescription")) {
			// 设置远程描述
			pc_wrapper.setRemoteDescription(param);
			
		} else if(method.equals("updateIce")) {
			// 更新ICE server
			pc_wrapper.updateIce(param);
			
		} else if(method.equals("addIceCandidate")) {
			// 增加ICE候选点
			pc_wrapper.addIceCandidate(param);
			
		} else if(method.equals("getStats")) {
			/*pc_wrapper.getStats(param);*/
			
		} else if(method.equals("mediastream_stop")) {
			// 停止本地流
			this.mediastream_stop(param);
			
		} else if(method.equals("player_new")) {
			// 新建并绑定播放器到某视图
			this.player_new(pc_id, param);
			
		} else if(method.equals("player_delete")) {
			// 删除并解除绑定播放器
			this.player_delete(param);
			
		} else if(method.equals("view_new")) {
			// 新建视图
			this.view_new(param);
			
		} else if(method.equals("view_delete")) {
			// 删除视图
			this.view_delete(param);
			
		} else if(method.equals("get_user_media")) {
			// 获取本地媒体流
			this.get_user_media(param);
			
		}
	}
	
	
	
	/**
	 * 创建新的视频播放器
	 * @param param
	 * @return
	 */
	public void player_new(String pc_id, JSONObject param) {
		try {
			// 解析JSON字符串到JSON对象
			String player_id = param.getString("play_id");
			String view_id = param.getString("view_id");
			String stream_type = param.getString("stream_type");
			
			// 根据view_id获得视图
			VideoStreamsView vsv = map_view.get(view_id);
			
			// 根据媒体流类型获得视频轨
			/*VideoTrack video_track = null;
			
			if(stream_type == "local") {
				MediaStream media_stream_local = map_pc.get(pc_id).media_stream_local;
				video_track = media_stream_local.videoTracks.get(0);
			} else {
				MediaStream media_stream_remote = map_pc.get(pc_id).media_stream_remote;
				video_track = media_stream_remote.videoTracks.get(0);
			}*/
			
			// 绑定视图到视频轨
			VideoTrack video_track = null;
			video_track = media_stream_local.videoTracks.get(0);
			VideoPlayer player = new VideoPlayer(player_id, vsv, video_track);
			
			// 将刚新播放器放入播放器map容器
			map_player.put(player_id, player);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除视频播放器
	 * @param param
	 * @return
	 */
	public void player_delete(JSONObject param) {
		try {
			// 获取播放器ID
			String player_id = param.getString("player_id");
			
			// 删除播放器
			map_player.remove(player_id);			
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
			// 通过JSON对象获取视图相关参数
			String view_id = param.getString("view_id");
			int width = param.getInt("width");
			int height = param.getInt("height");
			
			Point displaySize = new Point(width, height);
			
			// 新建视图
			VideoStreamsView vsv = new VideoStreamsView(view_id, room_context, displaySize);
			
			// 将视图放入map_view容器
			map_view.put(view_id, vsv);
			
			// 将视图加入布局
			//layout_line.addView(vsv);
			this.room_context.add_view(layout_line, vsv);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
			// 通过JSON对象获取视图ID
			String view_id = param.getString("view_id");
			
			// 删除视图
			VideoStreamsView vsv = map_view.remove(view_id);
			
			// 从布局中移除视图
			layout_line.removeView(vsv);
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
		// 停止本地流
		media_stream_local.dispose();
		
		// 将本地流设置为空
		media_stream_local = null;
	}
	
	/**
	 * 创建本地流
	 * @param param
	 * @return
	 * @throws JSONException 
	 */
	public void get_user_media(JSONObject param) {	
		// 根据JSON对象，创建本地流参数
		
		MediaConstraints videoConstraints = new MediaConstraints();
		try {
			JSONObject json = param.optJSONObject("video");
	        JSONObject mandatoryJSON = json.optJSONObject("mandatory");
//	        if (mandatoryJSON != null) {
//	          JSONArray mandatoryKeys = mandatoryJSON.names();
//	          if (mandatoryKeys != null) {
//	            for (int i = 0; i < mandatoryKeys.length(); ++i) {
//	              String key = mandatoryKeys.getString(i);
//	              String value = mandatoryJSON.getString(key);
//	              videoConstraints.mandatory.add(
//	                  new MediaConstraints.KeyValuePair(key, value));
//	            }
//	          }
//	        }
	        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("width", "640"));
	        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("height", "480"));
	        
	        JSONArray optionalJSON = json.optJSONArray("optional");
	        if (optionalJSON != null) {
	          for (int i = 0; i < optionalJSON.length(); ++i) {
	            JSONObject keyValueDict = optionalJSON.getJSONObject(i);
	            String key = keyValueDict.names().getString(0);
	            String value = keyValueDict.getString(key);
	            videoConstraints.optional.add(
	                new MediaConstraints.KeyValuePair(key, value));
	          }
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// 获取本地流
		if(media_stream_local == null) {
			media_stream_local = pc_factory.createLocalMediaStream("ARDAMS");
			VideoCapturer video_capturer = null;
			video_capturer = get_video_capturer();
			VideoSource video_source = pc_factory.createVideoSource(video_capturer, videoConstraints);
			VideoTrack video_track = pc_factory.createVideoTrack("ARDAMSv0", video_source);
			
			media_stream_local.addTrack(video_track);
			media_stream_local.addTrack(pc_factory.createAudioTrack("ARDAMSa0"));
		}
		
		// 产生本地流JSON对象
		JSONObject cb_param = new JSONObject();
		try {
			cb_param.put("pc_id", "0");
			cb_param.put("stream_type", "local");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// 产生getusermedia回调
		this.cb_method("cb_getUserMedia", "0", cb_param);
	}
	
	/**
     * 被java pc调用，从而进行js端的回调
     * @param method
     * @param pc_id
     * @param param
     */
	public void cb_method(String method, String pc_id, String param) {
    	this.room_context.cb_method(method, pc_id, param);
            //js_runtime.loadUrl("javascript:pcManagerJS.cb_method("+ method + "," + pc_id + "," + param.toString() + ")");
    }
	
    public void cb_method(String method, String pc_id, JSONObject param) {
    	this.room_context.cb_method(method, pc_id, param);
            //js_runtime.loadUrl("javascript:pcManagerJS.cb_method("+ method + "," + pc_id + "," + param.toString() + ")");
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
}
