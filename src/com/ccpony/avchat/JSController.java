package com.ccpony.avchat;

import android.app.Activity;
import android.webkit.WebView;

import com.ccpony.avchat.peerconnection.PCManager;

public class JSController {
	private Activity activity = null;
	private WebView	js_runtime = null;		 
	private PCManager pcManager = null;
	
	public JSController(Activity activity) {
		this.activity = activity;
		js_runtime = new WebView(this.activity);
		js_runtime.getSettings().setJavaScriptEnabled(true);
		pcManager = new PCManager(js_runtime);		
		js_runtime.addJavascriptInterface(pcManager, "pcManagerProxy");
	}
	
	public void start(String js_controller_url) {
		js_runtime.loadUrl(js_controller_url);
	}
	
	public void stop() {
		js_runtime.removeJavascriptInterface("pcManagerProxy");
		js_runtime.getSettings().setJavaScriptEnabled(false);
		js_runtime.loadUrl("");		
		js_runtime = null;	
	}
}
