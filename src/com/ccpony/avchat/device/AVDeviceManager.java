package com.ccpony.avchat.device;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class AVDeviceManager {
	private WebView js_runtime = null;
	
	class Optional {
		public Boolean video;
		public Boolean audio;
		public Boolean fake;
	}
	
	public AVDeviceManager(WebView js_runtime) {
		this.js_runtime = js_runtime;
	}
	
	@JavascriptInterface
	public void get_user_media(Optional config) {	
		// get media logic
		
		// result return
		// if ok;
		js_runtime.loadUrl("javascript:avDeviceManager.callback()");
		// else
		js_runtime.loadUrl("javascript:avDeviceManager.error()");
	}	
}
