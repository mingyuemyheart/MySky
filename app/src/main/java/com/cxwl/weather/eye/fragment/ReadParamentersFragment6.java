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

public class ReadParamentersFragment6 extends Fragment {
	
	private TextView tvNot1;//未定
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_read_parameters6, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		tvNot1 = (TextView) view.findViewById(R.id.tvNot1);
		
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
								//未定
								if (!obj.isNull("pushAddress")) {
									tvNot1.setText(obj.getString("pushAddress"));
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
