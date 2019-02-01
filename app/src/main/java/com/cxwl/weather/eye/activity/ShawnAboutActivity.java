package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 关于我们
 */
public class ShawnAboutActivity extends ShawnBaseActivity implements OnClickListener{

	private Context context;
	private LinearLayout llContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_about);
		context = this;
		initWidget();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("关于我们");
		llContainer = findViewById(R.id.llContainer);

		OkHttpContent();
	}

	/**
	 * 获取关于我们内容
	 */
	private void OkHttpContent() {
		final String url = "http://decision-admin.tianqi.cn/home/api/tqwyAboutContent";
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										llContainer.removeAllViews();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											LinearLayout ll = new LinearLayout(context);
											ll.setOrientation(LinearLayout.VERTICAL);
											JSONObject obj = array.getJSONObject(i);

											String name = obj.getString("name");
											TextView tvName = new TextView(context);
											tvName.setText(name);
											tvName.setTextColor(getResources().getColor(R.color.white));
											tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
											tvName.setLineSpacing((int) CommonUtil.dip2px(context, 3), 1);
											LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
											params1.setMargins(0, (int) CommonUtil.dip2px(context, 15), 0, (int) CommonUtil.dip2px(context, 2));
											tvName.setLayoutParams(params1);
											ll.addView(tvName);

											String content = obj.getString("content");
											TextView tvContent = new TextView(context);
											tvContent.setText(content);
											tvContent.setTextColor(getResources().getColor(R.color.text_color1));
											tvContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
											tvContent.setLineSpacing((int) CommonUtil.dip2px(context, 3), 1);
											ll.addView(tvContent);

											llContainer.addView(ll);
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
