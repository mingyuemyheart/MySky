package com.cxwl.weather.eye;

/**
 * 预览视频
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CustomHttpClient;
import com.cxwl.weather.eye.utils.CustomHttpClient2;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

@SuppressLint("SimpleDateFormat")
public class VideoDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private RelativeLayout reTitle = null;
	private EyeDto data = null;
	private Configuration configuration = null;//方向监听器
	private ProgressBar progressBar = null;
	private TXCloudVideoView mPlayerView = null;
	private TXLivePlayer mLivePlayer = null;

	private ImageView ivSetting = null;
	private ImageView ivWeather = null;
	private ImageView ivPicture = null;
	private LinearLayout llControl = null;
	
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
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("视频详情");
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ivSetting = (ImageView) findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		ivWeather = (ImageView) findViewById(R.id.ivWeather);
		ivWeather.setOnClickListener(this);
		ivPicture = (ImageView) findViewById(R.id.ivPicture);
		ivPicture.setOnClickListener(this);
		llControl = (LinearLayout) findViewById(R.id.llControl);
		
		if (TextUtils.equals(USERAGENT, "0")) {//0为有权限操作摄像头
			ivSetting.setVisibility(View.VISIBLE);
		}else {//1为无权限操作摄像头
			ivSetting.setVisibility(View.GONE);
		}
		
		mPlayerView = (TXCloudVideoView) findViewById(R.id.video_view);
		mPlayerView.setOnClickListener(this);
		mLivePlayer = new TXLivePlayer(mContext);
		mLivePlayer.setPlayerView(mPlayerView);
		showPort();
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				if (!TextUtils.isEmpty(data.StatusUrl)) {
					asyncQueryNetState(data.StatusUrl);
				}
			}
		}
	}
	
	/**
	 * 获取内网是否可用，不可用切换位外网
	 */
	private void asyncQueryNetState(String requestUrl) {
		HttpAsyncTaskNetState task = new HttpAsyncTaskNetState();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient2.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskNetState extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskNetState() {
		}
		
		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient2.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient2.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (!TextUtils.isEmpty(requestResult) && requestResult.contains("Active connections")) {
				initTXCloudVideoView(data.streamPrivate);
			}else {
				initTXCloudVideoView(data.streamPublic);
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 初始化播放器
	 */
	private void initTXCloudVideoView(String streamUrl) {
		if (!TextUtils.isEmpty(streamUrl)) {
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
					TextView tv = (TextView) findViewById(R.id.tv);
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
		switch (v.getId()) {
		case R.id.ivBack:
			exit();
			break;
		case R.id.llBack:
			finish();
			break;
		case R.id.ivSetting:
			Intent intentSet = new Intent(mContext, VideoSettingActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", data);
			intentSet.putExtras(bundle);
			startActivity(intentSet);
			overridePendingTransition(R.anim.in_downtoup, R.anim.out_downtoup);
			break;
		case R.id.ivWeather:
			Intent intentW = new Intent(mContext, SelectWeatherActivity.class);
			bundle = new Bundle();
			bundle.putParcelable("data", data);
			intentW.putExtras(bundle);
			startActivity(intentW);
			overridePendingTransition(R.anim.in_downtoup, R.anim.out_downtoup);
			break;
		case R.id.ivPicture:
			Intent intentPic = new Intent(mContext, PictureWallActivity.class);
			bundle = new Bundle();
			bundle.putParcelable("data", data);
			intentPic.putExtras(bundle);
			startActivity(intentPic);
			overridePendingTransition(R.anim.in_downtoup, R.anim.out_downtoup);
			break;

		default:
			break;
		}
	}

}
