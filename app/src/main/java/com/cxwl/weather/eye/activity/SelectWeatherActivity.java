package com.cxwl.weather.eye.activity;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.MyPagerAdapter;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.fragment.SelectWeatherFragment;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.MainViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 数据采集
 */
public class SelectWeatherActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private TextView tvTitle = null;
	private LinearLayout llBack = null;
	private ImageView ivBack = null;
	private EyeDto data = null;
	private TextView tvTemp, tvHumidity, tvRain, tvRainLevel, tvQuality, tvWindSpeed, tvWindDir, tvPressure, tvUltraviolet;
	private List<EyeDto> weatherList = new ArrayList<>();
	private TextView tvBar1, tvBar2, tvBar3, tvBar4, tvBar5, tvBar6, tvBar7, tvBar8;
	private MainViewPager viewPager = null;
	private MyPagerAdapter pagerAdapter = null;
	private List<Fragment> fragments = new ArrayList<>();
	private LinearLayout llContent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_weather);
		mContext = this;
		showDialog();
		initWidget();
	}
	
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("数据采集");
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.eye_btn_close);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvRain = (TextView) findViewById(R.id.tvRain);
		tvRainLevel = (TextView) findViewById(R.id.tvRainLevel);
		tvQuality = (TextView) findViewById(R.id.tvQuality);
		tvWindSpeed = (TextView) findViewById(R.id.tvWindSpeed);
		tvWindDir = (TextView) findViewById(R.id.tvWindDir);
		tvPressure = (TextView) findViewById(R.id.tvPressure);
		tvUltraviolet = (TextView) findViewById(R.id.tvUltraviolet);
		tvBar1 = (TextView) findViewById(R.id.tvBar1);
		tvBar1.setOnClickListener(new MyOnClickListener(0));
		tvBar2 = (TextView) findViewById(R.id.tvBar2);
		tvBar2.setOnClickListener(new MyOnClickListener(1));
		tvBar3 = (TextView) findViewById(R.id.tvBar3);
		tvBar3.setOnClickListener(new MyOnClickListener(2));
		tvBar4 = (TextView) findViewById(R.id.tvBar4);
		tvBar4.setOnClickListener(new MyOnClickListener(3));
		tvBar5 = (TextView) findViewById(R.id.tvBar5);
		tvBar5.setOnClickListener(new MyOnClickListener(4));
		tvBar6 = (TextView) findViewById(R.id.tvBar6);
		tvBar6.setOnClickListener(new MyOnClickListener(5));
		tvBar7 = (TextView) findViewById(R.id.tvBar7);
		tvBar7.setOnClickListener(new MyOnClickListener(6));
		tvBar8 = (TextView) findViewById(R.id.tvBar8);
		tvBar8.setOnClickListener(new MyOnClickListener(7));
		llContent = (LinearLayout) findViewById(R.id.llContent);
		
		data = getIntent().getExtras().getParcelable("data");
		if (!TextUtils.isEmpty(data.fNumber)) {
			OkHttpWeather("https://tqwy.tianqi.cn/tianqixy/userInfo/tqys", data.fNumber);
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager(List<EyeDto> weatherList) {
		for (int i = 0; i < 8; i++) {
			Fragment fragment = new SelectWeatherFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("index", i);
			bundle.putParcelableArrayList("weatherList", (ArrayList<? extends Parcelable>) weatherList);
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}
			
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		pagerAdapter = new MyPagerAdapter(SelectWeatherActivity.this, fragments);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				viewPager.setCurrentItem(arg0, false);
				if (arg0 == 0) {
					tvBar1.setTextColor(0xffea3143);
					tvBar2.setTextColor(Color.WHITE);
					tvBar3.setTextColor(Color.WHITE);
					tvBar4.setTextColor(Color.WHITE);
					tvBar5.setTextColor(Color.WHITE);
					tvBar6.setTextColor(Color.WHITE);
					tvBar7.setTextColor(Color.WHITE);
					tvBar8.setTextColor(Color.WHITE);
				}else if (arg0 == 1) {
					tvBar1.setTextColor(Color.WHITE);
					tvBar2.setTextColor(0xffea3143);
					tvBar3.setTextColor(Color.WHITE);
					tvBar4.setTextColor(Color.WHITE);
					tvBar5.setTextColor(Color.WHITE);
					tvBar6.setTextColor(Color.WHITE);
					tvBar7.setTextColor(Color.WHITE);
					tvBar8.setTextColor(Color.WHITE);
				}else if (arg0 == 2) {
					tvBar1.setTextColor(Color.WHITE);
					tvBar2.setTextColor(Color.WHITE);
					tvBar3.setTextColor(0xffea3143);
					tvBar4.setTextColor(Color.WHITE);
					tvBar5.setTextColor(Color.WHITE);
					tvBar6.setTextColor(Color.WHITE);
					tvBar7.setTextColor(Color.WHITE);
					tvBar8.setTextColor(Color.WHITE);
				}else if (arg0 == 3) {
					tvBar1.setTextColor(Color.WHITE);
					tvBar2.setTextColor(Color.WHITE);
					tvBar3.setTextColor(Color.WHITE);
					tvBar4.setTextColor(0xffea3143);
					tvBar5.setTextColor(Color.WHITE);
					tvBar6.setTextColor(Color.WHITE);
					tvBar7.setTextColor(Color.WHITE);
					tvBar8.setTextColor(Color.WHITE);
				}else if (arg0 == 4) {
					tvBar1.setTextColor(Color.WHITE);
					tvBar2.setTextColor(Color.WHITE);
					tvBar3.setTextColor(Color.WHITE);
					tvBar4.setTextColor(Color.WHITE);
					tvBar5.setTextColor(0xffea3143);
					tvBar6.setTextColor(Color.WHITE);
					tvBar7.setTextColor(Color.WHITE);
					tvBar8.setTextColor(Color.WHITE);
				}else if (arg0 == 5) {
					tvBar1.setTextColor(Color.WHITE);
					tvBar2.setTextColor(Color.WHITE);
					tvBar3.setTextColor(Color.WHITE);
					tvBar4.setTextColor(Color.WHITE);
					tvBar5.setTextColor(Color.WHITE);
					tvBar6.setTextColor(0xffea3143);
					tvBar7.setTextColor(Color.WHITE);
					tvBar8.setTextColor(Color.WHITE);
				}else if (arg0 == 6) {
					tvBar1.setTextColor(Color.WHITE);
					tvBar2.setTextColor(Color.WHITE);
					tvBar3.setTextColor(Color.WHITE);
					tvBar4.setTextColor(Color.WHITE);
					tvBar5.setTextColor(Color.WHITE);
					tvBar6.setTextColor(Color.WHITE);
					tvBar7.setTextColor(0xffea3143);
					tvBar8.setTextColor(Color.WHITE);
				}else if (arg0 == 7) {
					tvBar1.setTextColor(Color.WHITE);
					tvBar2.setTextColor(Color.WHITE);
					tvBar3.setTextColor(Color.WHITE);
					tvBar4.setTextColor(Color.WHITE);
					tvBar5.setTextColor(Color.WHITE);
					tvBar6.setTextColor(Color.WHITE);
					tvBar7.setTextColor(Color.WHITE);
					tvBar8.setTextColor(0xffea3143);
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/**
	 * 头标点击监听
	 * @author shawn_sun
	 */
	private class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index, false);
			}
		}
	}
	
	/**
	 * 获取天气要素数据
	 */
	private void OkHttpWeather(final String url, String fNumber) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("FacilityNumber", fNumber);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
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
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("code")) {
												String code  = object.getString("code");
												if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
													if (!object.isNull("at")) {//实况
														JSONObject atObj = object.getJSONObject("at");
														if (!atObj.isNull("temperature")) {
															tvTemp.setText(atObj.getString("temperature")+"");
														}
														if (!atObj.isNull("humidity")) {
															tvHumidity.setText(atObj.getString("humidity")+"");
														}
														if (!atObj.isNull("precipitation")) {
															tvRain.setText(atObj.getString("precipitation")+"");
														}
														if (!atObj.isNull("precipitationLevel")) {
															tvRainLevel.setText(atObj.getString("precipitationLevel")+"");
														}
														if (!atObj.isNull("quality")) {
															tvQuality.setText(atObj.getString("quality")+"");
														}
														if (!atObj.isNull("wind")) {
															tvWindSpeed.setText(atObj.getString("wind")+"");
														}
														if (!atObj.isNull("direction")) {
															String windDir = atObj.getString("direction");
															if (!TextUtils.isEmpty(windDir)) {
																tvWindDir.setText(CommonUtil.getWindDir(windDir));
															}
														}
														if (!atObj.isNull("pressure")) {
															tvPressure.setText(atObj.getString("pressure")+"");
														}
														if (!atObj.isNull("Ultraviolet")) {
															tvUltraviolet.setText(atObj.getString("Ultraviolet")+"");
														}
													}

													if (!object.isNull("list")) {
														weatherList.clear();
														JSONArray array = object.getJSONArray("list");
														for (int i = 0; i < array.length(); i++) {
															EyeDto dto = new EyeDto();
															JSONObject itemObj = array.getJSONObject(i);
															if (!itemObj.isNull("Datetime")) {
																dto.time = itemObj.getString("Datetime");
															}
															if (!itemObj.isNull("temperature")) {
																dto.temperature = Float.parseFloat(itemObj.getString("temperature"));
															}
															if (!itemObj.isNull("humidity")) {
																dto.humidity = Float.parseFloat(itemObj.getString("humidity"));
															}
															if (!itemObj.isNull("precipitation")) {
																dto.precipitation = Float.parseFloat(itemObj.getString("precipitation"));
															}
															if (!itemObj.isNull("precipitationLevel")) {
																dto.precipitationLevel = Float.parseFloat(itemObj.getString("precipitationLevel"));
															}
															if (!itemObj.isNull("quality")) {
																dto.quality = Float.parseFloat(itemObj.getString("quality"));
															}
															if (!itemObj.isNull("wind")) {
																dto.windSpeed = Float.parseFloat(itemObj.getString("wind"));
															}
															if (!itemObj.isNull("direction")) {
																dto.windDir = itemObj.getString("direction");
															}
															if (!itemObj.isNull("pressure")) {
																dto.pressure = Float.parseFloat(itemObj.getString("pressure"));
															}
															if (!itemObj.isNull("Ultraviolet")) {
																dto.ultraviolet = Float.parseFloat(itemObj.getString("Ultraviolet"));
															}
															weatherList.add(dto);
														}

														initViewPager(weatherList);
													}

													llContent.setVisibility(View.VISIBLE);
												}else {
													//失败
													if (!object.isNull("reason")) {
														String reason = object.getString("reason");
														if (!TextUtils.isEmpty(reason)) {
															Toast.makeText(mContext, reason, Toast.LENGTH_SHORT).show();
														}
													}
												}
											}
										}
										cancelDialog();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
	        overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
			break;

		default:
			break;
		}
	}

}
