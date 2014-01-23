package com.ccpony.avchat.view;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Point;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

public class ViewManager {
	HashMap<String, VideoStreamsView> view_map = new HashMap<String, VideoStreamsView>();
	LinearLayout line_layout = null;
	Activity activity = null;
	
	public ViewManager(Activity activity) {
		this.activity = activity;
		line_layout = new LinearLayout(activity);
	}
	
	@JavascriptInterface
	public void new_view(String view_id, int width, int height) {
		Point displaySize = new Point(width, height);		
		VideoStreamsView vsv = new VideoStreamsView(activity, displaySize);
		view_map.put(view_id, vsv);
		line_layout.addView(vsv);
	}

	@JavascriptInterface
	public void delete_view(String view_id) {
		VideoStreamsView vsv = view_map.get(view_id);
		line_layout.removeView(vsv);
	}
}
