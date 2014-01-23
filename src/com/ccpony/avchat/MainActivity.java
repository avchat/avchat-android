package com.ccpony.avchat;

import com.ccpony.avchat.peerconnection.PCManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
	private JSController js_controller = new JSController(this);
	private PCManager pcManager = new PCManager(js_controller.get_webView(), this);
	private String	js_controller_url = "http://192.168.1.201:3001/index.html";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		js_controller.start(js_controller_url, pcManager);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		js_controller.stop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
}
