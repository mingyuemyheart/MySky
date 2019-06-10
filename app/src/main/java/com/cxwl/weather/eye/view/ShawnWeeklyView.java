package com.cxwl.weather.eye.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.WeatherDto;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 一周预报曲线图
 */
public class ShawnWeeklyView extends View{
	
	private Context mContext;
	private List<WeatherDto> tempList = new ArrayList<>();
	private int maxTemp,minTemp;//最高温度
	private Paint lineP,textP;//画线画笔
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.CANADA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd", Locale.CANADA);
	private int totalDivider = 0, itemDivider = 1;

	public ShawnWeeklyView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public ShawnWeeklyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public ShawnWeeklyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
		
	}
	
	/**
	 * 进行赋值
	 */
	public void setData(List<WeatherDto> dataList) {
		if (!dataList.isEmpty()) {
			tempList.addAll(dataList);
			
			maxTemp = tempList.get(0).highTemp;
			minTemp = tempList.get(0).lowTemp;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxTemp <= tempList.get(i).highTemp) {
					maxTemp = tempList.get(i).highTemp;
				}
				if (minTemp >= tempList.get(i).lowTemp) {
					minTemp = tempList.get(i).lowTemp;
				}
			}

			totalDivider = maxTemp-minTemp;
			if (totalDivider <= 5) {
				itemDivider = 1;
			}else if (totalDivider <= 15) {
				itemDivider = 2;
			}else if (totalDivider <= 25) {
				itemDivider = 3;
			}else if (totalDivider <= 40) {
				itemDivider = 4;
			}else {
				itemDivider = 5;
			}
