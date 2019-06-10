package com.cxwl.weather.eye.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.dto.WeatherDto;
import com.cxwl.weather.eye.utils.AuthorityUtil;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.utils.SecretUrlUtil;
import com.cxwl.weather.eye.utils.WeatherUtil;
import com.cxwl.weather.eye.view.ShawnHourTempView;
import com.cxwl.weather.eye.view.ShawnWeeklyView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 未来天气
 */
public class ShawnForecastActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private EyeDto data;
	private TextView tvTemp, tvHumidity, tvQuality, tvWindSpeed, tvWindDir, tvPressure;
	private LinearLayout llContainer1,llContainer2;
	private int width;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private RelativeLayout reContent;
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_forecast);
		mContext = this;
		initRefreshLayout();
		initWidget();
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 400);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}

	private void refresh() {
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				checkAuthority();
			}
		}
	}


	/**
	 * 初始化控件
	 */
	private void initWidget() {
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("未来天气");
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTemp = findViewById(R.id.tvTemp);
		tvQuality = findViewById(R.id.tvQuality);
		tvHumidity = findViewById(R.id.tvHumidity);
		tvPressure = findViewById(R.id.tvPressure);
		tvWindSpeed = findViewById(R.id.tvWindSpeed);
		tvWindDir = findViewById(R.id.tvWindDir);
		llContainer1 = findViewById(R.id.llContainer1);
		llContainer2 = findViewById(R.id.llContainer2);
		reContent = findViewById(R.id.reContent);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;

		refresh();;
	}

	private void getWeatherInfo() {
		if (!TextUtils.isEmpty(data.lng) && !TextUtils.isEmpty(data.lat)) {
			OkHttpGeo(Double.valueOf(data.lng), Double.valueOf(data.lat));
		}
	}

	/**
	 * 获取天气数据
	 */
	private void OkHttpGeo(final double lng, final double lat) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.geo(lng, lat)).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("geo")) {
									JSONObject geoObj = obj.getJSONObject("geo");
									if (!geoObj.isNull("id")) {
										String cityId = geoObj.getString("id");
										getWeatherInfo(cityId);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

	private void getWeatherInfo(final String cityId) {
		if (TextUtils.isEmpty(cityId)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				WeatherAPI.getWeather2(mContext, cityId, Constants.Language.ZH_CN, new AsyncResponseHandler() {
					@Override
					public void onComplete(final Weather content) {
						super.onComplete(content);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String result = content.toString();
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);

										//实况信息
										if (!obj.isNull("l")) {
											JSONObject object = obj.getJSONObject("l");
											if (!object.isNull("l1")) {
												String factTemp = WeatherUtil.lastValue(object.getString("l1"));
												if (factTemp != null) {
													tvTemp.setText(factTemp+"℃");
												}
											}
											if (!object.isNull("l2")) {
												String humidity = WeatherUtil.lastValue(object.getString("l2"));
												if (humidity != null) {
													tvHumidity.setText(humidity+"%");
												}
											}
											if (!object.isNull("l10")) {
												String pressure = WeatherUtil.lastValue(object.getString("l10"));
												if (pressure != null) {
													tvPressure.setText(pressure+"hPa");
												}
											}
											if (!object.isNull("l3")) {
												String windForce = WeatherUtil.lastValue(object.getString("l3"));
												if (windForce != null) {
													tvWindSpeed.setText(WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
												}
											}
											if (!object.isNull("l4")) {
												String windDir = WeatherUtil.lastValue(object.getString("l4"));
												if (windDir != null) {
													tvWindDir.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))));
												}
											}
										}

										//空气质量
										if (!obj.isNull("k")) {
											JSONObject k = obj.getJSONObject("k");
											if (!k.isNull("k3")) {
												String airQua = WeatherUtil.lastValue(k.getString("k3"));
												if (airQua != null) {
													tvQuality.setText(airQua);
												}
											}
										}

										//逐小时预报信息
										if (!obj.isNull("jh")) {
											JSONArray jh = obj.getJSONArray("jh");
											List<WeatherDto> hourlyList = new ArrayList<>();
											for (int i = 0; i < jh.length(); i++) {
												JSONObject itemObj = jh.getJSONObject(i);
												WeatherDto dto = new WeatherDto();
												dto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
												dto.hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
												dto.hourlyDirCode = Integer.valueOf(itemObj.getString("jc"));
												dto.hourlyForce = Integer.valueOf(itemObj.getString("jd"));
												dto.hourlyTime = itemObj.getString("jf");
												hourlyList.add(dto);
											}

											ShawnHourTempView hourlyView = new ShawnHourTempView(mContext);
											hourlyView.setData(hourlyList);
											llContainer1.removeAllViews();
											llContainer1.addView(hourlyView, width, (int)(CommonUtil.dip2px(mContext, 150)));
										}

										//15天预报
										if (!obj.isNull("f")) {
											List<WeatherDto> weeklyList = new ArrayList<>();
											JSONObject f = obj.getJSONObject("f");
											String f0 = f.getString("f0");
											if (!f.isNull("f1")) {
												JSONArray f1 = f.getJSONArray("f1");
												for (int i = 0; i < f1.length(); i++) {
													WeatherDto dto = new WeatherDto();

													dto.week = CommonUtil.getWeek(i);//星期几
													dto.date = CommonUtil.getDate(f0, i);//日期

													JSONObject weeklyObj = f1.getJSONObject(i);
													//晚上
													dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
													dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
													dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
													dto.lowWindDir = Integer.valueOf(weeklyObj.getString("ff"));
													dto.lowWindForce = Integer.valueOf(weeklyObj.getString("fh"));

													//白天
													dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
													dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
													dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
													dto.highWindDir = Integer.valueOf(weeklyObj.getString("fe"));
													dto.highWindForce = Integer.valueOf(weeklyObj.getString("fg"));

													weeklyList.add(dto);

												}
											}

											//一周预报曲线
											ShawnWeeklyView weeklyView = new ShawnWeeklyView(mContext);
											weeklyView.setData(weeklyList);
											llContainer2.removeAllViews();
											llContainer2.addView(weeklyView, width*2, (int)(CommonUtil.dip2px(mContext, 400)));

										}

										reContent.setVisibility(View.VISIBLE);

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								refreshLayout.setRefreshing(false);

							}
						});
					}

					@Override
					public void onError(Throwable error, String content) {
						super.onError(error, content);
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

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.READ_PHONE_STATE
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			getWeatherInfo();
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				getWeatherInfo();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_LOCATION:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int gResult : grantResults) {
						if (gResult != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						getWeatherInfo();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						checkAuthority();
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
							checkAuthority();
							break;
						}
					}
				}
				break;
		}
	}

}
