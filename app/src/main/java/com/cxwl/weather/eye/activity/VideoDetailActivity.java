package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.dto.EyeDto;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 视频详情
 */
public class VideoDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private RelativeLayout reTitle;
	private EyeDto data;
	private Configuration configuration;//方向监听器
	private ProgressBar progressBar;
	private TXCloudVideoView mPlayerView;
	private TXLivePlayer mLivePlayer;
	private LinearLayout llControl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videodetail);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("视频详情");
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		reTitle = findViewById(R.id.reTitle);
		progressBar = findViewById(R.id.progressBar);
		ImageView ivSetting = findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		ImageView ivWeather = findViewById(R.id.ivWeather);
		ivWeather.setOnClickListener(this);
		ImageView ivPicture = findViewById(R.id.ivPicture);
		ivPicture.setOnClickListener(this);
		llControl = findViewById(R.id.llControl);
		
		if (TextUtils.equals(MyApplication.USERAGENT, "0")) {//0为有权限操作摄像头
			ivSetting.setVisibility(View.VISIBLE);
		}else {//1为无权限操作摄像头
			ivSetting.setVisibility(View.GONE);
		}
		
		mPlayerView = findViewById(R.id.video_view);
		mPlayerView.setOnClickListener(this);
		mLivePlayer = new TXLivePlayer(mContext);
		mLivePlayer.setPlayerView(mPlayerView);
		showPort();
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				if (!TextUtils.isEmpty(data.StatusUrl)) {
					OkHttpNetState(data.StatusUrl);
				}else {
					initTXCloudVideoView(data.streamPublic);
				}
			}
		}
	}

	/**
	 * 获取内网是否可用，不可用切换位外网
	 */
	private final OkHttpClient.Builder builder = new OkHttpClient.Builder()
			.connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS);
	private final OkHttpClient okHttpClient = builder.build();

	private void OkHttpNetState(String url) {
		okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initTXCloudVideoView(data.streamPublic);
					}
				});
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initTXCloudVideoView(data.streamPrivate);
					}
				});
			}
		});
	}

	/**
	 * 初始化播放器
	 */
	private void initTXCloudVideoView(String streamUrl) {
		if (!TextUtils.isEmpty(streamUrl) && mLivePlayer != null) {
			mLivePlayer.startPlay(streamUrl, TXLivePlayer.PLAY_TYPE_LIVE_RTMP);
			mLivePlayer.setPlayListener(new ITXLivePlayListener() {
				@Override
				public void onPlayEvent(int arg0, Bundle arg1) {
					if (arg0 == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {//视频播放开始
						progressBar.setVisibility(View.GONE);
					}
				}
				
				@Override
				public void onNetStatus(Bundle status) {
					TextView tv = findViewById(R.id.tv);
					tv.setText("Current status, CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)+
			                ", RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)+
			                ", SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps"+
			                ", FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS)+
			                ", ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps"+
			                ", VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps");
				}
			});
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configuration = newConfig;
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPort();
		}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			showLand();
		}
	}
	
	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		reTitle.setVisibility(View.VISIBLE);
		llControl.setVisibility(View.VISIBLE);
		fullScreen(false);
		switchVideo();
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reTitle.setVisibility(View.GONE);
		llControl.setVisibility(View.GONE);
		fullScreen(true);
		switchVideo();
	}
	
	/**
	 * 横竖屏切换视频窗口
	 */
	private void switchVideo() {
		if (mPlayerView != null) {
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getRealMetrics(dm);
			int width = dm.widthPixels;
			int height = width*9/16;
			LayoutParams params = mPlayerView.getLayoutParams();
			params.width = width;
			params.height = height;
			mPlayerView.setLayoutParams(params);
		}
		if (mLivePlayer != null) {
			mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
			mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
		}
	}
	
	private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
	
	private void exit() {
		if (configuration == null) {
	        finish();
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
		        finish();
			}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLivePlayer != null) {
			mLivePlayer.stopPlay(true);// true代表清除最后一帧画面
			mLivePlayer = null;
		}
		if (mPlayerView != null) {
			mPlayerView.onDestroy();
			mPlayerView = null;
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.ivBack:
			exit();
			break;
		case R.id.llBack:
			finish();
			break;
		case R.id.ivSetting:
			intent = new Intent(mContext, VideoSettingActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", data);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.in_downtoup, R.anim.out_downtoup);
			break;
		case R.id.ivWeather:
			intent = new Intent(mContext, SelectWeatherActivity.class);
			bundle = new Bundle();
			bundle.putParcelable("data", data);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.in_downtoup, R.anim.out_downtoup);
			break;
		case R.id.ivPicture:
			intent = new Intent(mContext, PictureWallActivity.class);
			bundle = new Bundle();
			bundle.putParcelable("data", data);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.in_downtoup, R.anim.out_downtoup);
			break;

		default:
			break;
		}
	}

}
