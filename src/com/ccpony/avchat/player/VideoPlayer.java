package com.ccpony.avchat.player;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoTrack;

import com.ccpony.avchat.view.VideoStreamsView;

public class VideoPlayer {
	public String player_id = "";

	public VideoPlayer(String player_id, VideoStreamsView vsv, VideoTrack videoTrack) {
		this.player_id = player_id;
		
		videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(vsv,
				VideoStreamsView.Endpoint.LOCAL)));
	}

	private class VideoCallbacks implements VideoRenderer.Callbacks {
		private final VideoStreamsView view;
		private final VideoStreamsView.Endpoint stream;

		public VideoCallbacks(VideoStreamsView view,
				VideoStreamsView.Endpoint stream) {
			this.view = view;
			this.stream = stream;
		}

		@Override
		public void setSize(final int width, final int height) {
			view.queueEvent(new Runnable() {
				public void run() {
					view.setSize(stream, width, height);
				}
			});
		}

		@Override
		public void renderFrame(I420Frame frame) {
			view.queueFrame(stream, frame);
		}
	}
}
