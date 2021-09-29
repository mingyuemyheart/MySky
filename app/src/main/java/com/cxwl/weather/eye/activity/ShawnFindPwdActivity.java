package com.cxwl.weather.eye.activity;

import android.content.Context;
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
 * 修改信息
 */
public class ShawnFindPwdActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private EditText etConfirm,etPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_find_pwd);
		mContext = this;
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("修改信息");
		etConfirm = findViewById(R.id.etConfirm);
		etPwd = findViewById(R.id.etPwd);
		TextView tvControl = findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("完成");
		tvControl.setVisibility(View.VISIBLE);
	}

	private void OkHttpModify() {
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入新密码", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(etConfirm.getText().toString())) {
			Toast.makeText(mContext, "请重新输入新密码", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.equals(etPwd.getText().toString(), etConfirm.getText().toString())) {
			Toast.makeText(mContext, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
			return;
		}

		final String url = "https://api.bluepi.tianqi.cn/Home/api/changePass_code";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("user", getIntent().getStringExtra("user"));
		builder.add("num", getIntent().getStringExtra("num"));
		builder.add("new_pwd", etConfirm.getText().toString());
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
										if (!obj.isNull("code")) {
											if (TextUtils.equals(obj.getString("code"), "200")) {//成功发送验证码
												Toast.makeText(mContext, "修改成功", Toast.LENGTH_SHORT).show();
												setResult(RESULT_OK);
												finish();
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
			case R.id.tvControl:
				OkHttpModify();
				break;

			default:
				break;
		}
	}

}
