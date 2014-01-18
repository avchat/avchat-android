package com.ccpony.avchat.peerconnection;

import android.webkit.WebView;

public class PCJava2JS {
	private WebView js_runtime = null;
	
	public PCJava2JS(WebView js_runtime) {
		this.js_runtime = js_runtime;
	}

	public void onicecandidate(String param1) {
		js_runtime.loadUrl("javascript:peerconnection.onicecandidate("+ param1 +")"); 
	}
	
	public void onaddstream() {
		js_runtime.loadUrl("javascript:peerconnection.onaddstream()");
	}
	
	public void onremovestream() {
		js_runtime.loadUrl("javascript:peerconnection.onremovestream()");
	}
	
	public void oniceconnectionstatechange() {
		js_runtime.loadUrl("javascript:peerconnection.oniceconnectionstatechange()");
	}
	
	public void cb_createOffer() {
		js_runtime.loadUrl("javascript:peerconnection.cb_createOffer()");
	}
	
	public void cb_createAnswer() {
		js_runtime.loadUrl("javascript:peerconnection.cb_createAnswer()");
	}	
}
