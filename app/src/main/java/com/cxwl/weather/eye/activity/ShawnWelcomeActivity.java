package com.cxwl.weather.eye.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.view.MyVideoView;

/**
 * 欢迎界面
 */
public class ShawnWelcomeActivity extends BaseActivity implements OnClickListener{

	private MyVideoView videoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_welcome);
		initWidget();
		initVideoView();
	}
	
	private void initWidget() {
		ImageView imageView = findViewById(R.id.imageView);
		TextView tvExit = findViewById(R.id.tvExit);
		tvExit.setOnClickListener(this);

		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(2000);
		alphaAnimation.setFillAfter(true);
		imageView.startAnimation(alphaAnimation);

	}
	
	private void initVideoView() {
		videoView = findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.welcome_video));
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
		startActivity(new Intent(ShawnWelcomeActivity.this, ShawnLoginActivity.class));
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
