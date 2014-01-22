package com.ccpony.avchat.peerconnection;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.JsonReader;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

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
		}
		return res.toString();
	}
	
	public void cb_method(String method, int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS."+ method + "," + pc_id + "," + param.toString() + ")");
	}
		
	// stream functions
	@JavascriptInterface 
	public void mediastream_stop(int pc_id) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
		//pc_wrapper.stop();
	}	
}
