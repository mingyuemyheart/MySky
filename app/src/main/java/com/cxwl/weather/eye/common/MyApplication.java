package com.cxwl.weather.eye.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyApplication extends Application{

	private static MyApplication instance;
	private static Map<String,Activity> destoryMap = new HashMap<>();

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		getUserInfo(this);
		initUmeng();
	}
	
	private void initUmeng() {
		UMConfigure.init(this, "5c21b94cb465f5278f0001b9", "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
		PlatformConfig.setWeixin("wx073ab8dcab423158", "baec24d052f3a9bd8ea1f0ad153e669b");
		PlatformConfig.setQQZone("1108069568", "Tdgxw95ccp1hraJ5");
		UMConfigure.setLogEnabled(false);
	}

	public static MyApplication getApplication() {
		return instance;
	}

	/**
	 * 添加到销毁队列
	 * @param activity 要销毁的activity
	 */
	public static void addDestoryActivity(Activity activity, String activityName) {
		destoryMap.put(activityName,activity);
	}

	/**
	 *销毁指定Activity
	 */
	public static void destoryActivity() {
		Set<String> keySet=destoryMap.keySet();
		for (String key:keySet){
			destoryMap.get(key).finish();
		}
	}

	//本地保存用户信息参数
	public static String TOKEN = "";
	public static String UID = "";
	public static String USERNAME = "";
	public static String PASSWORD = "";
	public static String USERTYPE = "";//操作状态，"1"不允许操作摄像头、"0"允许
	public static String AUTHORITY = "";//用户权限,"true"为会员用户，"false"为非会员用户
	public static String NICKNAME = "";//昵称
	public static String MAIL = "";//邮箱
	public static String PHONE = "";//电话

	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		private static final String token = "token";
		private static final String uid = "uid";
		private static final String userName = "uName";
		private static final String passWord = "pwd";
		private static final String userType = "userType";
		private static final String authority = "authority";//权限
		public static final String nickname = "nickname";//昵称
		public static final String mail = "mail";
		public static final String phone = "phone";
	}

	/**
	 * 清除用户信息
	 */
	public static void clearUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
		TOKEN = "";
		UID = "";
		USERNAME = "";
		PASSWORD = "";
		USERTYPE = "";
		AUTHORITY = "";
		NICKNAME = "";
		MAIL = "";
		PHONE = "";
	}

	/**
	 * 保存用户信息
	 */
	public static void saveUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(UserInfo.token, TOKEN);
		editor.putString(UserInfo.uid, UID);
		editor.putString(UserInfo.userName, USERNAME);
		editor.putString(UserInfo.passWord, PASSWORD);
		editor.putString(UserInfo.userType, USERTYPE);
		editor.putString(UserInfo.authority, AUTHORITY);
		editor.putString(UserInfo.nickname, NICKNAME);
		editor.putString(UserInfo.mail, MAIL);
		editor.putString(UserInfo.phone, PHONE);
		editor.apply();
	}

	/**
	 * 获取用户信息
	 */
	public static void getUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		UID = sharedPreferences.getString(UserInfo.token, "");
		UID = sharedPreferences.getString(UserInfo.uid, "");
		USERNAME = sharedPreferences.getString(UserInfo.userName, "");
		PASSWORD = sharedPreferences.getString(UserInfo.passWord, "");
		USERTYPE = sharedPreferences.getString(UserInfo.userType, "");
		AUTHORITY = sharedPreferences.getString(UserInfo.authority, "");
		NICKNAME = sharedPreferences.getString(UserInfo.nickname, "");
		MAIL = sharedPreferences.getString(UserInfo.mail, "");
		PHONE = sharedPreferences.getString(UserInfo.phone, "");
	}
	
}
