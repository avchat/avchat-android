package com.ccpony.avchat.player;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoTrack;

import com.ccpony.avchat.view.VideoStreamsView;

public class VideoPlayer {
	public VideoTrack videoTrack;
	
	public VideoPlayer(VideoStreamsView vsv) {
		videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(vsv,
			VideoStreamsView.Endpoint.LOCAL)));
	}
	
	// Implementation detail: bridge the VideoRenderer.Callbacks interface to
	// the
	// VideoStreamsView implementation.
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
