package com.ccpony.avchat.peerconnection;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class PCManager {
	private HashMap<Integer, PCWrapper> pc_map = new HashMap<Integer, PCWrapper>();
	private WebView js_runtime = null;
	private Activity activity = null;
	
	public PCManager(WebView webView, Activity activity) {
		this.js_runtime = webView;
		this.activity = activity;
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
		case 1: // new_pc
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
		case 14: // new_player
			res = pc_wrapper.new_player(param);
			break;
		case 15: // delete_player
			res = pc_wrapper.delete_player(param);
			break;
		case 16: // new_view 
			res = pc_wrapper.new_view(param);
			break;
		case 17: // delete_view
			res = pc_wrapper.delete_view(param);
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
}
