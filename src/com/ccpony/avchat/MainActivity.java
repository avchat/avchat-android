package com.ccpony.avchat;

import com.ccpony.avchat.peerconnection.PCManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	private JSController js_controller = null;
	private PCManager pc_manager = null;
	private String	js_controller_url = "http://192.168.10.250:4444/index.html";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		js_controller = new JSController(this);
		pc_manager = new PCManager(js_controller.get_webView(), this);
		this.setContentView(pc_manager.get_line_layout());
		
		js_controller.start(js_controller_url, pc_manager);
		System.out.println("onCreate");
		Log.d("main", "oncreate");
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
