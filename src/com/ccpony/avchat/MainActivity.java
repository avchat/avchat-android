package com.ccpony.avchat;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.webrtc.PeerConnectionFactory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;

import com.ccpony.avchat.peerconnection.PCManager;
import com.ccpony.avchat.peerconnection.PCWrapper;
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
		this.setContentView(this.layout_line);
		
		pc_manager = new PCManager(js_controller.get_webView(), this,
				layout_line);
		// this.setContentView(pc_manager.get_line_layout());
		
		js_controller.start(js_controller_url, pc_manager);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		js_controller.stop();
		
		pc_manager.mediastream_stop(null);
		
		Iterator iter = pc_manager.map_pc.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter.next(); 
		    String key = (String)entry.getKey(); 
		    PCWrapper val = (PCWrapper)entry.getValue(); 
		    val.close();
		} 
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
				System.out.println("[pony86: java cb_method] " + method + "," + pc_id + "," + param.toString());
				js_controller.get_webView().loadUrl("javascript:pcManagerJS.cb_method('"+ method + "','" + pc_id + "','" + param.toString() + "')");
			}
		});
	}
}
