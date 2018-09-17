package com.cxwl.weather.eye.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.ForePositionAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.RoundMenuView;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 视频操作
 */
public class VideoSettingActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private RelativeLayout reTitle;
	private EyeDto data;
	private Configuration configuration;//方向监听器
	private ProgressBar progressBar;
	private TXCloudVideoView mPlayerView;
	private TXLivePlayer mLivePlayer;

	private float startDegree = 0;//开始角度
	private float clickDegree = 0;//选中角度
	private ImageView ivMenuDir;
	private MyBroadCastReceiver mReceiver;
	private String commandBaseUrl = "https://tqwy.tianqi.cn/tianqixy/userInfo/facilordeinset";//发送指令baseUrl
	//操作类型 1 上，2下，3左，4右，5右上，6左上，7右下，8左下，
			//10变倍大，11变倍小，13聚焦近，14聚焦远，17打开光圈，18关闭光圈，19打开雨刷，
			//20关闭雨刷，23巡航开始，24巡航关闭，30亮度，31色度，32对比度，33饱和度，28预位置
	private String orderType = "1";
	private String OrederValue1 = "50";//0-100,水平、垂直速度、亮度、对比度、饱和度、色度，默认为50
	private int speed = 50;//速度
	private int brightness = 50;//亮度
	private int contrast = 50;//对比度
	private int saturation = 50;//饱和度
	private int chroma = 50;//色度
	
	private ImageView ivMinuse1, ivPlus1;
	private SeekBar seekBar1, seekBar2, seekBar3, seekBar4;
	private TextView tvSeekBar1, tvSeekBar2, tvSeekBar3, tvSeekBar4,tvValue1,tvForePosition;
	private static final int HANDLER_SPEED_MINUSE_DOWN = 2;
	private static final int HANDLER_SPEED_MINUSE_UP = 3;
	private static final int HANDLER_SPEED_PLUS_DOWN = 4;
	private static final int HANDLER_SPEED_PLUS_UP = 5;
	private boolean isClick = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videosetting);
		mContext = this;
		initBroadCastReceiver();
		initWidget();
		initRoundMenuView();
	}
	
	private void initBroadCastReceiver() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CONST.CIRCLE_CONTROLER);
		registerReceiver(mReceiver, intentFilter);
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("视频设置");
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		reTitle = findViewById(R.id.reTitle);
		progressBar = findViewById(R.id.progressBar);
		ImageView ivBack = findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.eye_btn_close);
		ivMenuDir = findViewById(R.id.ivMenuDir);
		ivMinuse1 = findViewById(R.id.ivMinuse1);
		ivMinuse1.setOnTouchListener(ivMinuse1Listener);
		ImageView ivMinuse2 = findViewById(R.id.ivMinuse2);
		ivMinuse2.setOnClickListener(this);
		ImageView ivMinuse3 = findViewById(R.id.ivMinuse3);
		ivMinuse3.setOnClickListener(this);
		ImageView ivMinuse4 = findViewById(R.id.ivMinuse4);
		ivMinuse4.setOnClickListener(this);
		ImageView ivMinuse5 = findViewById(R.id.ivMinuse5);
		ivMinuse5.setOnClickListener(this);
		ivPlus1 = findViewById(R.id.ivPlus1);
		ivPlus1.setOnTouchListener(ivPlus1Listener);
		ImageView ivPlus2 = findViewById(R.id.ivPlus2);
		ivPlus2.setOnClickListener(this);
		ImageView ivPlus3 = findViewById(R.id.ivPlus3);
		ivPlus3.setOnClickListener(this);
		ImageView ivPlus4 = findViewById(R.id.ivPlus4);
		ivPlus4.setOnClickListener(this);
		ImageView ivPlus5 = findViewById(R.id.ivPlus5);
		ivPlus5.setOnClickListener(this);
		tvValue1 = findViewById(R.id.tvValue1);
		tvSeekBar1 = findViewById(R.id.tvSeekBar1);
		tvSeekBar2 = findViewById(R.id.tvSeekBar2);
		tvSeekBar3 = findViewById(R.id.tvSeekBar3);
		tvSeekBar4 = findViewById(R.id.tvSeekBar4);
		seekBar1 = findViewById(R.id.seekBar1);
		seekBar2 = findViewById(R.id.seekBar2);
		seekBar3 = findViewById(R.id.seekBar3);
		seekBar4 = findViewById(R.id.seekBar4);
		seekBar1.setOnSeekBarChangeListener(seekBarListener1);
		seekBar2.setOnSeekBarChangeListener(seekBarListener2);
		seekBar3.setOnSeekBarChangeListener(seekBarListener3);
		seekBar4.setOnSeekBarChangeListener(seekBarListener4);
		LinearLayout ll8 = findViewById(R.id.ll8);
		ll8.setOnClickListener(this);
		tvForePosition = findViewById(R.id.tvForePosition);
		
		tvValue1.setText(speed+"");
		tvSeekBar1.setText(brightness+"");
		tvSeekBar2.setText(contrast+"");
		tvSeekBar3.setText(saturation+"");
		tvSeekBar4.setText(chroma+"");
		
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
				OkHttpParameter("https://tqwy.tianqi.cn/tianqixy/userInfo/obtain");
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
	
	private OnTouchListener ivMinuse1Listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isClick = true;
				Thread mThread = new Thread() {
					public void run() {
						while (isClick) {
							handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_DOWN);
							speed--;
							if (speed <= 0) {
								speed = 0;
							}
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				};
				mThread.start();
				break;
			case MotionEvent.ACTION_UP:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_UP);
				break;
			case MotionEvent.ACTION_CANCEL:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_UP);
				break;

			default:
				break;
			}
			return true;
		}
	};
	
	private OnTouchListener ivPlus1Listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isClick = true;
				Thread mThread = new Thread() {
					public void run() {
						while (isClick) {
							handler.sendEmptyMessage(HANDLER_SPEED_PLUS_DOWN);
							speed++;
							if (speed >= 100) {
								speed = 100;
							}
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				};
				mThread.start();
				break;
			case MotionEvent.ACTION_UP:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_PLUS_UP);
				break;
			case MotionEvent.ACTION_CANCEL:
				isClick = false;
				handler.sendEmptyMessage(HANDLER_SPEED_PLUS_UP);
				break;

			default:
				break;
			}
			return true;
		}
	};
	
	/**
	 * 获取摄像头移动速度、亮度、饱和度、对比度、色度
	 */
	private void OkHttpParameter(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("FID", data.fId);//设备id
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {

						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("code")) {
												String code  = object.getString("code");
												if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
													if (!object.isNull("list")) {
														JSONArray array = object.getJSONArray("list");
														JSONObject obj = array.getJSONObject(0);
														if (!obj.isNull("speed")) {
															speed = obj.getInt("speed");
															tvValue1.setText(speed+"");
														}
														if (!obj.isNull("brightness")) {
															brightness = obj.getInt("brightness");
															tvSeekBar1.setText(brightness+"");
															seekBar1.setProgress(brightness);
														}
														if (!obj.isNull("contrast")) {
															contrast = obj.getInt("contrast");
															tvSeekBar2.setText(contrast+"");
															seekBar2.setProgress(contrast);
														}
														if (!obj.isNull("saturation")) {
															saturation = obj.getInt("saturation");
															tvSeekBar3.setText(saturation+"");
															seekBar3.setProgress(saturation);
														}
														if (!obj.isNull("chroma")) {
															chroma = obj.getInt("chroma");
															tvSeekBar4.setText(chroma+"");
															seekBar4.setProgress(chroma);
														}
													}
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
				});
			}
		}).start();
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
		fullScreen(false);
		switchVideo();
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reTitle.setVisibility(View.GONE);
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

    @SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {  
	    public void handleMessage(Message msg) {  
	    	switch (msg.what) {
			case HANDLER_SPEED_MINUSE_DOWN:
				OrederValue1 = speed+"";
				tvValue1.setText(speed+"");
				ivMinuse1.setImageResource(R.drawable.eye_btn_minuse_press);
				break;
			case HANDLER_SPEED_MINUSE_UP:
				ivMinuse1.setImageResource(R.drawable.eye_btn_minuse);
				break;
			case HANDLER_SPEED_PLUS_DOWN:
				OrederValue1 = speed+"";
				tvValue1.setText(speed+"");
				ivPlus1.setImageResource(R.drawable.eye_btn_plus_press);
				break;
			case HANDLER_SPEED_PLUS_UP:
				ivPlus1.setImageResource(R.drawable.eye_btn_plus);
				break;

			default:
				break;
			}
	    	
	    };  
	};  
	
	/**
	 * 初始化圆形菜单按钮
	 */
	private void initRoundMenuView() {
		RoundMenuView roundMenuView = findViewById(R.id.roundMenuView);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		int width = dm.widthPixels;
		LayoutParams params = roundMenuView.getLayoutParams();
		params.width = width*3/5;
		params.height = width*3/5;
		roundMenuView.setLayoutParams(params);
		roundMenuView.setRadius(params.width/2);
		roundMenuView.setCenterXY(params.width/2, params.height/2);
		
		LayoutParams params2 = ivMenuDir.getLayoutParams();
		params2.width = params.width/3;
		params2.height = params.height/3;
		ivMenuDir.setLayoutParams(params2);
	}
	
	/**
	 * 旋转菜单动画
	 */
	private void rotateMenu(ImageView image, float startDegree, float endDegree) {
		RotateAnimation rotate = new RotateAnimation(startDegree, endDegree,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(500);
		rotate.setFillAfter(true);
		image.startAnimation(rotate);
	}
	
	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (TextUtils.equals(intent.getAction(), CONST.CIRCLE_CONTROLER)) {//接收圆形控制器指令
				Bundle bundle = intent.getExtras();
				orderType = bundle.getString("orderType");
				OrederValue1 = speed+"";
				if (TextUtils.equals(orderType, "7")) {//右下
					clickDegree = 135;
				}else if (TextUtils.equals(orderType, "2")) {//下
					clickDegree = 180;
				}else if (TextUtils.equals(orderType, "8")) {//左下
					clickDegree = 225;
				}else if (TextUtils.equals(orderType, "3")) {//左
					clickDegree = 270;
				}else if (TextUtils.equals(orderType, "6")) {//左上
					clickDegree = 315;
				}else if (TextUtils.equals(orderType, "1")) {//上
					clickDegree = 360;
				}else if (TextUtils.equals(orderType, "5")) {//右上
					clickDegree = 45;
				}else if (TextUtils.equals(orderType, "4")) {//右
					clickDegree = 90;
				}
				if (startDegree == 0) {
					rotateMenu(ivMenuDir, startDegree, clickDegree-45);
				}else {
					rotateMenu(ivMenuDir, startDegree-45, clickDegree-45);
				}
				startDegree = clickDegree;
				OkHttpCommand(commandBaseUrl);
			}
		}
	}
	
	private OnSeekBarChangeListener seekBarListener1 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			brightness = arg1;
			OrederValue1 = brightness+"";
			tvSeekBar1.setText(brightness+"");
			orderType = "30";//亮度
			OkHttpCommand(commandBaseUrl);
		}
	};
	
	private OnSeekBarChangeListener seekBarListener2 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			contrast = arg1;
			OrederValue1 = contrast+"";
			tvSeekBar2.setText(contrast+"");
			orderType = "32";//对比度
			OkHttpCommand(commandBaseUrl);
		}
	};
	
	private OnSeekBarChangeListener seekBarListener3 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			saturation = arg1;
			OrederValue1 = saturation+"";
			tvSeekBar3.setText(saturation+"");
			orderType = "33";//饱和度
			OkHttpCommand(commandBaseUrl);
		}
	};
	
	private OnSeekBarChangeListener seekBarListener4 = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			chroma = arg1;
			OrederValue1 = chroma+"";
			tvSeekBar4.setText(chroma+"");
			orderType = "31";//色度
			OkHttpCommand(commandBaseUrl);
		}
	};
	
	/**
	 * 发送指令
	 */
	private void OkHttpCommand(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("FID", data.fId);//设备id
		builder.add("FacilityZid", data.fGroupId);//设备组id
		builder.add("FacilityIP", data.fGroupIp);//设备组ip
		builder.add("OrederType", orderType);//命令类型
		builder.add("OrederValue1", OrederValue1);//水平速度
		builder.add("OrederValue2", OrederValue1);//垂直速度
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (!object.isNull("code")) {
											String code  = object.getString("code");
											if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
												//success
											}else if (TextUtils.equals(code, "701")) {
												if (!object.isNull("reason")) {
													String reason = object.getString("reason");
													if (!TextUtils.isEmpty(reason)) {
														Toast toast = Toast.makeText(mContext, reason, Toast.LENGTH_LONG);
														toast.setGravity(Gravity.TOP, 0, 300);
														toast.show();
													}
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
				});
			}
		}).start();
	}

	/**
	 * 设置预位置
	 */
	private void foreDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_fore_position, null);
		TextView tvMessage = view.findViewById(R.id.tvMessage);
		tvMessage.setText("设置预位置");
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		
		final List<EyeDto> foreList = new ArrayList<>();
		foreList.clear();
		for (int i = 0; i < 10; i++) {
			EyeDto dto = new EyeDto();
			dto.forePosition = i+"";
			foreList.add(dto);
		}
		ListView listView = view.findViewById(R.id.listView);
		ForePositionAdapter foreAdapter = new ForePositionAdapter(mContext, foreList);
		listView.setAdapter(foreAdapter);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				dialog.dismiss();
				EyeDto dto = foreList.get(arg2);
				tvForePosition.setText(dto.forePosition);
				OrederValue1 = tvForePosition.getText().toString();
				if (TextUtils.isEmpty(tvForePosition.getText().toString())) {
					OrederValue1 = "0";
				}
				orderType = "25";//查看预位置
				OkHttpCommand(commandBaseUrl);
			}
		});
		
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}
	
	private void exit() {
		if (configuration == null) {
	        finish();
	        overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
		        finish();
		        overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
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
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
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
			overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
			break;
		case R.id.ivMinuse1:
			speed--;
			OrederValue1 = speed+"";
			tvValue1.setText(speed+"");
			break;
		case R.id.ivPlus1:
			speed++;
			OrederValue1 = speed+"";
			tvValue1.setText(speed+"");
			break;
		case R.id.ivMinuse2:
			OrederValue1 = speed+"";
			orderType = "11";//变倍小
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ivPlus2:
			OrederValue1 = speed+"";
			orderType = "10";//变倍大
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ivMinuse3:
			OrederValue1 = speed+"";
			orderType = "14";//聚焦近
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ivPlus3:
			OrederValue1 = speed+"";
			orderType = "13";//聚焦远
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ivMinuse4:
			OrederValue1 = speed+"";
			orderType = "18";//关闭光圈
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ivPlus4:
			OrederValue1 = speed+"";
			orderType = "17";//打开光圈
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ivMinuse5:
			OrederValue1 = speed+"";
			orderType = "20";//关闭雨刷
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ivPlus5:
			OrederValue1 = speed+"";
			orderType = "19";//打开雨刷
			OkHttpCommand(commandBaseUrl);
			break;
		case R.id.ll8://设置预位置
			foreDialog();
			break;

		default:
			break;
		}
	}

}
