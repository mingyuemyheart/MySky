package com.cxwl.weather.eye.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;

/**
 * 摄像头参数展示
 * @author shawn_sun
 *
 */

public class ReadParamentersFragment5 extends Fragment {
	
	private TextView tvWeather1, tvWeather2, tvWeather3, tvWeather4, tvWeather5, tvWeather6, tvWeather7, tvWeather8, tvWeather9;//天气模块
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_read_parameters5, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		tvWeather1 = (TextView) view.findViewById(R.id.tvWeather1);
		tvWeather2 = (TextView) view.findViewById(R.id.tvWeather2);
		tvWeather3 = (TextView) view.findViewById(R.id.tvWeather3);
		tvWeather4 = (TextView) view.findViewById(R.id.tvWeather4);
		tvWeather5 = (TextView) view.findViewById(R.id.tvWeather5);
		tvWeather6 = (TextView) view.findViewById(R.id.tvWeather6);
		tvWeather7 = (TextView) view.findViewById(R.id.tvWeather7);
		tvWeather8 = (TextView) view.findViewById(R.id.tvWeather8);
		tvWeather9 = (TextView) view.findViewById(R.id.tvWeather9);
		
		String result = getArguments().getString("result");
		parseResult(result);
	}
	
	/**
	 * 解析数据并展示
	 * @param requestResult
	 */
	private void parseResult(String requestResult) {
		if (requestResult != null) {
			try {
				JSONObject object = new JSONObject(requestResult);
				if (object != null) {
					if (!object.isNull("code")) {
						String code  = object.getString("code");
						if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
							if (!object.isNull("list")) {
								JSONObject obj = new JSONObject(object.getString("list"));
								//天气模块
								if (!obj.isNull("weather1")) {
									tvWeather1.setText(obj.getInt("weather1")+"");
								}
								if (!obj.isNull("weather2")) {
									tvWeather2.setText(obj.getInt("weather2")+"");
								}
								if (!obj.isNull("weather3")) {
									tvWeather3.setText(obj.getInt("weather3")+"");
								}
								if (!obj.isNull("weather4")) {
									tvWeather4.setText(obj.getInt("weather4")+"");
								}
								if (!obj.isNull("weatherFrequency")) {
									tvWeather5.setText(obj.getInt("weatherFrequency")+"");
								}
								if (!obj.isNull("weatherFtpAddress")) {
									tvWeather6.setText(obj.getString("weatherFtpAddress"));
								}
								if (!obj.isNull("weatherFtpPort")) {
									tvWeather7.setText(obj.getInt("weatherFtpPort")+"");
								}
								if (!obj.isNull("weatherFtpUser")) {
									tvWeather8.setText(obj.getString("weatherFtpUser"));
								}
								if (!obj.isNull("weatherFtpPassword")) {
									tvWeather9.setText(obj.getString("weatherFtpPassword"));
								}
								
							}
						}else {
							//失败
							if (!object.isNull("reason")) {
								String reason = object.getString("reason");
								if (!TextUtils.isEmpty(reason)) {
									Toast.makeText(getActivity(), reason, Toast.LENGTH_SHORT).show();
								}
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
}
