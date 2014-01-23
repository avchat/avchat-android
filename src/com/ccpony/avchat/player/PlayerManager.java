package com.ccpony.avchat.player;

import java.util.HashMap;

import android.webkit.JavascriptInterface;

public class PlayerManager {
	HashMap<String, VideoPlayer> player_map = new HashMap<String, VideoPlayer>();
		
	@JavascriptInterface
	public void new_player() {
		VideoPlayer view_player = new VideoPlayer(null);
		player_map.put(null, view_player);
	}
	
	@JavascriptInterface
	public void delete_player() {		
		player_map.get(null);
	}
}
