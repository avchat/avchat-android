package com.ccpony.avchat;

import android.app.Activity;
import android.webkit.WebView;

import com.ccpony.avchat.app.AppJS2Java;
import com.ccpony.avchat.app.AppJava2JS;
import com.ccpony.avchat.peerconnection.PCJS2Java;
import com.ccpony.avchat.peerconnection.PCJava2JS;
import com.ccpony.avchat.player.PlayerJS2JavaA;
import com.ccpony.avchat.player.PlayerJS2JavaV;
import com.ccpony.avchat.player.PlayerJava2JSA;
import com.ccpony.avchat.player.PlayerJava2JSV;

public class JSController {
	private WebView			js_runtime = null;
	private String 			js_controller_url = "http://192.168.1.201:3001/index.html";
	
	private AppJava2JS 		app_java2js = null;
	private AppJS2Java 		app_js2java = null;
	
	private PCJava2JS		pc_java2js = null;
	private PCJS2Java		pc_js2java = null;
	
	private PlayerJava2JSA	player_java2js_a = null;
	private PlayerJS2JavaA	player_js2java_a = null;
	
	private PlayerJava2JSV	player_java2js_v = null;
	private PlayerJS2JavaV	player_js2java_v = null;
	
	
	public WebView getJs_runtime() {
		return js_runtime;
	}

	public void setJs_runtime(WebView js_runtime) {
		this.js_runtime = js_runtime;
	}

	public AppJava2JS getApp_java2js() {
		return app_java2js;
	}

	public void setApp_java2js(AppJava2JS app_java2js) {
		this.app_java2js = app_java2js;
	}

	public AppJS2Java getApp_js2java() {
		return app_js2java;
	}

	public void setApp_js2java(AppJS2Java app_js2java) {
		this.app_js2java = app_js2java;
	}

	public PCJava2JS getPc_java2js() {
		return pc_java2js;
	}

	public void setPc_java2js(PCJava2JS pc_java2js) {
		this.pc_java2js = pc_java2js;
	}

	public PCJS2Java getPc_js2java() {
		return pc_js2java;
	}

	public void setPc_js2java(PCJS2Java pc_js2java) {
		this.pc_js2java = pc_js2java;
	}

	public PlayerJava2JSA getPlayer_java2js_a() {
		return player_java2js_a;
	}

	public void setPlayer_java2js_a(PlayerJava2JSA player_java2js_a) {
		this.player_java2js_a = player_java2js_a;
	}

	public PlayerJS2JavaA getPlayer_js2java_a() {
		return player_js2java_a;
	}

	public void setPlayer_js2java_a(PlayerJS2JavaA player_js2java_a) {
		this.player_js2java_a = player_js2java_a;
	}

	public PlayerJava2JSV getPlayer_java2js_v() {
		return player_java2js_v;
	}

	public void setPlayer_java2js_v(PlayerJava2JSV player_java2js_v) {
		this.player_java2js_v = player_java2js_v;
	}

	public PlayerJS2JavaV getPlayer_js2java_v() {
		return player_js2java_v;
	}

	public void setPlayer_js2java_v(PlayerJS2JavaV player_js2java_v) {
		this.player_js2java_v = player_js2java_v;
	}	
	
	public void start(Activity activity) {
		js_runtime = new WebView(activity);		
		js_runtime.getSettings().setJavaScriptEnabled(true);	
		
		// add app interface
		js_runtime.addJavascriptInterface(app_java2js, "app_java2js");
		js_runtime.addJavascriptInterface(app_js2java, "app_js2java");
		
		// add peerconnection interface
		js_runtime.addJavascriptInterface(pc_java2js, "pc_java2js");
		js_runtime.addJavascriptInterface(pc_js2java, "pc_js2java");
		
		// add audio player interface
		js_runtime.addJavascriptInterface(player_java2js_a, "player_java2js_a");
		js_runtime.addJavascriptInterface(player_js2java_a, "player_js2java_a");
		
		// add video player interface
		js_runtime.addJavascriptInterface(player_java2js_v, "player_java2js_v");
		js_runtime.addJavascriptInterface(player_js2java_v, "player_js2java_v");
						
		js_runtime.loadUrl(js_controller_url);
	}
	
	public void stop() {
		// remove app interface
		js_runtime.removeJavascriptInterface("app_java2js");
		js_runtime.removeJavascriptInterface("app_js2java");
		
		// remove peerconnection interface
		js_runtime.removeJavascriptInterface("pc_java2js");
		js_runtime.removeJavascriptInterface("pc_js2java");
		
		// remove audio player interface
		js_runtime.removeJavascriptInterface("player_java2js_a");
		js_runtime.removeJavascriptInterface("player_js2java_a");
		
		// remove video player interface
		js_runtime.removeJavascriptInterface("player_java2js_v");
		js_runtime.removeJavascriptInterface("player_js2java_v");
		
		js_runtime.getSettings().setJavaScriptEnabled(false);
		js_runtime = null;	
	}
}
