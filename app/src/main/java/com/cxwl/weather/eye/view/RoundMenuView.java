package com.cxwl.weather.eye.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.utils.CommonUtil;

public class RoundMenuView extends ImageView implements OnGestureListener {
	
	private Context mContext = null;
	private static final int childMenuSize = 8;
	private static final float childAngle = 360f / childMenuSize;
	private float offsetAngle = 22.5f;
	private Paint paint;
	private GestureDetector gestureDetector;
	private int selectId = -1;
	private int radius = 270;//半径默认为270,单位px
	private int centerX = 270, centerY = 270;//默认半径，单位px
	private Bitmap circleBitmap = null;//圆形菜单
	private int menuWidth = radius*2, menuHeight = radius*2;
	private Bitmap dirBitmap = null;//带红点方向

	public RoundMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	@SuppressWarnings("deprecation")
	private void init() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		
		gestureDetector = new GestureDetector(this);
	}
	
	/**
	 * 设置半径长度
	 * @param radius
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	/**
	 * 设置圆心坐标
	 * @param x
	 * @param y
	 */
	public void setCenterXY(int x, int y) {
		this.centerX = x;
		this.centerY = y;
		menuWidth = x*2;
		menuHeight = y*2;
		circleBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), 
				R.drawable.eye_iv_circle_menu), menuWidth, menuHeight);
		dirBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), 
				R.drawable.eye_iv_circle_menu_dir), menuWidth/3, menuHeight/3);
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		
//		//画第一个圆
//		paint.setStyle(Paint.Style.FILL_AND_STROKE);
//		paint.setColor(0xff272a31);
//		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//		canvas.drawCircle(centerX, centerY, radius, paint);
//		
//		//画第二个圆
//		paint.setStyle(Paint.Style.FILL_AND_STROKE);
//		paint.setColor(0xff9c9c9c);
//		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//		canvas.drawCircle(centerX, centerY, radius-CommonUtil.dip2px(mContext, 5), paint);
//		
//		//画第三个圆
//		paint.setStyle(Paint.Style.FILL_AND_STROKE);
//		paint.setColor(0xff757575);
//		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//		canvas.drawCircle(centerX, centerY, radius/2, paint);
//		
//		// 画扇形
//		paint.setStyle(Paint.Style.FILL_AND_STROKE);
//		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
//		drawArc(canvas, new RectF(CommonUtil.dip2px(mContext, 5), CommonUtil.dip2px(mContext, 5), 
//				canvas.getWidth()-CommonUtil.dip2px(mContext, 5), canvas.getHeight()-CommonUtil.dip2px(mContext, 5)));
//		
//		//画第四个圆
//		paint.setStyle(Paint.Style.FILL_AND_STROKE);
//		paint.setColor(0xff272a31);
//		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//		canvas.drawCircle(centerX, centerY, radius/3, paint);
//		
//		//画第五个圆
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setColor(0x50ffffff);
//		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//		canvas.drawCircle(centerX, centerY, radius/3-CommonUtil.dip2px(mContext, 9), paint);
//		
//		//画第六个圆
//		paint.setStyle(Paint.Style.FILL_AND_STROKE);
//		paint.setColor(getResources().getColor(R.color.title_bg));
//		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//		canvas.drawCircle(centerX, centerY, radius/3-CommonUtil.dip2px(mContext, 12), paint);
		
		if (circleBitmap != null) {
			paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			paint.setColor(0xff272a31);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawBitmap(circleBitmap, 0, 0, paint);
		}
		
		// 画扇形
		drawArc(canvas, new RectF(CommonUtil.dip2px(mContext, 5), CommonUtil.dip2px(mContext, 5), 
				canvas.getWidth()-CommonUtil.dip2px(mContext, 5), canvas.getHeight()-CommonUtil.dip2px(mContext, 5)));
		
		//画方向圆
