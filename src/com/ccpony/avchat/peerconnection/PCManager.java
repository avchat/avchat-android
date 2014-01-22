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
	public void new_pc(int pc_id) {
		PCWrapper pc_wrapper = new PCWrapper(null, null);
		pc_map.put(pc_id, pc_wrapper);
	}
	
	@JavascriptInterface
	public void delete_pc(int pc_id) {
		pc_map.remove(pc_id);
	}
	
	// peer connection functions
	@JavascriptInterface 
	public void addStream(int pc_id, String stream) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);		
		try {
			JSONObject json = new JSONObject(stream);
		} catch (JSONException e) {			
			e.printStackTrace();
		}
	}

	@JavascriptInterface 
	public void removeStream(int pc_id, String stream) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	@JavascriptInterface 
	public void close(int pc_id) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	@JavascriptInterface 
	public void createOffer(int pc_id, String constraints) {	
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}
	
	@JavascriptInterface 
	public void createAnswer(int pc_id, String constraints) {	
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	@JavascriptInterface 
	public void setLocalDescription(int pc_id, String sd) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	@JavascriptInterface 
	public void setRemoteDescription(int pc_id, String sd) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	@JavascriptInterface 
	public void createDataChannel(int pc_id, String param) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	@JavascriptInterface 
	public void updateIce(int pc_id, String param) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	};

	@JavascriptInterface 
	public void addIceCandidate(int pc_id, String ice) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	@JavascriptInterface 
	public void getStats(int pc_id) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
	}

	// cb_xxx
	@JavascriptInterface 
	public void cb_createOffer(int pc_id, JSONObject offer) {
		js_runtime.loadUrl("javascript:pcMangerJS.onSignalingChange()");	
	}

	@JavascriptInterface 
	public void cb_createAnswer(int pc_id, JSONObject answer) {
		js_runtime.loadUrl("javascript:pcMangerJS.onSignalingChange()");	
	}

	// onxxx
	@JavascriptInterface 
	public void onSignalingChange(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onSignalingChange()");
	}

	@JavascriptInterface 
	public void onIceConnectionChange(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onIceConnectionChange()");
	}

	@JavascriptInterface 
	public void onIceGatheringChange(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onIceGatheringChange()");
	}

	@JavascriptInterface 
	public void onIceCandidate(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onIceCandidate()");
	}

	@JavascriptInterface 
	public void onError(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onError()");
	}

	@JavascriptInterface 
	public void onAddStream(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onAddStream()");
	}

	@JavascriptInterface 
	public void onRemoveStream(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onRemoveStream()");
	}

	@JavascriptInterface 
	public void onDataChannel(int pc_id, JSONObject param) {
		js_runtime.loadUrl("javascript:pcMangerJS.onDataChannel()");
	}
		
	// stream functions
	@JavascriptInterface 
	public void mediastream_stop(int pc_id) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
		//pc_wrapper.stop();
	}	
}
