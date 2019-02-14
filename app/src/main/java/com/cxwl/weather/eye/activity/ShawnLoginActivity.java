package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 登录界面
 */
public class ShawnLoginActivity extends ShawnBaseActivity implements OnClickListener{
	
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
		ImageView ivLogo = findViewById(R.id.ivLogo);
		TextView tvRegister = findViewById(R.id.tvRegister);
		tvRegister.setOnClickListener(this);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;

		ViewGroup.LayoutParams params = ivLogo.getLayoutParams();
		params.width = width*2/3;
		ivLogo.setLayoutParams(params);
		
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
			Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_SHORT).show();
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
		final String url = String.format("https://tqwy.tianqi.cn/tianqixy/sjdl?user=%s&pwd=%s", etUserName.getText().toString(), etPwd.getText().toString());
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
												String type = object.getString("type");//1为决策用户
												MyApplication.USERNAME = etUserName.getText().toString();
												MyApplication.PASSWORD = etPwd.getText().toString();
												MyApplication.USERTYPE = type;
												MyApplication.saveUserInfo(mContext);

												startActivity(new Intent(mContext, ShawnMainActivity.class));
												finish();
											}else {
												Toast.makeText(mContext, code, Toast.LENGTH_SHORT).show();
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
			case R.id.tvRegister:
				startActivityForResult(new Intent(this, ShawnRegisterActivity.class), 1001);
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
					etUserName.setText(MyApplication.USERNAME);
					etPwd.setText(MyApplication.PASSWORD);

					if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPwd.getText().toString())) {
						etUserName.setSelection(etUserName.getText().toString().length());
						etPwd.setSelection(etPwd.getText().toString().length());
						doLogin();
					}
					break;
			}
		}
	}
}
