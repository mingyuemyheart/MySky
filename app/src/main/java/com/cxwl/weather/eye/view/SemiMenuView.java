package com.cxwl.weather.eye.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.utils.CommonUtil;

public class SemiMenuView extends ImageView implements OnGestureListener {
	
	private Context mContext;
	private static final int childMenuSize = 30;
	private static final float childAngle = 180f / childMenuSize;
	private float offsetAngle = 0f;
	private Paint paint;
	private RectF rectF;
	private GestureDetector gestureDetector;
	private int selectId = -1;
	private int radius = 270;//半径默认为270,单位px
	private int centerX = 270, centerY = 270;//默认半径，单位px
	private final int HANDLER_PLAYING = 1001;
	private final int HANDLER_CANCEL = 1002;
	private int index = 0;
	private DrawThread drawThread = null;
	private boolean startDraw = false;//开始绘制
	private FinishListener finishListener = null;
	private Bitmap bitmap1, bitmap2, bitmap3, bitmap4, bitmap5;

	public SemiMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {
		paint = new Paint();
		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 50));
		paint.setColor(0xffed4046);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		
		gestureDetector = new GestureDetector(this);
		
		bitmap1 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.eye_btn_setting1), 
				(int)(CommonUtil.dip2px(mContext, 24)), (int)(CommonUtil.dip2px(mContext, 24)));
		bitmap2 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.eye_btn_setting2), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
		bitmap3 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.eye_btn_setting3), 
				(int)(CommonUtil.dip2px(mContext, 22)), (int)(CommonUtil.dip2px(mContext, 22)));
		bitmap4 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.eye_btn_setting4), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
		bitmap5 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.eye_btn_setting5), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
	}
	
	/**
	 * 开始绘制
	 * @param startDraw true为打开menu，false为关闭menu
	 */
	public void boostThread(boolean startDraw) {
		this.startDraw = startDraw;
		if (drawThread == null) {
			drawThread = new DrawThread();
			drawThread.start();
		}
	}
	
	public interface FinishListener {
		void loadComplete(boolean startDraw);
	}
	
	public FinishListener getFinishListener() {
		return finishListener;
	}

	public void setFinishListener(FinishListener finishListener) {
		this.finishListener = finishListener;
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
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (rectF == null) {
			rectF = new RectF(CommonUtil.dip2px(mContext, 25), CommonUtil.dip2px(mContext, 25), 
					canvas.getWidth()-CommonUtil.dip2px(mContext, 25), canvas.getHeight()-CommonUtil.dip2px(mContext, 25));
		}
		paint.setStrokeWidth(CommonUtil.dip2px(mContext, 50));
		paint.setColor(0xffed4046);
		if (startDraw) {
			canvas.drawArc(rectF, 180, -(index * childAngle + offsetAngle), false, paint);
		}else {
			canvas.drawArc(rectF, 180, -((childMenuSize-index) * childAngle + offsetAngle), false, paint);
		}
		
		if (index == childMenuSize && startDraw) {
//			if (TextUtils.equals(CONST.AUTHORITY, CONST.COMMON)) {//普通用户
//				//设置
//				double x3 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 3, childMenuSize, offsetAngle + childAngle / 2);
//				double y3 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 3, childMenuSize, offsetAngle + childAngle / 2);
//				canvas.drawBitmap(bitmap1, (float)x3-bitmap1.getWidth()/2, (float)y3-bitmap1.getHeight()/2, paint);
//				
//				//分享
//				double x6 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 6, childMenuSize, offsetAngle + childAngle / 2);
//				double y6 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 6, childMenuSize, offsetAngle + childAngle / 2);
//				canvas.drawBitmap(bitmap2, (float)x6, (float)y6-bitmap2.getHeight()/2, paint);
//				
//				//关于
//				double x9 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 9, childMenuSize, offsetAngle + childAngle / 2);
//				double y9 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 9, childMenuSize, offsetAngle + childAngle / 2);
//				canvas.drawBitmap(bitmap3, (float)x9, (float)y9-bitmap3.getHeight()/3, paint);
//				
//				//个人
//				double x12 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 12, childMenuSize, offsetAngle + childAngle / 2);
//				double y12 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 12, childMenuSize, offsetAngle + childAngle / 2);
//				canvas.drawBitmap(bitmap4, (float)x12, (float)y12, paint);
//			}else {
				//设置
				double x3 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 3, childMenuSize, offsetAngle + childAngle / 2);
				double y3 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 3, childMenuSize, offsetAngle + childAngle / 2);
				canvas.drawBitmap(bitmap1, (float)x3-bitmap1.getWidth()/2, (float)y3-bitmap1.getHeight()/2, paint);
				
				//分享
				double x5 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 5, childMenuSize, offsetAngle + childAngle / 2);
				double y5 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 5, childMenuSize, offsetAngle + childAngle / 2);
				canvas.drawBitmap(bitmap2, (float)x5-bitmap2.getWidth()/2, (float)y5-bitmap2.getHeight()/2, paint);
				
				//关于
				double x7 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 7, childMenuSize, offsetAngle + childAngle / 2);
				double y7 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 7, childMenuSize, offsetAngle + childAngle / 2);
				double x8 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 8, childMenuSize, offsetAngle + childAngle / 2);
				double y8 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 8, childMenuSize, offsetAngle + childAngle / 2);
				canvas.drawBitmap(bitmap3, (float)(x7+x8)/2, (float)(y7+y8)/2-bitmap3.getHeight()/2, paint);
				
				//个人
				double x10 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 10, childMenuSize, offsetAngle + childAngle / 2);
				double y10 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 10, childMenuSize, offsetAngle + childAngle / 2);
				canvas.drawBitmap(bitmap4, (float)x10, (float)y10-bitmap4.getHeight()/3, paint);
				
				//设备
				double x12 = centerX + getRoundX(radius-CommonUtil.dip2px(mContext, 25), 12, childMenuSize, offsetAngle + childAngle / 2);
				double y12 = centerY + getRoundY(radius-CommonUtil.dip2px(mContext, 25), 12, childMenuSize, offsetAngle + childAngle / 2);
				canvas.drawBitmap(bitmap5, (float)x12, (float)y12, paint);
//			}
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
		if (selectId != -1) {
			Intent intent = new Intent();
			intent.setAction(CONST.SIME_CIRCLE_CONTROLER);
			intent.putExtra("selectId", selectId);//选中扇形id
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
	
	private class DrawThread extends Thread {
		static final int STATE_NONE = 1;
		static final int STATE_PLAYING = 1;
		static final int STATE_CANCEL = 3;
		private int state;
		private int index;
		private int count;
		
		private DrawThread() {
			this.count = childMenuSize;
			this.index = 0;
			this.state = STATE_NONE;
		}
		
		@Override
		public void run() {
			super.run();
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				startDraw();
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void startDraw() {
			if (index >= count || index < 0) {
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_CANCEL;
				message.arg1 = index;
				mHandler.sendMessage(message);
				index = 0;
			}else {
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_PLAYING;
				message.arg1 = index++;
				mHandler.sendMessage(message);
			}
		}
		
		private void cancel() {
			this.state = STATE_CANCEL;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}
		
		public void setCurrent(int index) {
			this.index = index;
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_PLAYING: 
				index = msg.arg1;
				postInvalidate();
				break;
			case HANDLER_CANCEL:
				index = msg.arg1;
				postInvalidate();
				clearThread();
				if (finishListener != null) {
					finishListener.loadComplete(startDraw);
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	private void clearThread() {
		if (drawThread != null) {
			drawThread.cancel();
			drawThread = null;
		}
	}
	
}