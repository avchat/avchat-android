package com.ccpony.avchat.peerconnection;

import java.util.HashMap;

import android.webkit.JavascriptInterface;

public class PCManager {
	HashMap<String, PCWrapper> pc_map = new HashMap<String, PCWrapper>();
	
	@JavascriptInterface
	public void create_pc(String pc_id) {
		PCWrapper pc_wrapper = new PCWrapper(null, null);
		pc_map.put(pc_id, pc_wrapper);
	}
	
	@JavascriptInterface
	public void add_stream(String pc_id) {
		PCWrapper pc_wrapper = pc_map.get(pc_id);
		pc_wrapper.addStream();
	}
}