//		if (dirBitmap != null) {
//			paint.setStyle(Paint.Style.FILL_AND_STROKE);
//			paint.setColor(getResources().getColor(R.color.title_bg));
//			paint.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//			canvas.drawBitmap(dirBitmap, menuWidth/3, menuHeight/3, paint);
//		}
	}
	
	/**
	 * 画扇形
	 * @param canvas
	 * @param rectF
	 */
	private void drawArc(Canvas canvas, RectF rectF) {
		for (int i = 0; i < childMenuSize; i++) {
			if (i == selectId) { // 如果是选中就将扇形画成实心的,否则画空心的扇形
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setColor(0x50272a31);
				canvas.drawArc(rectF, i * childAngle + offsetAngle, childAngle, true, paint);
			} else {
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.TRANSPARENT);
				canvas.drawArc(rectF, i * childAngle + offsetAngle, childAngle, true, paint);
			}

//			// 计算扇形中心点的坐标
//			double x = centerX + getRoundX(centerX / 3.0f * 2, i, childMenuSize, offsetAngle + childAngle / 2);
//			double y = centerY + getRoundY(centerY / 3.0f * 2, i, childMenuSize, offsetAngle + childAngle / 2);
			
//			paint.setStyle(Paint.Style.FILL_AND_STROKE);
//			paint.setColor(Color.WHITE);
//			canvas.drawCircle((float)x, (float)y, 10, paint);
			

//			// 画三角形
//			Path path = new Path();
//			path.moveTo((float) x, (float) y);// 此点为多边形的起点
//			path.lineTo((float) x - i*10, (float) y + i*10);
//			path.lineTo((float) x + i*10, (float) y + i*10);
//			path.close(); // 使这些点构成封闭的多边形
//			paint.setColor(Color.WHITE);
//			paint.setStyle(Paint.Style.FILL_AND_STROKE);
//			canvas.drawPath(path, paint);
		}
	}

	/**
	 * 计算圆形等分扇形的点Y坐标
	 * @param r 圆形直径
	 * @param i 第几个等分扇形
	 * @param n  等分扇形个数
	 * @param offset_angle  与X轴偏移角度
	 * @return Y坐标
	 */
	private double getRoundY(float r, int i, int n, float offset_angle) {
		return r * Math.sin(i * 2 * Math.PI / n + Math.PI / 180 * offset_angle);
	}

	/**
	 * 计算圆形等分扇形的点X坐标
	 * @param r 圆形直径
	 * @param i 第几个等分扇形
	 * @param n  等分扇形个数
	 * @param offset_angle 与X轴偏移角度
	 * @return x坐标
	 */
	private double getRoundX(float r, int i, int n, float offset_angle) {
		return r * Math.cos(i * 2 * Math.PI / n + Math.PI / 180 * offset_angle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//判断点击是最小圆范围，则不触发点击效果
		if ((event.getX() > radius*2/3 && event.getX() < radius*4/3) && (event.getY() > radius*2/3 && event.getY() < radius*4/3)) {
//			Toast.makeText(mContext, "x="+event.getX()+"---y="+event.getY(), Toast.LENGTH_SHORT).show();
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			selectId = whichSector(event.getX()-centerX, event.getY()-centerY, radius);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			selectId = -1;
			invalidate();
			break;
		}
		gestureDetector.onTouchEvent(event);
		return true;
	}

	/**
	 * 计算两个坐标点与圆点之间的夹角
	 * @param px1 点1的x坐标
	 * @param py1 点1的y坐标
	 * @param px2 点2的x坐标
	 * @param py2 点2的y坐标
	 * @return 夹角度数
	 */
	private double calculateScrollAngle(float px1, float py1, float px2, float py2) {
		double radian1 = Math.atan2(py1, px1);
		double radian2 = Math.atan2(py2, px2);
		double diff = radian2 - radian1;
		return Math.round(diff / Math.PI * 180);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
//		Toast.makeText(mContext, ""+selectId, Toast.LENGTH_SHORT).show();
		//操作类型 1 上，2下，3左，4右，5右上，6左上，7右下，8左下，
		//10变倍大，11变倍小，13聚焦近，14聚焦远，17打开光圈，18关闭光圈，19打开雨刷，
		//20关闭雨刷，23巡航开始，24巡航关闭，30亮度，31色度，32对比度，33饱和度
		String orderType = null;
		if (selectId == 0) {//右下
			orderType = "7";
		}else if (selectId == 1) {//下
			orderType = "2";
		}else if (selectId == 2) {//左下
			orderType = "8";
		}else if (selectId == 3) {//左
			orderType = "3";
		}else if (selectId == 4) {//左上
			orderType = "6";
		}else if (selectId == 5) {//上
			orderType = "1";
		}else if (selectId == 6) {//右上
			orderType = "5";
		}else if (selectId == 7) {//右
			orderType = "4";
		}
		if (!TextUtils.isEmpty(orderType)) {
			Intent intent = new Intent();
			intent.setAction(CONST.CIRCLE_CONTROLER);
			intent.putExtra("orderType", orderType);//指令类型
			mContext.sendBroadcast(intent);
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//		float tpx = e2.getX() - 200;
//		float tpy = e2.getY() - 200;
//		float disx = (int) distanceX;
//		float disy = (int) distanceY;
//		double scrollAngle = calculateScrollAngle(tpx, tpy, tpx + disx, tpy + disy);
//		offsetAngle -= scrollAngle;
//		selectId = whichSector(0, 40, 200);// 0,40是中心三角定点相对于圆点的坐标
//		invalidate();
//		Log.e("CM", "offsetAngle:" + offsetAngle);
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 计算点在那个扇形区域
	 * 
	 * @param X
	 * @param Y
	 * @param R 半径
	 * @return
	 */
	private int whichSector(double X, double Y, double R) {
		double mod;
		mod = Math.sqrt(X * X + Y * Y); // 将点(X,Y)视为复平面上的点，与复数一一对应，现求复数的模。
		double offset_angle;
		double arg;
		arg = Math.round(Math.atan2(Y, X) / Math.PI * 180);// 求复数的辐角。
		arg = arg < 0 ? arg + 360 : arg;
		if (offsetAngle % 360 < 0) {
			offset_angle = 360 + offsetAngle % 360;
		} else {
			offset_angle = offsetAngle % 360;
		}
		if (mod > R) { // 如果复数的模大于预设的半径，则返回0。
			return -2;
		} else { // 根据复数的辐角来判别该点落在那个扇区。
			for (int i = 0; i < childMenuSize; i++) {
				if (isSelect(arg, i, offset_angle) || isSelect(360 + arg, i, offset_angle)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 判读该区域是否被选中
	 * @param arg 角度
	 * @param i
	 * @param offsetAngle 偏移角度
	 * @return 是否被选中
	 */
	private boolean isSelect(double arg, int i, double offsetAngle) {
		return arg > (i * childAngle + offsetAngle % 360) && arg < ((i + 1) * childAngle + offsetAngle % 360);
	}
	
}