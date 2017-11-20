package com.cxwl.weather.eye.common;

import android.app.Application;

import com.umeng.socialize.PlatformConfig;

public class MyApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	{
		//umeng分享的平台注册
		PlatformConfig.setWeixin("wx907025d7235082ed", "b77e6317267a09999d585ccdf144f7cc");
		PlatformConfig.setQQZone("1106012860", "SUwxXCjqWlTcRlb0");
	}
	
}
