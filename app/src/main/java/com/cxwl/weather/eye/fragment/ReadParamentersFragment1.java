package com.cxwl.weather.eye.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;

/**
 * 摄像头参数展示
 */
public class ReadParamentersFragment1 extends Fragment {
	
	private TextView tvVideo1, tvVideo2, tvVideo3, tvVideo4, tvVideo5, tvVideo6, tvVideo7, tvVideo8;//音视频参数
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_read_parameters1, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		tvVideo1 = (TextView) view.findViewById(R.id.tvVideo1);
		tvVideo2 = (TextView) view.findViewById(R.id.tvVideo2);
		tvVideo3 = (TextView) view.findViewById(R.id.tvVideo3);
		tvVideo4 = (TextView) view.findViewById(R.id.tvVideo4);
		tvVideo5 = (TextView) view.findViewById(R.id.tvVideo5);
		tvVideo6 = (TextView) view.findViewById(R.id.tvVideo6);
		tvVideo7 = (TextView) view.findViewById(R.id.tvVideo7);
		tvVideo8 = (TextView) view.findViewById(R.id.tvVideo8);
		
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
								//音视频参数
								if (!obj.isNull("videoType")) {
									tvVideo1.setText(obj.getString("videoType"));
								}
								if (!obj.isNull("videoResolution")) {
									tvVideo2.setText(obj.getString("videoResolution"));
								}
								if (!obj.isNull("videoBitRate")) {
									tvVideo3.setText(obj.getInt("videoBitRate")+"");
								}
								if (!obj.isNull("videoFrameRate")) {
									tvVideo4.setText(obj.getInt("videoFrameRate")+"");
								}
								if (!obj.isNull("videoIFrameRate")) {
									tvVideo5.setText(obj.getInt("videoIFrameRate")+"");
								}
								if (!obj.isNull("videoSmooth")) {
									tvVideo6.setText(obj.getInt("videoSmooth")+"");
								}
								if (!obj.isNull("videoCoding")) {
									tvVideo7.setText(obj.getString("videoCoding"));
								}
								if (!obj.isNull("videoExtendedCoding")) {
									tvVideo8.setText(obj.getString("videoExtendedCoding"));
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
