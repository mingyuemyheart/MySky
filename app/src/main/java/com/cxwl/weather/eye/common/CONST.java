package com.cxwl.weather.eye.common;

import android.os.Environment;

public class CONST {
	
	//广播标示
	public static String CIRCLE_CONTROLER = "circle_controler";//圆形按钮控制器
	public static String SIME_CIRCLE_CONTROLER = "sime_circle_controler";//半圆形按钮控制器
    public static long min = 1000*60;
	public static long EXPERIENCETIME = min*20;//体验时间，默认20分钟
	public static long EXPERIENCEREFRESH = min*1;//刷新间隔时间，默认1分钟
	
	//下拉刷新progresBar四种颜色
	public static final int color1 = android.R.color.holo_red_dark;
	public static final int color2 = android.R.color.holo_red_light;
	public static final int color3 = android.R.color.holo_red_dark;
	public static final int color4 = android.R.color.holo_red_light;

	//通用
	public static String SDCARD_PATH = Environment.getExternalStorageDirectory()+"/WeatherEyeNew";
	public static String DOWNLOAD_ADDR = SDCARD_PATH + "/download/";//下载视频保存的路径
	
}
