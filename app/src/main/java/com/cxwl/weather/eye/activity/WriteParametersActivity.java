package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.utils.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 摄像头参数录入
 * @author shawn_sun
 *
 */

public class WriteParametersActivity extends BaseActivity implements OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private ImageView ivControl = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private TextView tvSeries = null;//序列号
	private TextView tvLng, tvLat = null;//经纬度
	private EditText etPro, etCity, etArea, etAddr = null;//地址
	private EditText etIp = null;//设备所在内网ip
	private ImageView ivLocation = null;
	private ScrollView scrollView = null;
	private TextView tvPost = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write_parameters);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivControl = (ImageView) findViewById(R.id.ivControl);
		ivControl.setOnClickListener(this);
		ivControl.setVisibility(View.VISIBLE);
		ivControl.setImageResource(R.drawable.eye_btn_add);
		tvSeries = (TextView) findViewById(R.id.tvSeries);
		tvLng = (TextView) findViewById(R.id.tvLng);
		tvLat = (TextView) findViewById(R.id.tvLat);
		etPro = (EditText) findViewById(R.id.etPro);
		etCity = (EditText) findViewById(R.id.etCity);
		etArea = (EditText) findViewById(R.id.etArea);
		etAddr = (EditText) findViewById(R.id.etAddr);
		etIp = (EditText) findViewById(R.id.etIp);
		ivLocation = (ImageView) findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		tvPost = (TextView) findViewById(R.id.tvPost);
		tvPost.setOnClickListener(this);
		
		startLocation();
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
        mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		cancelDialog();
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
			tvLng.setText(amapLocation.getLongitude()+"");
			tvLat.setText(amapLocation.getLatitude()+"");
			etPro.setText(amapLocation.getProvince());
			if (!TextUtils.isEmpty(etPro.getText().toString())) {
				etPro.setSelection(etPro.getText().toString().length());
			}
			etCity.setText(amapLocation.getCity());
			etArea.setText(amapLocation.getDistrict());
			String addr = amapLocation.getAoiName();
			if (TextUtils.isEmpty(addr)) {
				addr = amapLocation.getRoad();
			}
    		etAddr.setText(addr);
        }
	}
	
	/**
	 * post序列号给后台确认信息
	 */
	private void OkHttpSeriesNo(final String url, String seriesNo) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("FacilityNumber", seriesNo);
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
													if (!object.isNull("list")) {
														JSONObject obj = new JSONObject(object.getString("list"));
														if (!obj.isNull("Longitude")) {
															tvLng.setText(obj.getString("Longitude"));
														}

														if (!obj.isNull("Dimensionality")) {
															tvLat.setText(obj.getString("Dimensionality"));
														}

														if (!obj.isNull("Province")) {
															etPro.setText(obj.getString("Province"));
															if (!TextUtils.isEmpty(etPro.getText().toString())) {
																etPro.setSelection(etPro.getText().toString().length());
															}
														}

														if (!obj.isNull("City")) {
															etCity.setText(obj.getString("City"));
														}

														if (!obj.isNull("County")) {
															etArea.setText(obj.getString("County"));
														}

														if (!obj.isNull("Location")) {
															etAddr.setText(obj.getString("Location"));
														}

														scrollView.setVisibility(View.VISIBLE);
													}
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
	
	/**
	 * 验证post信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(tvLng.getText().toString()) || TextUtils.isEmpty(tvLat.getText().toString())) {
			Toast.makeText(mContext, "请点击定位获取经纬度", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPro.getText().toString())) {
			Toast.makeText(mContext, "请输入所在省名称", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etCity.getText().toString())) {
			Toast.makeText(mContext, "请输入所在市名称", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etArea.getText().toString())) {
			Toast.makeText(mContext, "请输入所在县（区）名称", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etAddr.getText().toString())) {
			Toast.makeText(mContext, "请输入具体地址", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etIp.getText().toString())) {
			Toast.makeText(mContext, "请输入内网IP", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * post上传信息给后台
	 */
	private void OkHttpInfo(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("FacilityNumber", tvSeries.getText().toString());
		builder.add("Longitude", tvLng.getText().toString());
		builder.add("Dimensionality", tvLat.getText().toString());
		builder.add("Province", etPro.getText().toString());
		builder.add("City", etCity.getText().toString());
		builder.add("County", etArea.getText().toString());
		builder.add("Location", etAddr.getText().toString());
		builder.add("FacilityIP", etIp.getText().toString());
		RequestBody body = builder.build();
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
						cancelDialog();
						Intent intent = new Intent(mContext, ReadParametersActivity.class);
						intent.putExtra("result", result);
						startActivity(intent);
					}
				});
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivControl:
			startActivityForResult(new Intent(mContext, ZXingActivity.class), 0);
			break;
		case R.id.ivLocation:
			showDialog();
			startLocation();
			break;
		case R.id.tvPost:
			if (checkInfo()) {
				showDialog();
				OkHttpInfo("https://tqwy.tianqi.cn/tianqixy/userInfo/faciliupapp");
			}
			break;

		default:
			break;
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        	switch (requestCode) {
        	case 0:
        		if(resultCode == RESULT_OK){
        			Bundle bundle = data.getExtras();
        			String result = bundle.getString("result");
        			if (!TextUtils.isEmpty(result)) {
        				tvSeries.setText(result);
        				showDialog();
						OkHttpSeriesNo("https://tqwy.tianqi.cn/tianqixy/userInfo/faciliisnull", result);
        			}
        		}
        		break;
        	}
		}
    }	

}
