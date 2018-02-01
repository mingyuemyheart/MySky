package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录界面
 */

public class LoginActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private EditText etUserName = null;//用户名
	private EditText etPwd = null;//密码
	private TextView tvLogin = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etUserName = (EditText) findViewById(R.id.etUserName);
		etPwd = (EditText) findViewById(R.id.etPwd);
		tvLogin = (TextView) findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
		
		SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		String userName = sharedPreferences.getString(UserInfo.userName, null);
		String pwd = sharedPreferences.getString(UserInfo.passWord, null);
		etUserName.setText(userName);
		etPwd.setText(pwd);
		
		if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPwd.getText().toString())) {
			etUserName.setSelection(etUserName.getText().toString().length());
			etPwd.setSelection(etPwd.getText().toString().length());
			doLogin();
		}
	}
	
	private void doLogin() {
		if (checkInfo()) {
			showDialog();
			OkHttpLogin("https://tqwy.tianqi.cn/tianqixy/logininfo?");
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
	 * 异步请求
	 */
	private void OkHttpLogin(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("UserNo", etUserName.getText().toString());
		builder.add("UserPwd", etPwd.getText().toString());
		RequestBody body = builder.build();
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
												JSONObject obj = new JSONObject(object.getString("list"));
												String uid = null;
												if (!obj.isNull("Userid")) {
													uid = obj.getString("Userid");
												}

												String authority = null;//0(标识管理员) 1 （组长） 2（普通）
												if (!obj.isNull("UserAuthority")) {
													authority = obj.getString("UserAuthority");
												}

												String userAgent = null;//设备操作权限（0为拥有操作权限 1没有）
												if (!obj.isNull("UserAgent")) {
													userAgent = obj.getString("UserAgent");
												}

												String nickname = null;//昵称
												if (!obj.isNull("UserName")) {
													nickname = obj.getString("UserName");
												}

												String mail = null;
												if (!obj.isNull("UserMail")) {
													mail = obj.getString("UserMail");
												}

												String phone = null;
												if (!obj.isNull("UserPhone")) {
													phone = obj.getString("UserPhone");
												}

												//把用户信息保存在sharedPreferance里
												SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
												Editor editor = sharedPreferences.edit();
												editor.putString(UserInfo.userName, etUserName.getText().toString());
												editor.putString(UserInfo.passWord, etPwd.getText().toString());
												editor.putString(UserInfo.uId, uid);
												editor.putString(UserInfo.authority, authority);
												editor.putString(UserInfo.userAgent, userAgent);
												editor.putString(UserInfo.nickname, nickname);
												editor.putString(UserInfo.mail, mail);
												editor.putString(UserInfo.phone, phone);
												editor.commit();

												USERNAME = etUserName.getText().toString();
												PASSWORD = etPwd.getText().toString();
												UID = uid;
												AUTHORITY = authority;
												USERAGENT = userAgent;
												NICKNAME = nickname;
												MAIL = mail;
												PHONE = phone;

												startActivity(new Intent(mContext, WeatherEyeActivity.class));
												finish();
											}
										}else if (TextUtils.equals(code, "100")) {//录入人员
											if (!object.isNull("list")) {
												//把用户信息保存在sharedPreferance里
												SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
												Editor editor = sharedPreferences.edit();
												editor.putString(UserInfo.userName, etUserName.getText().toString());
												editor.putString(UserInfo.passWord, etPwd.getText().toString());
												editor.commit();

												USERNAME = etUserName.getText().toString();
												PASSWORD = etPwd.getText().toString();

												Intent intent = new Intent(mContext, WriteParametersActivity.class);
												startActivity(intent);
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
								}
								cancelDialog();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
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
