package com.ccpony.avchat;

import android.app.Activity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.ccpony.avchat.peerconnection.PCManager;

public class JSController {
	private WebView js_runtime = null;

	public JSController(Activity activity) {
		js_runtime = new WebView(activity);
		js_runtime.getSettings().setJavaScriptEnabled(true);
		
		// 调试支持用
		js_runtime.setWebChromeClient(new WebChromeClient() {
			public void onConsoleMessage(String message, int lineNumber,
					String sourceID) {
				Log.d("js_runtime", message + " -- From line " + lineNumber
						+ " of " + sourceID);
			}
		});
	}

	public WebView get_webView() {
		return js_runtime;
	}

	public void start(String js_controller_url, PCManager pcManager) {
		js_runtime.addJavascriptInterface(pcManager, "pcManagerProxy");
		js_runtime.loadUrl(js_controller_url);
	}

	public void stop() {
		js_runtime.removeJavascriptInterface("pcManagerProxy");
		js_runtime.getSettings().setJavaScriptEnabled(false);
		js_runtime.loadUrl("");
		js_runtime = null;
	}
}
