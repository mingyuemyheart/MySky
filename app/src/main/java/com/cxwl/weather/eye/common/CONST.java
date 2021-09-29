package com.cxwl.weather.eye.common;

public class CONST {

	public static String BASE_URL = "https://mysky-app.weather.com.cn/app9030";
	
	//广播标示
	public static String CIRCLE_CONTROLER = "circle_controler";//圆形按钮控制器
	public static String SIME_CIRCLE_CONTROLER = "sime_circle_controler";//半圆形按钮控制器
    public static long min = 1000*60;
	public static long EXPERIENCETIME = min*20;//体验时间，默认20分钟
	public static long EXPERIENCEREFRESH = min*1;//刷新间隔时间，默认1分钟

	//下拉刷新progresBar四种颜色
	public static final int color1 = android.R.color.holo_blue_bright;
	public static final int color2 = android.R.color.holo_blue_light;
	public static final int color3 = android.R.color.holo_blue_bright;
	public static final int color4 = android.R.color.holo_blue_light;

	public static final String ACTIVITY_NAME = "activity_name";//界面名称
	public static final String WEB_URL = "web_Url";//网页地址的标示

	public static String JPG = ".jpg";

	//通用
	public static double locationLat = 39.084158, locationLng = 117.200983;//默认天津市中心定位点
	public static String DECISION_USER = "1";//决策用户，"0"为非决策用户
	public static String MEMBER_USER = "true";//会员用户，"false"为非会员用户

}
