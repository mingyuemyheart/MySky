package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.utils.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录界面
 */
public class ShawnLoginActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private EditText etUserName,etPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_login);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etUserName = findViewById(R.id.etUserName);
		etPwd = findViewById(R.id.etPwd);
		TextView tvLogin = findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
		
		etUserName.setText(MyApplication.USERNAME);
		etPwd.setText(MyApplication.PASSWORD);

		if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPwd.getText().toString())) {
			etUserName.setSelection(etUserName.getText().toString().length());
			etPwd.setSelection(etPwd.getText().toString().length());
			doLogin();
		}
	}
	
	private void doLogin() {
		if (checkInfo()) {
			showDialog();
			OkHttpLogin();
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, "请输入用户名", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 登录
	 */
	private void OkHttpLogin() {
		final String url = "https://tqwy.tianqi.cn/tianqixy/logininfo?";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("UserNo", etUserName.getText().toString());
		builder.add("UserPwd", etPwd.getText().toString());
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
												if (!object.isNull("list")) {
													JSONObject obj = new JSONObject(object.getString("list"));
													if (!obj.isNull("Userid")) {
														MyApplication.UID = obj.getString("Userid");
													}

													if (!obj.isNull("UserAuthority")) {//0(标识管理员) 1 （组长） 2（普通）
														MyApplication.AUTHORITY = obj.getString("UserAuthority");
													}

													if (!obj.isNull("UserAgent")) {//设备操作权限（0为拥有操作权限 1没有）
														MyApplication.USERAGENT = obj.getString("UserAgent");
													}

													if (!obj.isNull("UserName")) {
														MyApplication.NICKNAME = obj.getString("UserName");
													}

													if (!obj.isNull("UserMail")) {
														MyApplication.MAIL = obj.getString("UserMail");
													}

													if (!obj.isNull("UserPhone")) {
														MyApplication.PHONE = obj.getString("UserPhone");
													}

													MyApplication.USERNAME = etUserName.getText().toString();
													MyApplication.PASSWORD = etPwd.getText().toString();
													MyApplication.saveUserInfo(mContext);

													startActivity(new Intent(mContext, ShawnMainActivity.class));
													finish();
												}
											}else if (TextUtils.equals(code, "100")) {//录入人员
												if (!object.isNull("list")) {
													MyApplication.USERNAME = etUserName.getText().toString();
													MyApplication.PASSWORD = etPwd.getText().toString();
													MyApplication.saveUserInfo(mContext);

													startActivity(new Intent(mContext, WriteParametersActivity.class));
													finish();
												}
											}else {
												//失败
												if (!object.isNull("reason")) {
													String reason = object.getString("reason");
													if (!TextUtils.isEmpty(reason)) {
														Toast.makeText(mContext, reason, Toast.LENGTH_SHORT).show();
													}
												}
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
		case R.id.tvLogin:
			doLogin();
			break;

		default:
			break;
		}
	}
}
