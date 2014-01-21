package com.ccpony.avchat.peerconnection;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.JsonReader;
import android.webkit.JavascriptInterface;

public class PCManager {
	HashMap<String, PCWrapper> pc_map = new HashMap<String, PCWrapper>();
	
	@JavascriptInterface
	public void add_pc(String pc_id) {
		PCWrapper pc_wrapper = new PCWrapper(null, null);
		pc_map.put(pc_id, pc_wrapper);
	}
	
	@JavascriptInterface
	public void remove_pc(String pc_id) {
		pc_map.remove(pc_id);
	}
	
//	@JavascriptInterface
//	public void add_stream(String pc_id, JSONObject obj) {
//		PCWrapper pc_wrapper = pc_map.get(pc_id);
//		String param = obj.toString();
//		try {
//			JSONObject res = new JSONObject(param);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
