package com.cxwl.weather.eye.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import com.cxwl.weather.eye.R;
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
import java.util.ArrayList;
import java.util.List;

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
    @SuppressWarnings("deprecation")
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
    public static void share(final Activity activity) {
    	ShareAction panelAction = new ShareAction(activity);
		panelAction.setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.SMS);
		panelAction.setShareboardclickCallback(new ShareBoardlistener() {
			@Override
			public void onclick(SnsPlatform arg0, SHARE_MEDIA arg1) {
				ShareAction sAction = new ShareAction(activity);
				sAction.setPlatform(arg1);
				UMWeb web = new UMWeb("https://www.tianqi.cn/tianqiwangyan.html");
				web.setTitle(activity.getString(R.string.app_name));//标题
				web.setDescription("集天气要素、图片、视频采集、存储、加工、夜彩功能于一体的实景天气软硬件整体解决方案");
				web.setThumb(new UMImage(activity, R.drawable.eye_icon));
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
    
}
