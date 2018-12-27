package com.cxwl.weather.eye.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 旋转背景线
 * @author shawn_sun
 */
public class ShawnRotateLineView2 extends View{

	private Context mContext;
	private Paint lineP,textP;//画线画笔
	private String month,day;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("dd", Locale.CHINA);

	public ShawnRotateLineView2(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public ShawnRotateLineView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public ShawnRotateLineView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		lineP.setColor(0x80000000);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));

		textP = new Paint();
		textP.setAntiAlias(true);
		textP.setColor(Color.WHITE);
		textP.setTextSize(getResources().getDimension(R.dimen.level_6));
//		textP.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

	}

	public void setData(String time) {
		if (!TextUtils.isEmpty(time)) {
			try {
				month = sdf2.format(sdf1.parse(time));
				day = sdf3.format(sdf1.parse(time));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();

		//绘制三角形
		lineP.setColor(0x80000000);
		lineP.setStyle(Style.FILL_AND_STROKE);
		Path path = new Path();
		path.moveTo(w, h);
		path.lineTo(0, h);
		path.lineTo(w, 0);
		path.close();
		canvas.drawPath(path, lineP);

		//绘制日期
		lineP.setColor(Color.WHITE);
		lineP.setStyle(Style.STROKE);
		canvas.drawLine(w/2+15, h-15, w-15, h/2+15, lineP);
		float monthWidth = textP.measureText(month);
		canvas.drawText(month, w/8*7-monthWidth/2, h/8*7, textP);
		float dayWidth = textP.measureText(month);
		canvas.drawText(day, w/8*5-dayWidth/2, h/8*5+dayWidth/2, textP);

	}

}
