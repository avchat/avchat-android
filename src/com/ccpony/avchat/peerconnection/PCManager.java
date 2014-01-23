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

import com.ccpony.avchat.player.VideoPlayer;
import com.ccpony.avchat.view.VideoStreamsView;

import android.app.Activity;
import android.graphics.Point;
import android.util.JsonReader;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class PCManager {
	private HashMap<Integer, PCWrapper> pc_map = new HashMap<Integer, PCWrapper>();
	private WebView js_runtime = null;
	
	public PCManager(WebView webView) {
		this.js_runtime = webView;
	}

	@JavascriptInterface
	public String call_method(int method, int pc_id, String param_str) {
		JSONObject res = null;
		PCWrapper pc_wrapper = pc_map.get(pc_id);	
		JSONObject json = null;
		try {
			json = new JSONObject(param_str);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch(method) {
		case 1: // new_pc
			pc_wrapper = new PCWrapper(null, null);
			pc_map.put(pc_id, pc_wrapper);
			break;
		case 2: // addStream
			//res = pc_wrapper.addStream(json);	 
			break;
		case 3: // removeStream
			//res = pc_wrapper.removeStream(json);
			break;
		case 4: // close
			//res = pc_wrapper.close();
			break;
		case 5: // createAnswer
			//res = pc_wrapper.createAnswer(json);
			break;
		case 6: // createOffer
			//res = pc_wrapper.createOffer(json);
			break;
		case 7: // createDataChannel
			//res = pc_wrapper.createDataChannel(json);
			break;
		case 8: // setLocalDescription
			//res = pc_wrapper.setLocalDescription(json);
			break;
		case 9: // setRemoteDescription
			//res = pc_wrapper.setRemoteDescription(json);
			break;
		case 10: // updateIce
			//res = pc_wrapper.updateIce(json);
			break;
		case 11: // addIceCandidate
			//res = pc_wrapper.addIceCandidate(json);
			break;
		case 12: // getStats
			//res = pc_wrapper.getStats(json);
			break;
		case 13: // mediastream_stop
			//res = pc_wrapper.mediastream_stop(json);
			break;
		case 14: // new_player
			//res = pc_wrapper.new_player(json);
			break;
		case 15: // delete_player
			//res = pc_wrapper.delete_player(json);
			break;
		case 16: // new_view 
			//res = pc_wrapper.new_view(json);
			break;
		case 17: // delete_view
			//res = pc_wrapper.delete_view(json);
			break;
		case 18: // get_user_media
			//res = pc_wrapper.get_user_media(json);
			break;
		}
		return res.toString();
	}
	
	public void cb_method(String method, int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS."+ method + "," + pc_id + "," + param.toString() + ")");
	}
}
