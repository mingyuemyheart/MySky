package com.cxwl.weather.eye.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CommonUtil;

/**
 * 湿度曲线
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class HumidityView extends View{
	
	private Context mContext = null;
	private List<EyeDto> tempList = new ArrayList<EyeDto>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	
	public HumidityView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public HumidityView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public HumidityView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<EyeDto> dataList) {
		if (!dataList.isEmpty()) {
			for (int i = 0; i < dataList.size(); i++) {
				tempList.add(dataList.get(i));
			}
			if (tempList.isEmpty()) {
				return;
			}
			
			maxValue = 100;
			minValue = 0;
		}
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}
		
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 50);
		float chartH = h-CommonUtil.dip2px(mContext, 50);
		float leftMargin = CommonUtil.dip2px(mContext, 30);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 25);
		float bottomMargin = CommonUtil.dip2px(mContext, 25);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度
		
		int size = tempList.size();
		float columnWidth = chartW/(size-1);
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			EyeDto dto = tempList.get(i);
			dto.x = columnWidth*i+leftMargin;
			dto.y = 0;
			
			float value = dto.humidity;
			if (value >= 0) {
				dto.y = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (minValue >= 0) {
					dto.y = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}else {
				dto.y = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (maxValue < 0) {
					dto.y = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}
			tempList.set(i, dto);
		}
		
		//绘制刻度线，每间隔为20
		int totalDivider = (int) (Math.ceil(Math.abs(maxValue))+Math.floor(Math.abs(minValue)));
		int itemDivider = 20;
		for (int i = (int) minValue; i <= totalDivider; i+=itemDivider) {
			float dividerY = 0;
			int value = i;
			if (value >= 0) {
				dividerY = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (minValue >= 0) {
					dividerY = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}else {
				dividerY = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (maxValue < 0) {
					dividerY = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}
			lineP.setColor(0xff999999);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.2f));
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			textP.setColor(getResources().getColor(R.color.text_color1));
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			canvas.drawText(String.valueOf(i), CommonUtil.dip2px(mContext, 5), dividerY, textP);
		}
		
		//绘制区域
		for (int i = 0; i < size; i++) {
			EyeDto dto = tempList.get(i);
			
			if (i < size-1) {
				Path rectPath = new Path();
				rectPath.moveTo(dto.x, dto.y);
				rectPath.lineTo(dto.x+columnWidth, tempList.get(i+1).y);
				rectPath.lineTo(dto.x+columnWidth, h-bottomMargin);
				rectPath.lineTo(dto.x, h-bottomMargin);
				rectPath.close();
				lineP.setColor(0xffe73540);
				lineP.setStyle(Style.FILL_AND_STROKE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
				canvas.drawPath(rectPath, lineP);
			}
			
			//绘制纵向线
			lineP.setColor(0xc0ffffff);
			lineP.setStyle(Style.STROKE);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.2f));
			canvas.drawLine(dto.x, dto.y, dto.x, h-bottomMargin, lineP);
			
			//绘制白点
			lineP.setColor(Color.WHITE);
			lineP.setStyle(Style.FILL_AND_STROKE);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 10));
			canvas.drawPoint(dto.x, dto.y, lineP);
			
			//绘制曲线上每个点的数据值
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			float tempWidth = textP.measureText((int)(dto.humidity)+"");
			canvas.drawText((int)(dto.humidity)+"", dto.x-tempWidth/2, dto.y-CommonUtil.dip2px(mContext, 10f), textP);

			//绘制24小时
			textP.setColor(0xff999999);
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			try {
				if (!TextUtils.isEmpty(dto.time)) {
					String time = sdf2.format(sdf1.parse(dto.time));
					if (!TextUtils.isEmpty(time)) {
						float text = textP.measureText(time+"时");
						textP.setColor(0xff999999);
						textP.setTextSize(CommonUtil.dip2px(mContext, 10));
						canvas.drawText(time+"时", dto.x-text/2, h-CommonUtil.dip2px(mContext, 10f), textP);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
//		lineP.reset();
//		textP.reset();
	}

}
