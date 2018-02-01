package com.cxwl.weather.eye.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.view.MyVideoView;

/**
 * 引导视频
 * @author shawn_sun
 *
 */

public class WelcomeVideoActivity extends BaseActivity implements OnClickListener{
	
	private MyVideoView videoView = null;
	private TextView tvExit = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome_video);
		initWidget();
		initVideoView();
	}
	
	private void initWidget() {
		tvExit = (TextView) findViewById(R.id.tvExit);
		tvExit.setOnClickListener(this);
	}
	
	private void initVideoView() {
		videoView = (MyVideoView) findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.welcome));
        videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer arg0) {
		        videoView.start();
			}
		});
        videoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				intentLogin();
			}
		});
	}
	
	private void intentLogin() {
		startActivity(new Intent(WelcomeVideoActivity.this, LoginActivity.class));
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (videoView != null) {
			videoView.stopPlayback();
			videoView = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvExit:
			intentLogin();
			break;

		default:
			break;
		}
	}

}
