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
		UMConfigure.init(this, "58b9167e5312dd1ea90008b1", "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
		PlatformConfig.setWeixin("wx907025d7235082ed", "b77e6317267a09999d585ccdf144f7cc");
		PlatformConfig.setQQZone("1106012860", "SUwxXCjqWlTcRlb0");
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
	public static String UID = "";
	public static String USERNAME = "";
	public static String PASSWORD = "";
	public static String USERAGENT = "";//设备操作权限（0为拥有操作权限 1没有）
	public static String AUTHORITY = "";//用户权限,0(标识管理员) 1 （组长） 2（普通）
	public static String NICKNAME = "";//昵称
	public static String MAIL = "";//邮箱
	public static String PHONE = "";//电话

	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		private static final String uid = "uid";
		private static final String userName = "uName";
		private static final String passWord = "pwd";
		private static final String authority = "authority";//权限
		private static final String userAgent = "UserAgent";//操作摄像头权限
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
		UID = "";
		USERNAME = "";
		PASSWORD = "";
		USERAGENT = "";
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
		editor.putString(UserInfo.uid, UID);
		editor.putString(UserInfo.userName, USERNAME);
		editor.putString(UserInfo.passWord, PASSWORD);
		editor.putString(UserInfo.authority, AUTHORITY);
		editor.putString(UserInfo.userAgent, USERAGENT);
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
		UID = sharedPreferences.getString(UserInfo.uid, "");
		USERNAME = sharedPreferences.getString(UserInfo.userName, "");
		PASSWORD = sharedPreferences.getString(UserInfo.passWord, "");
		AUTHORITY = sharedPreferences.getString(UserInfo.authority, "");
		USERAGENT = sharedPreferences.getString(UserInfo.userAgent, "");
		NICKNAME = sharedPreferences.getString(UserInfo.nickname, "");
		MAIL = sharedPreferences.getString(UserInfo.mail, "");
		PHONE = sharedPreferences.getString(UserInfo.phone, "");
	}
	
}
