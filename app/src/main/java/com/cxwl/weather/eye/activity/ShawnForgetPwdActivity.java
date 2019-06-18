package com.cxwl.weather.eye.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.utils.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录界面
 */
public class ShawnForgetPwdActivity extends ShawnBaseActivity implements OnClickListener {
	
	private Context mContext;
	private EditText etUserName,etPwd;
	private TextView tvSend;
	private int seconds = 60;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_forget_pwd);
		mContext = this;
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("验证信息");
		etUserName = findViewById(R.id.etUserName);
		etPwd = findViewById(R.id.etPwd);
		tvSend = findViewById(R.id.tvSend);
		tvSend.setOnClickListener(this);
		TextView tvControl = findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("完成");
		tvControl.setVisibility(View.VISIBLE);
	}

	/**
	 * 获取验证码
	 */
	private void OkHttpCode() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, "请输入手机号码", Toast.LENGTH_SHORT).show();
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

		final String url = "https://api.bluepi.tianqi.cn/Home/api/sendVcode";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("user", etUserName.getText().toString());
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("status")) {
											if (obj.getInt("status") == 1) {//成功发送验证码
												//发送验证码成功
												etPwd.setFocusable(true);
												etPwd.setFocusableInTouchMode(true);
												etPwd.requestFocus();
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
					}else {
						tvSend.setText(seconds--+"s");
					}
					break;

				default:
					break;
			}
		};
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

	private void OkHttpVerify() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, "请输入手机号码", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入验证码", Toast.LENGTH_SHORT).show();
			return;
		}
		final String url = "https://api.bluepi.tianqi.cn/Home/api/userCodeAuth";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("user", etUserName.getText().toString());
		builder.add("num", etPwd.getText().toString());
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mContext, "验证码验证失败", Toast.LENGTH_SHORT).show();
							}
						});
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
											if (TextUtils.equals(obj.getString("meaning"), "200")) {
												Intent intent = new Intent(mContext, ShawnFindPwdActivity.class);
												intent.putExtra("user", etUserName.getText().toString());
												intent.putExtra("num", etPwd.getText().toString());
												startActivityForResult(intent, 1001);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvSend:
				OkHttpCode();
				break;
			case R.id.tvControl:
				OkHttpVerify();
				break;

			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1001:
					setResult(RESULT_OK);
					finish();
					break;
			}
		}
	}

}
