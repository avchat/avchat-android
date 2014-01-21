package com.ccpony.avchat;

import com.ccpony.avchat.device.AVDeviceManager;
import com.ccpony.avchat.peerconnection.PCManager;
import com.ccpony.avchat.player.PlayerManager;
import com.ccpony.avchat.view.ViewManager;

import android.app.Activity;
import android.webkit.WebView;

public class JSController {
	public WebView	js_runtime = null;	
	
	public PlayerManager playerManager = new PlayerManager(); 
	public PCManager pcManager = null;
	public ViewManager viewManager = new ViewManager();
	public AVDeviceManager avDeviceManager = null;
	
	public void start(Activity activity,String js_controller_url) {
		js_runtime = new WebView(activity);		
		js_runtime.getSettings().setJavaScriptEnabled(true);	
		
		js_runtime.addJavascriptInterface(playerManager, "playerManager");
		js_runtime.addJavascriptInterface(pcManager, "pcManager");
		js_runtime.addJavascriptInterface(viewManager, "viewManager");
		js_runtime.addJavascriptInterface(avDeviceManager, "avDeviceManager");
		
		avDeviceManager = new AVDeviceManager(js_runtime);
		pcManager = new PCManager(js_runtime);
						
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
