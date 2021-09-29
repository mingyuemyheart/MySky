package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.MyApplication;

/**
 * 个人信息
 * @author shawn_sun
 */
public class ShawnUserinfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private TextView tvName,tvMail,tvPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_userinfo);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("用户信息");
		LinearLayout llName = findViewById(R.id.llName);
		llName.setOnClickListener(this);
		tvName = findViewById(R.id.tvName);
		LinearLayout llMail = findViewById(R.id.llMail);
		llMail.setOnClickListener(this);
		tvMail = findViewById(R.id.tvMail);
		LinearLayout llPhone = findViewById(R.id.llPhone);
		llPhone.setOnClickListener(this);
		tvPhone = findViewById(R.id.tvPhone);
		tvName.setText(MyApplication.NICKNAME);
		tvMail.setText(MyApplication.MAIL);
		tvPhone.setText(MyApplication.PHONE);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.llName:
			Intent intentName = new Intent(mContext, ShawnModifyUserinfoActivity.class);
			intentName.putExtra("title", getString(R.string.modify_nickname));
			intentName.putExtra("value", tvName.getText().toString());
			startActivityForResult(intentName, 0);
			break;
		case R.id.llMail:
			Intent intentMail = new Intent(mContext, ShawnModifyUserinfoActivity.class);
			intentMail.putExtra("title", getString(R.string.modify_mail));
			intentMail.putExtra("value", tvMail.getText().toString());
			startActivityForResult(intentMail, 1);
			break;
		case R.id.llPhone:
			Intent intentPhone = new Intent(mContext, ShawnModifyUserinfoActivity.class);
			intentPhone.putExtra("title", getString(R.string.modify_phone));
			intentPhone.putExtra("value", tvPhone.getText().toString());
			startActivityForResult(intentPhone, 2);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String modifyValue = null;
			if (bundle != null) {
				modifyValue = bundle.getString("modifyValue");
			}
			
			switch (requestCode) {
			case 0://修改昵称
				if (modifyValue != null) {
					tvName.setText(modifyValue);
				}
				break;
			case 1://修改邮箱
				if (modifyValue != null) {
					tvMail.setText(modifyValue);
				}
				break;
			case 2://修改电话
				if (modifyValue != null) {
					tvPhone.setText(modifyValue);
				}
				break;

			default:
				break;
			}
		}
	}
	
}
