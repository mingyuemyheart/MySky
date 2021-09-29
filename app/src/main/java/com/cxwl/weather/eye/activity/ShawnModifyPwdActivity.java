package com.cxwl.weather.eye.activity;

import android.content.Context;
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
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 修改密码
 */
public class ShawnModifyPwdActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private EditText etOldPwd,etNewPwd,etConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_modify_pwd);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etOldPwd = findViewById(R.id.etOldPwd);
		etNewPwd = findViewById(R.id.etNewPwd);
		etConfirm = findViewById(R.id.etConfirm);
		ImageView ivLogo = findViewById(R.id.ivLogo);
		TextView tvModify = findViewById(R.id.tvModify);
		tvModify.setOnClickListener(this);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;

		ViewGroup.LayoutParams params = ivLogo.getLayoutParams();
		params.width = width*2/3;
		ivLogo.setLayoutParams(params);
	}

	private void doModify() {
		if (checkInfo()) {
			showDialog();
			OkHttpModifyPwd();
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etOldPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入原始密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etNewPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入新密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etConfirm.getText().toString())) {
			Toast.makeText(mContext, "请输入确认密码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!TextUtils.equals(etNewPwd.getText().toString(), etConfirm.getText().toString())) {
			Toast.makeText(mContext, "新密码与确认密码不一致", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 修改密码
	 */
	private void OkHttpModifyPwd() {
		final String url = "https://api.bluepi.tianqi.cn/Home/api/changePass";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("user", MyApplication.USERNAME);
		builder.add("pwd", etOldPwd.getText().toString());
		builder.add("new_pwd", etNewPwd.getText().toString());
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
										if (!object.isNull("msg")) {
											String msg = object.getString("msg");
											if (!TextUtils.isEmpty(msg)) {
												Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
											}
										}
										if (!object.isNull("code")) {
											String code  = object.getString("code");
											if (TextUtils.equals(code, "200")) {//成功
												MyApplication.PASSWORD = etNewPwd.getText().toString();
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
			case R.id.tvModify:
				doModify();
				break;

		default:
			break;
		}
	}
}
