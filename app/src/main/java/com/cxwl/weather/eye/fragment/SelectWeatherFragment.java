package com.cxwl.weather.eye.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.view.HumidityView;
import com.cxwl.weather.eye.view.PressureView;
import com.cxwl.weather.eye.view.QualityView;
import com.cxwl.weather.eye.view.RainLevelView;
import com.cxwl.weather.eye.view.RainView;
import com.cxwl.weather.eye.view.TemperatureView;
import com.cxwl.weather.eye.view.UltravioletView;
import com.cxwl.weather.eye.view.WindView;

/**
 * 数据采集
 */

public class SelectWeatherFragment extends Fragment{
	
	private List<EyeDto> weatherList = new ArrayList<>();
	private LinearLayout llContainer = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_select_weather, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		llContainer = (LinearLayout) view.findViewById(R.id.llContainer);
		
		int index = getArguments().getInt("index");
		weatherList.clear();
		weatherList.addAll(getArguments().<EyeDto>getParcelableArrayList("weatherList"));
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		
		if (index == 0) {//温度
			TemperatureView temperatureView = new TemperatureView(getActivity());
			temperatureView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(temperatureView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}else if (index == 1) {//湿度
			HumidityView humidityView = new HumidityView(getActivity());
			humidityView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(humidityView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}else if (index == 2) {//降水
			RainView rainView = new RainView(getActivity());
			rainView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(rainView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}else if (index == 3) {//降水级别
			RainLevelView rainLevelView = new RainLevelView(getActivity());
			rainLevelView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(rainLevelView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}else if (index == 4) {//空气质量
			QualityView qualityView = new QualityView(getActivity());
			qualityView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(qualityView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}else if (index == 5) {//风向风速
			WindView windView = new WindView(getActivity());
			windView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(windView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}else if (index == 6) {//气压
			PressureView pressureView = new PressureView(getActivity());
			pressureView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(pressureView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}else if (index == 7) {//紫外线
			UltravioletView ultravioletView = new UltravioletView(getActivity());
			ultravioletView.setData(weatherList);
			llContainer.removeAllViews();
			llContainer.addView(ultravioletView, width*2, LinearLayout.LayoutParams.MATCH_PARENT);
		}
	}
	
}
