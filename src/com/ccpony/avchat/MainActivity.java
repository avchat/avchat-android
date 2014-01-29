package com.ccpony.avchat;

import org.json.JSONObject;
import org.webrtc.PeerConnectionFactory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;

import com.ccpony.avchat.peerconnection.PCManager;
import com.ccpony.avchat.view.VideoStreamsView;

public class MainActivity extends Activity {
	private JSController js_controller = null;
	private PCManager pc_manager = null;
	//private String js_controller_url = "http://192.168.10.250:4444/index.html";
	private String js_controller_url = "http://192.168.1.201:3001/index.html";
	public LinearLayout layout_line = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		PeerConnectionFactory.initializeAndroidGlobals(this);

		js_controller = new JSController(this);
		this.layout_line = new LinearLayout(this);
		this.layout_line.setOrientation(LinearLayout.VERTICAL);
		pc_manager = new PCManager(js_controller.get_webView(), this,
				layout_line);
		// this.setContentView(pc_manager.get_line_layout());
		this.setContentView(this.layout_line);
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

	public void add_view(final LinearLayout line,final VideoStreamsView vsv) {
		runOnUiThread(new Runnable() {
			public void run() {
				line.addView(vsv);
			}
		});
	}
	
	public void cb_method(final String method, final String pc_id, final JSONObject param) {
		runOnUiThread(new Runnable() {
			public void run() {
				System.out.println(method);
				System.out.println(pc_id);
				System.out.println(param.toString());
				js_controller.get_webView().loadUrl("javascript:pcManagerJS.cb_method('"+ method + "','" + pc_id + "','" + param.toString() + "')");
			}
		});
	}
	
	public void cb_method(final String method, final String pc_id, final String param) {
		runOnUiThread(new Runnable() {
			public void run() {
				System.out.println(method);
				System.out.println("pc_id:"+pc_id);
				//System.out.println(param);
				js_controller.get_webView().loadUrl("javascript:pcManagerJS.cb_method('"+ method + "','" + pc_id + "',\"" + param + "\")");
			}
		});
	}
}
