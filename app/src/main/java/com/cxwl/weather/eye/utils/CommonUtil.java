package com.cxwl.weather.eye.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.location.LocationManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.activity.ShawnMemberActivity;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.common.MyApplication;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommonUtil {

	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static float dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return dpValue * scale;
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static float px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return pxValue / scale;
    }

	/**
	 * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
	 * @param context
	 * @return true 表示开启
	 */
	public static boolean isLocationOpen(final Context context) {
		LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
		boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network) {
			return true;
		}
		return false;
	}
    
    /**
	 * 解决ScrollView与ListView共存的问题
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		listView.setLayoutParams(params);
	}
	
	/**
	 * 解决ScrollView与GridView共存的问题
	 * 
	 */
	public static void setGridViewHeightBasedOnChildren(GridView gridView) {
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		
		Class<GridView> tempGridView = GridView.class; // 获得gridview这个类的class
		int column = -1;
        try {
 
            Field field = tempGridView.getDeclaredField("mRequestedNumColumns"); // 获得申明的字段
            field.setAccessible(true); // 设置访问权限
            column = Integer.valueOf(field.get(gridView).toString()); // 获取字段的值
        } catch (Exception e1) {
        }

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i+=column) {
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + (gridView.getVerticalSpacing() * (listAdapter.getCount()/column - 1) + 0);
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		gridView.setLayoutParams(params);
	}
	
	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		Bitmap image = null;
		AssetManager am = context.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	/**
	 * 读取assets下文件
	 * @param fileName
	 * @return
	 */
	public static String getFromAssets(Context context, String fileName) {
		String Result = "";
		try {
			InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result;
	}
	
	/**  
	 * 截取webView快照(webView加载的整个内容的大小)  
	 * @param webView  
	 * @return  
	 */  
	@SuppressWarnings("deprecation")
	public static Bitmap captureWebView(WebView webView){
	    Picture snapShot = webView.capturePicture();  
	    Bitmap bitmap = Bitmap.createBitmap(snapShot.getWidth(),snapShot.getHeight(), Bitmap.Config.ARGB_8888);  
	    Canvas canvas = new Canvas(bitmap);  
	    snapShot.draw(canvas);  
	    clearCanvas(canvas);
	    return bitmap;  
	}  
	
	/**
	 * 截取scrollView
	 * @param scrollView
	 * @return
	 */
	public static Bitmap captureScrollView(ScrollView scrollView) {  
        int height = 0;  
        // 获取scrollview实际高度  
        for (int i = 0; i < scrollView.getChildCount(); i++) {  
        	height += scrollView.getChildAt(i).getHeight();  
        	scrollView.getChildAt(i).setBackgroundColor(0xffffff);  
        }  
        // 创建对应大小的bitmap  
        Bitmap bitmap = Bitmap.createBitmap(scrollView.getWidth(), height, Config.ARGB_8888);  
        Canvas canvas = new Canvas(bitmap);  
        scrollView.draw(canvas);  
        clearCanvas(canvas);
        return bitmap;  
    }  
	
	/**
	 * 截取listview
	 * @param listView
	 * @return
	 */
    public static Bitmap captureListView(ListView listView){
        ListAdapter listAdapter = listView.getAdapter();
        int count = listAdapter.getCount();
        if (count > 30) {
        	count = 30;
		}
        List<View> childViews = new ArrayList<>(count);
        int totalHeight = 0;
        for(int i = 0; i < count; i++){
        	View itemView = listAdapter.getView(i, null, listView);
        	itemView.measure(0, 0); 
			childViews.add(itemView);
			totalHeight += itemView.getMeasuredHeight();
        }
        Bitmap bitmap = Bitmap.createBitmap(listView.getWidth(), totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int yPos = 0;
        //把每个ItemView生成图片，并画到背景画布上
        for(int j = 0; j < childViews.size(); j++){
            View itemView = childViews.get(j);
            int childHeight = itemView.getMeasuredHeight();
            itemView.layout(0, 0, listView.getWidth(), childHeight);
            itemView.buildDrawingCache();
            Bitmap itemBitmap = itemView.getDrawingCache();
            if(itemBitmap!=null){
                canvas.drawBitmap(itemBitmap, 0, yPos, null);
            }
            yPos = childHeight +yPos;
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        clearCanvas(canvas);
        return bitmap;
    }
    
    /**
     * 截屏自定义view
     * @param view
     * @return
     */
    public static Bitmap captureMyView(View view) {
		if (view == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		bitmap = view.getDrawingCache();
		clearCanvas(canvas);
		return bitmap;
	}
    
	/**
     * 截屏,可是区域
     * @return
     */
	public static Bitmap captureView(View view) {
		if (view == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		clearCanvas(canvas);
		return bitmap;
	}
	
	/**
	 * 合成图片
	 * @param bitmap1
	 * @param bitmap2
	 * @param isCover 判断是否为覆盖合成
	 * @return
	 */
	public static Bitmap mergeBitmap(Context context, Bitmap bitmap1, Bitmap bitmap2, boolean isCover) {
    	if (bitmap1 == null || bitmap2 == null) {
			return null;
		}
    	
    	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, width, width*bitmap1.getHeight()/bitmap1.getWidth(), true);
    	bitmap2 = Bitmap.createScaledBitmap(bitmap2, width, width*bitmap2.getHeight()/bitmap2.getWidth(), true);
    	
    	Bitmap bitmap;
        Canvas canvas;
        if (isCover) {
        	int height = bitmap1.getHeight();
        	if (bitmap1.getHeight() > bitmap2.getHeight()) {
				height = bitmap1.getHeight();
			}else {
				height = bitmap2.getHeight();
			}
        	bitmap = Bitmap.createBitmap(bitmap1.getWidth(), height, Config.ARGB_8888);
        	canvas = new Canvas(bitmap);
        	canvas.drawBitmap(bitmap1, 0, 0 , null);
        	canvas.drawBitmap(bitmap2, 0, 0, null);
		}else {
			bitmap = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight()+bitmap2.getHeight(), Config.ARGB_8888);
			canvas = new Canvas(bitmap);
        	canvas.drawBitmap(bitmap1, 0, 0 , null);
        	canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
		}
        clearCanvas(canvas);
        return bitmap;
    }
    
    public static void clearBitmap(Bitmap bitmap) {
		if (bitmap != null) {
//			if (!bitmap.isRecycled()) {
//				bitmap.recycle();
//			}
			bitmap = null;
			System.gc();
		}
	}
    
    public static void clearCanvas(Canvas canvas) {
    	if (canvas != null) {
			canvas = null;
		}
    }

	/**
	 * 分享功能
	 * @param activity
	 */
	public static void share(final Activity activity, final Bitmap bitmap) {
		ShareAction panelAction = new ShareAction(activity);
		panelAction.setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.QZONE);
		panelAction.setShareboardclickCallback(new ShareBoardlistener() {
			@Override
			public void onclick(SnsPlatform arg0, SHARE_MEDIA arg1) {
				ShareAction shareAction = new ShareAction(activity);
				shareAction.setPlatform(arg1);
				if (bitmap != null) {
					shareAction.withMedia(new UMImage(activity, bitmap));
				}
				shareAction.share();
			}
		});
		panelAction.open();
	}
    
    /**
     * 分享功能
     * @param activity
     */
    public static void share(final Activity activity, final String title, final String content, final String dataUrl) {
    	ShareAction panelAction = new ShareAction(activity);
		panelAction.setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.QZONE);
		panelAction.setShareboardclickCallback(new ShareBoardlistener() {
			@Override
			public void onclick(SnsPlatform arg0, SHARE_MEDIA arg1) {
				ShareAction sAction = new ShareAction(activity);
				sAction.setPlatform(arg1);
				UMWeb web = new UMWeb(dataUrl);
				web.setTitle(title);//标题
				web.setDescription(content);
				web.setThumb(new UMImage(activity, R.drawable.shawn_icon));
				sAction.withMedia(web);
				sAction.share();
			}
		});
        panelAction.open();
    }

	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
    
    /**
     * 获取风向
     * @param code
     * @return
     */
    public static String getWindDir(String code) {
		if (TextUtils.equals(code, "N")) {
			return "北";
		} else if (TextUtils.equals(code, "S"))  {
			return "南";
		} else if (TextUtils.equals(code, "W"))  {
			return "西";
		} else if (TextUtils.equals(code, "E"))  {
			return "东";
		} else if (TextUtils.equals(code, "NE"))  {
			return "东北";
		} else if (TextUtils.equals(code, "SE"))  {
			return "东南";
		} else if (TextUtils.equals(code, "NW"))  {
			return "西北";
		} else if (TextUtils.equals(code, "SW"))  {
			return "西南";
		} else if (TextUtils.equals(code, "NNE"))  {
			return "北东北";
		} else if (TextUtils.equals(code, "ENE"))  {
			return "东东北";
		} else if (TextUtils.equals(code, "ESE"))  {
			return "东东南";
		} else if (TextUtils.equals(code, "SSE"))  {
			return "南东南";
		} else if (TextUtils.equals(code, "SSW"))  {
			return "南西南";
		} else if (TextUtils.equals(code, "WSW"))  {
			return "西西南";
		} else if (TextUtils.equals(code, "WNW"))  {
			return "西西北";
		} else if (TextUtils.equals(code, "NNW"))  {
			return "北西北";
		}
		return "";
	}
    
    /**
     * 获取风向角度
     * @param code
     * @return
     */
    public static float getWindDegree(String code) {
		if (TextUtils.equals(code, "N")) {
			return 0;
		} else if (TextUtils.equals(code, "S"))  {
			return 180;
		} else if (TextUtils.equals(code, "W"))  {
			return 270;
		} else if (TextUtils.equals(code, "E"))  {
			return 90;
		} else if (TextUtils.equals(code, "NE"))  {
			return 45;
		} else if (TextUtils.equals(code, "SE"))  {
			return 135;
		} else if (TextUtils.equals(code, "NW"))  {
			return 315;
		} else if (TextUtils.equals(code, "SW"))  {
			return 225;
		} else if (TextUtils.equals(code, "NNE"))  {
			return 22.5f;
		} else if (TextUtils.equals(code, "ENE"))  {
			return 67.5f;
		} else if (TextUtils.equals(code, "ESE"))  {
			return 112.5f;
		} else if (TextUtils.equals(code, "SSE"))  {
			return 157.5f;
		} else if (TextUtils.equals(code, "SSW"))  {
			return 202.5f;
		} else if (TextUtils.equals(code, "WSW"))  {
			return 247.5f;
		} else if (TextUtils.equals(code, "WNW"))  {
			return 292.5f;
		} else if (TextUtils.equals(code, "NNW"))  {
			return 337.5f;
		}
		return 0;
	}

	/**
	 * 根据当前时间获取日期
	 * @param i (+1为后一天，-1为前一天，0表示当天)
	 * @return
	 */
	public static String getDate(String time, int i) {
		Calendar c = Calendar.getInstance();

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
		try {
			Date date = sdf2.parse(time);
			c.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		c.add(Calendar.DAY_OF_MONTH, i);
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
		String date = sdf1.format(c.getTime());
		return date;
	}

	/**
	 * 根据当前时间获取星期几
	 * @param i (+1为后一天，-1为前一天，0表示当天)
	 * @return
	 */
	public static String getWeek(int i) {
		String week = "";

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_WEEK, i);

		switch (c.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				week = "周日";
				break;
			case Calendar.MONDAY:
				week = "周一";
				break;
			case Calendar.TUESDAY:
				week = "周二";
				break;
			case Calendar.WEDNESDAY:
				week = "周三";
				break;
			case Calendar.THURSDAY:
				week = "周四";
				break;
			case Calendar.FRIDAY:
				week = "周五";
				break;
			case Calendar.SATURDAY:
				week = "周六";
				break;
		}

		return week;
	}

	public static String getWeek(String time) {
		String week = "";

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(new Date(sdf1.parse(time).getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		switch (c.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				week = "日";
				break;
			case Calendar.MONDAY:
				week = "一";
				break;
			case Calendar.TUESDAY:
				week = "二";
				break;
			case Calendar.WEDNESDAY:
				week = "三";
				break;
			case Calendar.THURSDAY:
				week = "四";
				break;
			case Calendar.FRIDAY:
				week = "五";
				break;
			case Calendar.SATURDAY:
				week = "六";
				break;
		}

		return week;
	}

	/**
	 * 保存体验时间
	 */
	public static void saveExperienceTime(Context context, long time) {
		long lastTime = readExperienceTime(context);//上一次保存的时间
		SharedPreferences sp = context.getSharedPreferences("EXPERIECETIME", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong("time", lastTime+time);
		editor.apply();
	}

	/**
	 * 读取体验时间
	 */
	private static long readExperienceTime(Context context) {
		SharedPreferences sp = context.getSharedPreferences("EXPERIECETIME", Context.MODE_PRIVATE);
		long lastTime = sp.getLong("time", 0);
		return lastTime;
	}

	/**
	 * 清除体验时间
	 */
	public static void clearExperienceTime(Context context) {
		SharedPreferences sp = context.getSharedPreferences("EXPERIECETIME", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.clear();
		editor.apply();
	}

	/**
	 * 是否显示成为会员对框框
	 * @return
	 */
	public static boolean showExperienceTime(Context context) {
		if (TextUtils.equals(MyApplication.USERTYPE, CONST.DECISION_USER)) {//决策用户，不用判断是否显示
			return false;
		}else {//非决策用户，判断是否是会员用户
			if (TextUtils.equals(MyApplication.AUTHORITY, CONST.MEMBER_USER)) {//会员用户
				return false;
			}else {
				long expericenceTime = CommonUtil.readExperienceTime(context);
				if (expericenceTime > CONST.EXPERIENCETIME) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 体验时间提醒
	 */
	public static void dialogExpericence(final Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_expericence, null);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(context, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				context.startActivity(new Intent(context, ShawnMemberActivity.class));
			}
		});
	}
    
}
