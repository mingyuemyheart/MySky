package com.cxwl.weather.eye.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.utils.AuthorityUtil;
import com.cxwl.weather.eye.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 延时摄影
 */
public class ShawnDelayActivity extends BaseActivity implements View.OnClickListener, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

	private ConstraintLayout reTitle;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private MediaPlayer mPlayer;
	private ImageView ivPlay,ivExpand;
	private Configuration configuration;//方向监听器
	private String videoUrl,videoName;
	private LinearLayout llShare,llSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_delay);
		showDialog();
		initWidget();
		initSurfaceView();
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
		setSurfaceViewLayout();
	}

	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		reTitle.setVisibility(View.VISIBLE);
		ivExpand.setImageResource(R.drawable.icon_expand);
		fullScreen(false);
	}

	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reTitle.setVisibility(View.GONE);
		ivExpand.setImageResource(R.drawable.icon_collose);
		fullScreen(true);
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

	private void initWidget() {
		reTitle = findViewById(R.id.reTitle);
		ImageView ivBack = findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.icon_close);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("延时摄影");
		ivPlay = findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		ivExpand = findViewById(R.id.ivControl);
		ivExpand.setOnClickListener(this);
		ivExpand.setVisibility(View.VISIBLE);
		llShare = findViewById(R.id.llShare);
		llShare.setOnClickListener(this);
		llSave = findViewById(R.id.llSave);
		llSave.setOnClickListener(this);

		showPort();

	}

	private void setSurfaceViewLayout() {
		int width = CommonUtil.widthPixels(this);
		int height = width * 9 / 16;
		ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
		params.width = width;
		params.height = height;
		surfaceView.setLayoutParams(params);
	}

	/**
	 * 初始化surfaceView
	 */
	private void initSurfaceView() {
		surfaceView = findViewById(R.id.surfaceView);
		surfaceView.setOnClickListener(this);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);

		setSurfaceViewLayout();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setDisplay(holder);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);

		videoUrl = "https://mysky-app.weather.com.cn/sky//31010100991187545008/video/202108/2021-08-21%2020:00:01.mp4";
		videoName = "测试";
		//设置显示视频显示在SurfaceView上
		try {
			if (!TextUtils.isEmpty(videoUrl)) {
				mPlayer.setDataSource(videoUrl);
				mPlayer.prepareAsync();
				llShare.setVisibility(View.VISIBLE);
				llSave.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		surfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = holder;
		releaseMediaPlayer();
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		cancelDialog();
		swithVideo();
	}

	private void swithVideo() {
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				ivPlay.setImageResource(R.drawable.icon_play);
			}else {
				mPlayer.start();
				ivPlay.setImageResource(R.drawable.icon_pause);
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		handler.removeMessages(1001);
		ivPlay.setVisibility(View.VISIBLE);
		ivPlay.setImageResource(R.drawable.icon_play);
	}

	/**
	 * 释放MediaPlayer资源
	 */
	private void releaseMediaPlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1001:
					ivPlay.setVisibility(View.GONE);
					break;
			}
		}
	};

	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 */
	private void dialogDownload(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = view.findViewById(R.id.tvMessage);
		TextView tvContent = view.findViewById(R.id.tvContent);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(this, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		tvNegtive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		tvPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				downloadVideo();
			}
		});
	}

	/**
	 * 下载视频
	 */
	private void downloadVideo() {
		if (TextUtils.isEmpty(videoUrl)) {
			return;
		}
		DownloadManager dManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(videoUrl);
		DownloadManager.Request request = new DownloadManager.Request(uri);
		// 设置下载路径和文件名
		String filename = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);//获取文件名称
		String filePath = getExternalFilesDir(null).getAbsolutePath();
		Log.e("filePath", "filePath="+filePath+",fileName="+filename);
		request.setDestinationInExternalPublicDir(getExternalFilesDir(null).getAbsolutePath(), filename);
		request.setDescription(filename);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setMimeType("application/vnd.android.package-archive");
		// 设置为可被媒体扫描器找到
		request.allowScanningByMediaScanner();
		// 设置为可见和可管理
		request.setVisibleInDownloadsUi(true);
		long referneceId = dManager.enqueue(request);
	}

	private void exit() {
		if (configuration == null) {
			finish();
			overridePendingTransition(R.anim.shawn_in_uptodown, R.anim.shawn_out_uptodown);
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				finish();
				overridePendingTransition(R.anim.shawn_in_uptodown, R.anim.shawn_out_uptodown);
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
		ivPlay.setVisibility(View.GONE);
		releaseMediaPlayer();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				exit();
				break;
			case R.id.ivExpand:
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
			case R.id.surfaceView:
				if (ivPlay.getVisibility() == View.VISIBLE) {
					ivPlay.setVisibility(View.GONE);
				}else {
					ivPlay.setVisibility(View.VISIBLE);
					handler.removeMessages(1001);
					Message msg = handler.obtainMessage(1001);
					msg.what = 1001;
					handler.sendMessageDelayed(msg, 5000);
				}
				break;
			case R.id.ivPlay:
				swithVideo();
				break;
			case R.id.llShare:
				CommonUtil.share(this, getString(R.string.app_name), getString(R.string.app_name), videoUrl);
				break;
			case R.id.llSave:
				checkAuthority();
				break;

		}
	}

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			dialogDownload("视频下载", "确定要下载该视频文件？");
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				dialogDownload("视频下载", "确定要下载该视频文件？");
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
						dialogDownload("视频下载", "确定要下载该视频文件？");
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