//			maxTemp = maxTemp+itemDivider*3/2;
//			minTemp = minTemp-itemDivider;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 280);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 160);

		int size = tempList.size();
		
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);

			//获取最高温度对应的坐标点信息
			dto.highX = (chartW/(size-1))*i + leftMargin;
			float highTemp = tempList.get(i).highTemp;
			dto.highY = chartH*Math.abs(maxTemp-highTemp)/totalDivider+topMargin;
			Log.e("highTemp", highTemp+"---"+dto.highY);

			//获取最低温度的对应的坐标点信息
			dto.lowX = (chartW/(size-1))*i + leftMargin;
			float lowTemp = tempList.get(i).lowTemp;
			dto.lowY = chartH*Math.abs(maxTemp-lowTemp)/totalDivider+topMargin;
			Log.e("lowTemp", lowTemp+"---"+dto.lowY);
			
			tempList.set(i, dto);
		}

		//绘制最低温度、最高温度曲线
		for (int i = 0; i < size-1; i++) {
			float x1 = tempList.get(i).lowX;
			float y1 = tempList.get(i).lowY;
			float x2 = tempList.get(i+1).lowX;
			float y2 = tempList.get(i+1).lowY;
			float wt = (x1 + x2) / 2;
			float x3 = wt;
			float y3 = y1;
			float x4 = wt;
			float y4 = y2;
			Path pathLow = new Path();
			pathLow.moveTo(x1, y1);
			pathLow.cubicTo(x3, y3, x4, y4, x2, y2);
			lineP.setColor(0xff2C84D3);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 2f));
			canvas.drawPath(pathLow, lineP);

			x1 = tempList.get(i).highX;
			y1 = tempList.get(i).highY;
			x2 = tempList.get(i+1).highX;
			y2 = tempList.get(i+1).highY;
			wt = (x1 + x2) / 2;
			x3 = wt;
			y3 = y1;
			x4 = wt;
			y4 = y2;
			Path pathHigh = new Path();
			pathHigh.moveTo(x1, y1);
			pathHigh.cubicTo(x3, y3, x4, y4, x2, y2);
			lineP.setColor(0xffE34606);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 2f));
			canvas.drawPath(pathHigh, lineP);
		}


		for (int i = 0; i < tempList.size(); i++) {
			WeatherDto dto = tempList.get(i);

			textP.setColor(getResources().getColor(R.color.text_color4));
			textP.setTextSize(getResources().getDimension(R.dimen.level_5));

			//绘制周几、日期、天气现象和天气现象图标
			float weekText = textP.measureText(dto.week);
			canvas.drawText(dto.week, dto.highX-weekText/2, CommonUtil.dip2px(mContext, 20), textP);

			try {
				String date = sdf2.format(sdf1.parse(dto.date));
				float dateText = textP.measureText(date);
				canvas.drawText(date, dto.highX-dateText/2, CommonUtil.dip2px(mContext, 35), textP);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			//绘制高温及天气现象
			float highPheText = textP.measureText(dto.highPhe);//天气现象字符串占像素宽度
			canvas.drawText(dto.highPhe, dto.highX-highPheText/2, CommonUtil.dip2px(mContext, 60), textP);
			Bitmap b = WeatherUtil.getDayBitmap(mContext, dto.highPheCode);
			Bitmap newBit = ThumbnailUtils.extractThumbnail(b, (int)(CommonUtil.dip2px(mContext, 18)), (int)(CommonUtil.dip2px(mContext, 18)));
			canvas.drawBitmap(newBit, dto.highX-newBit.getWidth()/2, CommonUtil.dip2px(mContext, 65), textP);
			String highWindDir = mContext.getString(WeatherUtil.getWindDirection(dto.highWindDir));
			String highWindForce = WeatherUtil.getDayWindForce(dto.highWindForce);
			float highWindDirWidth = textP.measureText(highWindDir);
			float highWindForceWidth = textP.measureText(highWindForce);
			canvas.drawText(highWindDir, dto.highX-highWindDirWidth/2, CommonUtil.dip2px(mContext, 100), textP);
			canvas.drawText(highWindForce, dto.highX-highWindForceWidth/2, CommonUtil.dip2px(mContext, 115), textP);

			//绘制低温及天气现象
			Bitmap lb = WeatherUtil.getNightBitmap(mContext, dto.lowPheCode);
			Bitmap newLbit = ThumbnailUtils.extractThumbnail(lb, (int)(CommonUtil.dip2px(mContext, 18)), (int)(CommonUtil.dip2px(mContext, 18)));
			canvas.drawBitmap(newLbit, dto.lowX-newLbit.getWidth()/2, h-CommonUtil.dip2px(mContext, 80), textP);
			float lowPheText = textP.measureText(dto.lowPhe);//天气现象字符串占像素宽度
			canvas.drawText(dto.lowPhe, dto.lowX-lowPheText/2, h-CommonUtil.dip2px(mContext, 45), textP);
			String lowWindDir = mContext.getString(WeatherUtil.getWindDirection(dto.lowWindDir));
			String lowWindForce = WeatherUtil.getDayWindForce(dto.lowWindForce);
			float lowWindDirWidth = textP.measureText(lowWindDir);
			float lowWindForceWidth = textP.measureText(lowWindForce);
			canvas.drawText(lowWindDir, dto.lowX-lowWindDirWidth/2, h-CommonUtil.dip2px(mContext, 25), textP);
			canvas.drawText(lowWindForce, dto.lowX-lowWindForceWidth/2, h-CommonUtil.dip2px(mContext, 10), textP);

			//绘制曲线上每个时间点上的圆点marker
			if (i != 0) {
				lineP.setColor(0xff2C84D3);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
				canvas.drawPoint(dto.lowX, dto.lowY, lineP);
				lineP.setColor(Color.WHITE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
				canvas.drawPoint(dto.lowX, dto.lowY, lineP);
				lineP.setColor(0xffE34606);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
				canvas.drawPoint(dto.highX, dto.highY, lineP);
				lineP.setColor(Color.WHITE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
				canvas.drawPoint(dto.highX, dto.highY, lineP);
			}else {
				lineP.setColor(0xff2C84D3);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
				canvas.drawPoint(dto.lowX, dto.lowY, lineP);
				lineP.setColor(0xffE34606);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
				canvas.drawPoint(dto.highX, dto.highY, lineP);
			}

			//绘制曲线上每个时间点的温度值
			textP.setColor(Color.WHITE);
			float highText = textP.measureText(String.valueOf(tempList.get(i).highTemp)+"℃");//高温字符串占像素宽度
			canvas.drawText(String.valueOf(tempList.get(i).highTemp)+"℃", dto.highX-highText/2, dto.highY-CommonUtil.dip2px(mContext, 10), textP);

			float lowText = textP.measureText(String.valueOf(tempList.get(i).lowTemp)+"℃");//低温字符串所占的像素宽度
			canvas.drawText(String.valueOf(tempList.get(i).lowTemp)+"℃", dto.lowX-lowText/2, dto.lowY+CommonUtil.dip2px(mContext, 20), textP);
		}

	}
	
}
