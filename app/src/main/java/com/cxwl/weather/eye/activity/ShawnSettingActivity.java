package com.cxwl.weather.eye.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;

/**
 * 设置界面
 */
public class ShawnSettingActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_setting);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("设置");
		TextView tvLogout = findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
	}

	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 */
	private void dialogLogout(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_delete, null);
		TextView tvMessage = view.findViewById(R.id.tvMessage);
		TextView tvContent = view.findViewById(R.id.tvContent);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				CommonUtil.clearExperienceTime(mContext);
				MyApplication.clearUserInfo(mContext);
				OkHttpUtil.cookieMap.clear();
				startActivity(new Intent(mContext, ShawnLoginActivity.class));
				finish();
				MyApplication.destoryActivity();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvLogout:
			dialogLogout("退出登录", "确定要退出登录？");
			break;

		default:
			break;
		}
	}
	
}
