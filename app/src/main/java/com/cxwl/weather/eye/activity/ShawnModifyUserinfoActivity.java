package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
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
 * 修改个人信息
 * @author shawn_sun
 */
public class ShawnModifyUserinfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private TextView tvControl;
	private EditText editText;
	private String title,value;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_modify_userinfo);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvControl = findViewById(R.id.tvControl);
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setText("保存");
		editText = findViewById(R.id.editText);
		
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
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
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
								OkHttpModify();
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
	private void OkHttpModify() {
		final String url = "https://tqwy.tianqi.cn/tianqixy/userInfo/updateuser";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("UserID", MyApplication.UID);
		String key = null;
		if (TextUtils.equals(title, getString(R.string.modify_nickname))) {
			key = "UserName";
		}else if (TextUtils.equals(title, getString(R.string.modify_mail))) {
			key = "UserMail";
		}else if (TextUtils.equals(title, getString(R.string.modify_phone))) {
			key = "UserPhone";
		}
		builder.add(key, editText.getText().toString().trim());
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
												//把用户信息保存在sharedPreferance里
												SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.USERINFO, Context.MODE_PRIVATE);
												Editor editor = sharedPreferences.edit();
												if (TextUtils.equals(title, getString(R.string.modify_nickname))) {
													editor.putString(MyApplication.UserInfo.nickname, editText.getText().toString().trim());
													MyApplication.NICKNAME = editText.getText().toString().trim();
												}else if (TextUtils.equals(title, getString(R.string.modify_mail))) {
													editor.putString(MyApplication.UserInfo.mail, editText.getText().toString().trim());
													MyApplication.MAIL = editText.getText().toString().trim();
												}else if (TextUtils.equals(title, getString(R.string.modify_phone))) {
													editor.putString(MyApplication.UserInfo.phone, editText.getText().toString().trim());
													MyApplication.PHONE = editText.getText().toString().trim();
												}
												editor.apply();

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
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
