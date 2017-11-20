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

public class ReadParamentersFragment4 extends Fragment {
	
	private TextView tvNetwork1, tvNetwork2, tvNetwork3, tvNetwork4, tvNetwork5, tvNetwork6, tvNetwork7;//网络管理
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_read_parameters4, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		tvNetwork1 = (TextView) view.findViewById(R.id.tvNetwork1);
		tvNetwork2 = (TextView) view.findViewById(R.id.tvNetwork2);
		tvNetwork3 = (TextView) view.findViewById(R.id.tvNetwork3);
		tvNetwork4 = (TextView) view.findViewById(R.id.tvNetwork4);
		tvNetwork5 = (TextView) view.findViewById(R.id.tvNetwork5);
		tvNetwork6 = (TextView) view.findViewById(R.id.tvNetwork6);
		tvNetwork7 = (TextView) view.findViewById(R.id.tvNetwork7);
		
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
								//网络管理
								if (!obj.isNull("pictureFtpAddress")) {
									tvNetwork1.setText(obj.getString("pictureFtpAddress"));
								}
								if (!obj.isNull("pictureFtpPort")) {
									tvNetwork2.setText(obj.getInt("pictureFtpPort")+"");
								}
								if (!obj.isNull("pictureFtpUser")) {
									tvNetwork3.setText(obj.getString("pictureFtpUser"));
								}
								if (!obj.isNull("pictureFtpPassword")) {
									tvNetwork4.setText(obj.getString("pictureFtpPassword"));
								}
								if (!obj.isNull("ntpAddress")) {
									tvNetwork5.setText(obj.getString("ntpAddress"));
								}
								if (!obj.isNull("ntpPort")) {
									tvNetwork6.setText(obj.getInt("ntpPort")+"");
								}
								if (!obj.isNull("ntpFrequency")) {
									tvNetwork7.setText(obj.getInt("ntpFrequency")+"");
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
