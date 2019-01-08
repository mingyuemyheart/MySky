package com.cxwl.weather.eye.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.AuthorityUtil;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.utils.SecretUrlUtil;
import com.cxwl.weather.eye.utils.WeatherUtil;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 视频详情
 */
public class ShawnVideoDetailActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private RelativeLayout reTitle;
	private EyeDto data;
	private Configuration configuration;//方向监听器
	private ProgressBar progressBar;
	private TXCloudVideoView mPlayerView;
	private TXLivePlayer mLivePlayer;
	private LinearLayout llControl;
	private TextView tvTemp, tvHumidity, tvQuality, tvWindSpeed, tvWindDir, tvPressure;
	private long expericenceTime;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_video_detail);
		mContext = this;
		initWidget();
	}

	private void initTimer() {
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(10001);
				}
			}, 0, CONST.EXPERIENCEREFRESH);//一分钟刷新一次
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 10001) {
				CommonUtil.saveExperienceTime(mContext, System.currentTimeMillis()-expericenceTime);//保存体验时间
				if (CommonUtil.showExperienceTime(mContext)) {
					CommonUtil.dialogExpericence(mContext);
					resetTimer();
					if (mLivePlayer != null) {
						mLivePlayer.stopPlay(true);// true代表清除最后一帧画面
						mLivePlayer = null;
					}
					if (mPlayerView != null) {
						mPlayerView.onDestroy();
						mPlayerView = null;
					}
				}
			}
		}
	};

	private void resetTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
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
		llControl = findViewById(R.id.llControl);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
//		ivShare.setVisibility(View.VISIBLE);
		ImageView ivControl = findViewById(R.id.ivControl);
		ivControl.setOnClickListener(this);
		ivControl.setVisibility(View.VISIBLE);
		LinearLayout llDelay = findViewById(R.id.llDelay);
		llDelay.setOnClickListener(this);
		LinearLayout llPicture = findViewById(R.id.llPicture);
		llPicture.setOnClickListener(this);
		LinearLayout llCalendar = findViewById(R.id.llCalendar);
		llCalendar.setOnClickListener(this);
		LinearLayout llSetting = findViewById(R.id.llSetting);
		llSetting.setOnClickListener(this);
		TextView tvWeather = findViewById(R.id.tvWeather);
		tvWeather.setOnClickListener(this);

		tvTemp = findViewById(R.id.tvTemp);
		tvQuality = findViewById(R.id.tvQuality);
		tvHumidity = findViewById(R.id.tvHumidity);
		tvPressure = findViewById(R.id.tvPressure);
		tvWindSpeed = findViewById(R.id.tvWindSpeed);
		tvWindDir = findViewById(R.id.tvWindDir);

		if (TextUtils.equals(MyApplication.USERTYPE, "1")) {//1为决策用户有权限操作摄像头
			llSetting.setVisibility(View.VISIBLE);
		}else {
			llSetting.setVisibility(View.GONE);
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

				checkAuthority();
			}
		}
	}

	private void getWeatherInfo() {
		if (!TextUtils.isEmpty(data.lng) && !TextUtils.isEmpty(data.lat)) {
			OkHttpGeo(Double.valueOf(data.lng), Double.valueOf(data.lat));
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
						expericenceTime = System.currentTimeMillis();
						initTimer();
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
		resetTimer();

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
		Bundle bundle;
		switch (v.getId()) {
			case R.id.llBack:
				exit();
				break;
			case R.id.ivShare:
				Bitmap bitmap1 = CommonUtil.captureMyView(mPlayerView);
				CommonUtil.share(this, bitmap1);
				break;
			case R.id.ivControl:
				if (configuration == null) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}else {
					if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					}
				}
				break;
			case R.id.tvWeather:
				intent = new Intent(mContext, ShawnForecastActivity.class);
				bundle = new Bundle();
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			case R.id.llDelay:
				intent = new Intent(mContext, ShawnDelayActivity.class);
				bundle = new Bundle();
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.shawn_in_downtoup, R.anim.shawn_out_downtoup);
				break;
			case R.id.llPicture:
				intent = new Intent(mContext, ShawnPictureWallActivity.class);
				bundle = new Bundle();
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.shawn_in_downtoup, R.anim.shawn_out_downtoup);
				break;
			case R.id.llCalendar:
				intent = new Intent(mContext, ShawnCalendarActivity.class);
				bundle = new Bundle();
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.shawn_in_downtoup, R.anim.shawn_out_downtoup);
				break;
			case R.id.llSetting:
				intent = new Intent(mContext, ShawnVideoSettingActivity.class);
				bundle = new Bundle();
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.shawn_in_downtoup, R.anim.shawn_out_downtoup);
				break;

			default:
				break;
			}
	}

	/**
	 * 获取天气数据
	 */
	private void OkHttpGeo(final double lng, final double lat) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.geo(lng, lat)).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("geo")) {
									JSONObject geoObj = obj.getJSONObject("geo");
									if (!geoObj.isNull("id")) {
										String cityId = geoObj.getString("id");
										getWeatherInfo(cityId);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

	private void getWeatherInfo(final String cityId) {
		if (TextUtils.isEmpty(cityId)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				WeatherAPI.getWeather2(mContext, cityId, Constants.Language.ZH_CN, new AsyncResponseHandler() {
					@Override
					public void onComplete(final Weather content) {
						super.onComplete(content);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String result = content.toString();
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);

										//实况信息
										if (!obj.isNull("l")) {
											JSONObject object = obj.getJSONObject("l");
											if (!object.isNull("l1")) {
												String factTemp = WeatherUtil.lastValue(object.getString("l1"));
												if (factTemp != null) {
													tvTemp.setText(factTemp+"℃");
												}
											}
											if (!object.isNull("l2")) {
												String humidity = WeatherUtil.lastValue(object.getString("l2"));
												if (humidity != null) {
													tvHumidity.setText(humidity+"%");
												}
											}
											if (!object.isNull("l10")) {
												String pressure = WeatherUtil.lastValue(object.getString("l10"));
												if (pressure != null) {
													tvPressure.setText(pressure+"hPa");
												}
											}
											if (!object.isNull("l3")) {
												String windForce = WeatherUtil.lastValue(object.getString("l3"));
												if (windForce != null) {
													tvWindSpeed.setText(WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
												}
											}
											if (!object.isNull("l4")) {
												String windDir = WeatherUtil.lastValue(object.getString("l4"));
												if (windDir != null) {
													tvWindDir.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))));
												}
											}
										}

										//空气质量
										if (!obj.isNull("k")) {
											JSONObject k = obj.getJSONObject("k");
											if (!k.isNull("k3")) {
												String airQua = WeatherUtil.lastValue(k.getString("k3"));
												if (airQua != null) {
													tvQuality.setText(airQua);
												}
											}
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

							}
						});
					}

					@Override
					public void onError(Throwable error, String content) {
						super.onError(error, content);
					}
				});

			}
		}).start();
	}

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.READ_PHONE_STATE
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			getWeatherInfo();
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				getWeatherInfo();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_LOCATION:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int gResult : grantResults) {
						if (gResult != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						getWeatherInfo();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						checkAuthority();
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
							checkAuthority();
							break;
						}
					}
				}
				break;
		}
	}

}
