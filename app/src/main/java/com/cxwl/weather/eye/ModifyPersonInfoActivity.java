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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.utils.CustomHttpClient;

/**
 * 修改个人信息
 * @author shawn_sun
 *
 */

public class ModifyPersonInfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvControl = null;
	private EditText editText = null;
	private String title = null;
	private String value = null;//修改内容

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_personinfo);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setText("保存");
		editText = (EditText) findViewById(R.id.editText);
		
		title = getIntent().getStringExtra("title");
		if (title != null) {
			tvTitle.setText(title);
		}
		
		value = getIntent().getStringExtra("value");
		if (value != null) {
			editText.setText(value);
			editText.setSelection(value.length());
		}
		
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
			}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (!TextUtils.equals(editText.getText().toString().trim(), value)) {
					tvControl.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							if (TextUtils.isEmpty(editText.getText().toString().trim())) {
								Toast.makeText(mContext, "修改内容不能为空", Toast.LENGTH_SHORT).show();
							}else {
								showDialog();
								asyncModify("https://tqwy.tianqi.cn/tianqixy/userInfo/updateuser");
							}
						}
					});
				}else {
					tvControl.setOnClickListener(null);
				}
			}
		});
	}
	
	/**
	 * 修改用户信息
	 */
	private void asyncModify(String requestUrl) {
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
			NameValuePair pair1 = new BasicNameValuePair("UserID", UID);
			String key = null;
			if (TextUtils.equals(title, getString(R.string.modify_nickname))) {
				key = "UserName";
			}else if (TextUtils.equals(title, getString(R.string.modify_mail))) {
				key = "UserMail";
			}else if (TextUtils.equals(title, getString(R.string.modify_phone))) {
				key = "UserPhone";
			}
	        NameValuePair pair2 = new BasicNameValuePair(key, editText.getText().toString().trim());
	        
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
								//把用户信息保存在sharedPreferance里
								SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
								Editor editor = sharedPreferences.edit();
								if (TextUtils.equals(title, getString(R.string.modify_nickname))) {
									editor.putString(UserInfo.nickname, editText.getText().toString().trim());
									NICKNAME = editText.getText().toString().trim();
								}else if (TextUtils.equals(title, getString(R.string.modify_mail))) {
									editor.putString(UserInfo.mail, editText.getText().toString().trim());
									MAIL = editText.getText().toString().trim();
								}else if (TextUtils.equals(title, getString(R.string.modify_phone))) {
									editor.putString(UserInfo.phone, editText.getText().toString().trim());
									PHONE = editText.getText().toString().trim();
								}
								editor.commit();
								
								Intent intent = new Intent();
								intent.putExtra("modifyValue", editText.getText().toString().trim());
								setResult(RESULT_OK, intent);
								finish();
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
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
