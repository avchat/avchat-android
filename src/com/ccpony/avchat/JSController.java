package com.ccpony.avchat;

import android.app.Activity;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ccpony.avchat.peerconnection.PCManager;

public class JSController {
	protected static final String TAG = "JSController";
	private WebView js_runtime = null;

	public JSController(Activity activity) {
		js_runtime = new WebView(activity);
		js_runtime.getSettings().setJavaScriptEnabled(true);
		js_runtime.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		// 调试支持用
		js_runtime.setWebChromeClient(new WebChromeClient());
//		js_runtime.setWebChromeClient(new WebChromeClient() {  // Purely for debugging.
//	        public boolean onConsoleMessage (ConsoleMessage msg) {
//	          Log.d(TAG, "console: " + msg.message() + " at " +
//	              msg.sourceId() + ":" + msg.lineNumber());
//	          return false;
//	        }
//	      });
//			js_runtime.setWebViewClient(new WebViewClient() {  // Purely for debugging.
//	        public void onReceivedError(
//	            WebView view, int errorCode, String description,
//	            String failingUrl) {
//	          Log.e(TAG, "JS error: " + errorCode + " in " + failingUrl +
//	              ", desc: " + description);
//	        }
//	      });
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
