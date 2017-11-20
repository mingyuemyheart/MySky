package com.cxwl.weather.eye.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Activity;


public class CONST {
	
	//广播标示
	public static String CIRCLE_CONTROLER = "circle_controler";//圆形按钮控制器
	public static String SIME_CIRCLE_CONTROLER = "sime_circle_controler";//半圆形按钮控制器
	
	public static String MANAGER = "0";//管理员身份
	public static String HEADMAN = "1";//组长省份
	public static String COMMON = "2";//普通用户身份
	
	//下拉刷新progresBar四种颜色
	public static final int color1 = android.R.color.holo_red_dark;
	public static final int color2 = android.R.color.holo_red_light;
	public static final int color3 = android.R.color.holo_red_dark;
	public static final int color4 = android.R.color.holo_red_light;
	
	//销毁登录状态前的activity
	private static Map<String,Activity> destoryMap = new HashMap<String, Activity>();

    /**
     * 添加到销毁队列
     * @param activity 要销毁的activity
     */
    public static void addDestoryActivity(Activity activity,String activityName) {
        destoryMap.put(activityName,activity);
    }
    
	/**
	*销毁指定Activity
	*/
    public static void destoryActivity(String activityName) {
       Set<String> keySet=destoryMap.keySet();
        for (String key:keySet){
            destoryMap.get(key).finish();
        }
    }
	
}
