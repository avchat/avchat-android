package com.ccpony.avchat;

import com.ccpony.avchat.device.AVDeviceManager;
import com.ccpony.avchat.peerconnection.PCManager;
import com.ccpony.avchat.player.PlayerManager;
import com.ccpony.avchat.view.ViewManager;

import android.app.Activity;
import android.webkit.WebView;

public class JSController {
	public WebView	js_runtime = null;
	public String	js_controller_url = "http://192.168.1.201:3001/index.html";
	
	public PlayerManager playerManager = new PlayerManager(); 
	public PCManager pcManager = new PCManager();
	public ViewManager viewManager = new ViewManager();
	public AVDeviceManager avDeviceManager = new AVDeviceManager();
	
	public void start(Activity activity) {
		js_runtime = new WebView(activity);		
		js_runtime.getSettings().setJavaScriptEnabled(true);	
		
		js_runtime.addJavascriptInterface(playerManager, "playerManager");
		js_runtime.addJavascriptInterface(pcManager, "pcManager");
		js_runtime.addJavascriptInterface(viewManager, "viewManager");
		js_runtime.addJavascriptInterface(avDeviceManager, "avDeviceManager");		
						
		js_runtime.loadUrl(js_controller_url);
	}
	
	public void stop() {
		js_runtime.loadUrl("");
		
		js_runtime.removeJavascriptInterface("avDeviceManager");
		js_runtime.removeJavascriptInterface("viewManager");
		js_runtime.removeJavascriptInterface("pcManager");
		js_runtime.removeJavascriptInterface("playerManager");
		
		js_runtime.getSettings().setJavaScriptEnabled(false);
		js_runtime = null;	
	}
}
