package com.cxwl.weather.eye.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.utils.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 注册
 */
public class ShawnRegisterActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private EditText etUserName,etCode,etPwd,etConfirm;
	private TextView tvSend;
	private int seconds = 60;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_register);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etUserName = findViewById(R.id.etUserName);
		etPwd = findViewById(R.id.etPwd);
		etCode = findViewById(R.id.etCode);
		etConfirm = findViewById(R.id.etConfirm);
		ImageView ivLogo = findViewById(R.id.ivLogo);
		tvSend = findViewById(R.id.tvSend);
		tvSend.setOnClickListener(this);
		TextView tvRegister = findViewById(R.id.tvRegister);
		tvRegister.setOnClickListener(this);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;

		ViewGroup.LayoutParams params = ivLogo.getLayoutParams();
		params.width = width*2/3;
		ivLogo.setLayoutParams(params);
	}

	/**
	 * 获取验证码
	 */
	private void OkHttpCode() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_SHORT).show();
			return;
		}

		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(101);
				}
			}, 0, 1000);
		}
		final String url = "https://tqwy.tianqi.cn/tianqixy/sjdx?user="+etUserName.getText().toString();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("meaning")) {
											if (TextUtils.equals(obj.getString("meaning"), "200")) {//成功发送验证码
												//发送验证码成功
												etCode.setFocusable(true);
												etCode.setFocusableInTouchMode(true);
												etCode.requestFocus();
											} else {//发送验证码失败
												resetTimer();
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

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 101:
					if (seconds <= 0) {
						resetTimer();
					} else {
						tvSend.setText(seconds-- + "s");
					}
					break;

				default:
					break;
			}
		}

		;
	};

	/**
	 * 重置计时器
	 */
	private void resetTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		seconds = 60;
		tvSend.setText("获取验证码");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		resetTimer();
	}
	
	private void doRegister() {
		if (checkInfo()) {
			showDialog();
			OkHttpRegister();
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etCode.getText().toString())) {
			Toast.makeText(mContext, "请输入验证码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etConfirm.getText().toString())) {
			Toast.makeText(mContext, "请输入确认密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!TextUtils.equals(etPwd.getText().toString(), etConfirm.getText().toString())) {
			Toast.makeText(mContext, "密码与确认密码不一致", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 注册
	 */
	private void OkHttpRegister() {
		final String url = String.format("https://tqwy.tianqi.cn/tianqixy/sjzc?user=%s&pwd=%s&num=%s", etUserName.getText().toString(), etPwd.getText().toString(), etCode.getText().toString());
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
										if (!object.isNull("meaning")) {
											String code  = object.getString("meaning");
											if (TextUtils.equals(code, "200")) {//成功
												MyApplication.USERNAME = etUserName.getText().toString();
												MyApplication.PASSWORD = etPwd.getText().toString();
												MyApplication.saveUserInfo(mContext);
												setResult(RESULT_OK);
												finish();
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								cancelDialog();
							}
						});
					}
				});
			}
		}).start();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvSend:
				OkHttpCode();
				break;
			case R.id.tvRegister:
				doRegister();
				break;

		default:
			break;
		}
	}
}
