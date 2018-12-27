package com.cxwl.weather.eye.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.WeatherDto;
import com.cxwl.weather.eye.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 逐小时温度曲线
 */
public class ShawnHourTempView extends View {

    private Context mContext;
    private List<WeatherDto> tempList = new ArrayList<>();
    private float maxValue = 0;
    private float minValue = 0;
    private Paint lineP = null;//画线画笔
    private Paint textP = null;//写字画笔
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH时", Locale.CHINA);
    private float totalDivider = 0;
    private float itemDivider = 1;

    public ShawnHourTempView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ShawnHourTempView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public ShawnHourTempView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    public void setData(List<WeatherDto> dataList) {
        if (!dataList.isEmpty()) {
            tempList.addAll(dataList);
            if (tempList.isEmpty()) {
                return;
            }

            maxValue = tempList.get(0).hourlyTemp;
            minValue = tempList.get(0).hourlyTemp;
            for (int i = 0; i < tempList.size(); i++) {
                WeatherDto dto = tempList.get(i);
                if (maxValue <= dto.hourlyTemp) {
                    maxValue = dto.hourlyTemp;
                }
                if (minValue >= dto.hourlyTemp) {
                    minValue = dto.hourlyTemp;
                }
            }

            if (maxValue == 0 && minValue == 0) {
                maxValue = 5;
                minValue = 0;
            } else {
                totalDivider = maxValue-minValue;
                if (totalDivider <= 5) {
                    itemDivider = 1;
                }else if (totalDivider <= 10) {
                    itemDivider = 2;
                }else if (totalDivider <= 15) {
                    itemDivider = 3;
                }else if (totalDivider <= 20) {
                    itemDivider = 4;
                }else {
                    itemDivider = 5;
                }

                maxValue = maxValue + itemDivider - (maxValue % itemDivider) + itemDivider/2;
                minValue = minValue - itemDivider - (minValue % itemDivider);
            }
            totalDivider = maxValue-minValue;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tempList.isEmpty()) {
            return;
        }

        canvas.drawColor(Color.TRANSPARENT);
        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float chartW = w - CommonUtil.dip2px(mContext, 30);
        float chartH = h - CommonUtil.dip2px(mContext, 30);
        float leftMargin = CommonUtil.dip2px(mContext, 20);
        float rightMargin = CommonUtil.dip2px(mContext, 10);
        float bottomMargin = CommonUtil.dip2px(mContext, 30);

        int size = tempList.size();
        float columnWidth = chartW / (size - 1);
        //获取曲线上每个温度点的坐标
        for (int i = 0; i < size; i++) {
            WeatherDto dto = tempList.get(i);
            dto.x = columnWidth * i + leftMargin;

            float value = dto.hourlyTemp;
            dto.y = chartH*(maxValue-value)/totalDivider;
            Log.e("temp", value+"---"+dto.y+"---"+chartH+"---"+h);
            tempList.set(i, dto);
        }

        //绘制刻度线
        for (int i = (int)minValue; i <= maxValue; i+=itemDivider) {
            float dividerY = chartH*(maxValue-i)/totalDivider;
            lineP.setColor(0x30999999);
            lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.2f));
            canvas.drawLine(leftMargin, dividerY, w - rightMargin, dividerY, lineP);
            textP.setColor(getResources().getColor(R.color.text_color1));
            textP.setTextSize(CommonUtil.dip2px(mContext, 10));
            canvas.drawText(String.valueOf(i), CommonUtil.dip2px(mContext, 5), dividerY, textP);
        }

        //绘制区域
        for (int i = 0; i < size; i++) {
            WeatherDto dto = tempList.get(i);

            if (i < size - 1) {
                Path rectPath = new Path();
                rectPath.moveTo(dto.x, dto.y);
                rectPath.lineTo(dto.x + columnWidth, tempList.get(i + 1).y);
                rectPath.lineTo(dto.x + columnWidth, h - bottomMargin);
                rectPath.lineTo(dto.x, h - bottomMargin);
                rectPath.close();
                lineP.setColor(0x90e73540);
                lineP.setStyle(Style.FILL_AND_STROKE);
                lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.2f));
                canvas.drawPath(rectPath, lineP);
            }

            //绘制白点
            lineP.setColor(Color.WHITE);
            lineP.setStyle(Style.FILL_AND_STROKE);
            lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
            canvas.drawPoint(dto.x, dto.y, lineP);

            //绘制曲线上每个点的数据值
            textP.setColor(Color.WHITE);
            textP.setTextSize(CommonUtil.dip2px(mContext, 10));
            float tempWidth = textP.measureText(dto.hourlyTemp + "");
            canvas.drawText(dto.hourlyTemp + "", dto.x - tempWidth / 2, dto.y - CommonUtil.dip2px(mContext, 5f), textP);
        }

        //绘制24小时
        textP.setColor(0xff999999);
        textP.setTextSize(CommonUtil.dip2px(mContext, 10));
        for (int i = 0; i < size; i+=2) {
            WeatherDto dto = tempList.get(i);
            try {
                if (!TextUtils.isEmpty(dto.hourlyTime)) {
                    String time = sdf2.format(sdf1.parse(dto.hourlyTime));
                    if (!TextUtils.isEmpty(time)) {
                        float text = textP.measureText(time);
                        canvas.drawText(time, dto.x - text / 2, h - CommonUtil.dip2px(mContext, 10f), textP);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

}
