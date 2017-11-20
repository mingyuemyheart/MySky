package com.cxwl.weather.eye;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.utils.CustomHttpClient;

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
			asyncQuery("https://tqwy.tianqi.cn/tianqixy/logininfo?");
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
	private void asyncQuery(String requestUrl) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("UserNo", etUserName.getText().toString());
	        NameValuePair pair2 = new BasicNameValuePair("UserPwd", etPwd.getText().toString());
	        
			nvpList.add(pair1);
			nvpList.add(pair2);
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			cancelDialog();
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
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
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
