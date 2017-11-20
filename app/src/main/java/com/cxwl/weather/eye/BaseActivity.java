package com.cxwl.weather.eye;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.manager.SystemStatusManager;
import com.cxwl.weather.eye.view.MyDialog;


public class BaseActivity extends Activity{
	
	private Context mContext = null;
	private MyDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setTranslucentStatus();
	}
	
	/**
	 * 设置状态栏背景状态
	 */
	@SuppressLint("InlinedApi") 
	private void setTranslucentStatus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window win = getWindow();
			WindowManager.LayoutParams winParams = win.getAttributes();
			final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			winParams.flags |= bits;
			win.setAttributes(winParams);
		}
		SystemStatusManager tintManager = new SystemStatusManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0);// 状态栏无背景
	}
	
	public void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog(mContext);
		}
		mDialog.show();
	}
	
	public void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	//本地保存用户信息参数
	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		public static final String userName = "uName";
		public static final String passWord = "pwd";
		public static final String uId = "uId";
		public static final String authority = "authority";//权限
		public static final String userAgent = "UserAgent";//操作摄像头权限
		public static final String nickname = "nickname";//昵称
		public static final String mail = "mail";
		public static final String phone = "phone";
	}

	public static String USERNAME = null;//用户名
	public static String PASSWORD = null;//用户密码
	public static String UID = null;//用户id
	public static String USERAGENT = null;//设备操作权限（0为拥有操作权限 1没有）
	public static String AUTHORITY = null;//用户权限,0(标识管理员) 1 （组长） 2（普通）
	public static String NICKNAME = null;//昵称
	public static String MAIL = null;//邮箱
	public static String PHONE = null;//电话
	
}
