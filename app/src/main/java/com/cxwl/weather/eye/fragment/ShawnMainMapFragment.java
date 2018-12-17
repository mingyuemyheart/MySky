package com.cxwl.weather.eye.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.activity.VideoDetailActivity;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.MyDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 主界面-地图
 */
public class ShawnMainMapFragment extends Fragment implements OnClickListener, OnMarkerClickListener{
	
	private MapView mMapView;
	private AMap aMap;
	private List<EyeDto> dataList = new ArrayList<>();
	private MyDialog mDialog;
	private List<Marker> markerList = new ArrayList<>();
	private ImageView ivJiangshui,ivLegend;
	private TextView tvName,tvTime;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy.MM.dd HH:00", Locale.CHINA);
	private List<Polygon> polygons = new ArrayList<>();//图层数据

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.shawn_fragment_main_map, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showDialog();
		initMap(view, savedInstanceState);
		initWidget(view);
	}
	
	private void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog(getActivity());
		}
		mDialog.show();
	}
	
	private void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	private void initWidget(View view) {
		ImageView ivRefresh = view.findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		ivJiangshui = view.findViewById(R.id.ivJiangshui);
		ivJiangshui.setOnClickListener(this);
		ivLegend = view.findViewById(R.id.ivLegend);
		tvName = view.findViewById(R.id.tvName);
		tvName.setText("全国降水量预报24小时");
		tvTime = view.findViewById(R.id.tvTime);

		String imgUrl = "http://decision-admin.tianqi.cn/Public/images/decision_images/%E9%99%8D%E6%B0%B4.png";
		Picasso.get().load(imgUrl).into(ivLegend);

		refresh();
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap(View view, Bundle bundle) {
		mMapView = view.findViewById(R.id.mapView);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.7f));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
	}
	
	private void refresh() {
		OkHttpList();
	}
	
	/**
	 * 获取地图上所有设备信息
	 */
	private void OkHttpList() {
		String c = null;
		for (String host : OkHttpUtil.cookieMap.keySet()) {
			if (OkHttpUtil.cookieMap.containsKey(host)) {
				List<Cookie> cookies = OkHttpUtil.cookieMap.get(host);
				for (Cookie cookie : cookies) {
					c = cookie.name()+":"+cookie.value();
				}
			}
		}
		final String url = String.format("https://api.bluepi.tianqi.cn/outdata/other/newselmallf/cookie/%s&UserNo=%s", c, MyApplication.USERNAME);
//		final String url = "https://tqwy.tianqi.cn/tianqixy/userInfo/selmallf";
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (!object.isNull("code")) {
											String code  = object.getString("code");
											if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
												if (!object.isNull("list")) {
                                                    dataList.clear();
													JSONArray array = new JSONArray(object.getString("list"));
													for (int i = 0; i < array.length(); i++) {
														JSONObject itemObj = array.getJSONObject(i);
														EyeDto dto = new EyeDto();
														if (!itemObj.isNull("Fzid")) {
															dto.fGroupId = itemObj.getString("Fzid");
														}
														if (!itemObj.isNull("Fid")) {
															dto.fId = itemObj.getString("Fid");
														}
														if (!itemObj.isNull("FacilityIP")) {
															dto.fGroupIp = itemObj.getString("FacilityIP");
														}
														if (!itemObj.isNull("Location")) {
															dto.location = itemObj.getString("Location");
														}
														if (!itemObj.isNull("StatusUrl")) {
															dto.StatusUrl = itemObj.getString("StatusUrl");
														}
														if (!itemObj.isNull("FacilityNumber")) {
															dto.fNumber = itemObj.getString("FacilityNumber");
														}
														if (!itemObj.isNull("FacilityUrlWithin")) {
															dto.streamPrivate = itemObj.getString("FacilityUrlWithin");
														}
														if (!itemObj.isNull("FacilityUrl")) {
															dto.streamPublic = itemObj.getString("FacilityUrl");
														}
														if (!itemObj.isNull("Dimensionality")) {
															dto.lat = itemObj.getString("Dimensionality");
														}
														if (!itemObj.isNull("Longitude")) {
															dto.lng = itemObj.getString("Longitude");
														}
                                                        dataList.add(dto);
													}

													addMarkers();

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

	private void removeMarkers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (Marker marker : markerList) {
					marker.remove();
				}
				markerList.clear();
			}
		}).start();
	}

	private void addMarkers() {
		removeMarkers();

		new Thread(new Runnable() {
			@Override
			public void run() {
				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				for (int i = 0; i < dataList.size(); i++) {
					EyeDto dto = dataList.get(i);
					MarkerOptions options = new MarkerOptions();
					if (!TextUtils.isEmpty(dto.fId)) {
						options.snippet(dto.fId);
					}
					options.anchor(0.5f, 0.5f);
					if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
						LatLng latLng = new LatLng(Double.valueOf(dto.lat), Double.valueOf(dto.lng));
						builder.include(latLng);
						options.position(latLng);
					}
					View view = inflater.inflate(R.layout.view_eye_marker, null);
					TextView tvMarker = view.findViewById(R.id.tvMarker);
//					if (!TextUtils.isEmpty(dto.location)) {
//						tvMarker.setText(dto.location.trim());
//						tvMarker.setVisibility(View.VISIBLE);
//					}
					options.icon(BitmapDescriptorFactory.fromView(view));
					Marker marker = aMap.addMarker(options);
					if (marker != null) {
						markerList.add(marker);
						Animation animation = new ScaleAnimation(0,1,0,1);
						animation.setInterpolator(new LinearInterpolator());
						animation.setDuration(400);
						marker.setAnimation(animation);
						marker.startAnimation();
					}
				}
				aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
			}
		}).start();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		for (int i = 0; i < dataList.size(); i++) {
			EyeDto dto = dataList.get(i);
			if (TextUtils.equals(dto.fId, marker.getSnippet())) {
				Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivRefresh:
				showDialog();
				refresh();
				break;
			case R.id.ivJiangshui:
				if (ivLegend.getVisibility() == View.VISIBLE) {
					tvName.setVisibility(View.GONE);
					tvTime.setVisibility(View.GONE);
					ivLegend.setVisibility(View.INVISIBLE);
					ivJiangshui.setImageResource(R.drawable.com_jiangshui);
					clearPolygons();
				} else {
					OkHttpRain();
					tvName.setVisibility(View.VISIBLE);
					tvTime.setVisibility(View.VISIBLE);
					ivLegend.setVisibility(View.VISIBLE);
					ivJiangshui.setImageResource(R.drawable.com_jiangshui_press);
				}
				break;

			default:
				break;
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (mMapView != null) {
			mMapView.onResume();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (mMapView != null) {
			mMapView.onPause();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMapView != null) {
			mMapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
	}

	/**
	 * 24小时降水
	 */
	private void OkHttpRain() {
		if (polygons.size() > 0) {
			for (int i = 0; i < polygons.size(); i++) {
				polygons.get(i).setVisible(true);
			}
			return;
		}
		final String url = "http://scapi.weather.com.cn/weather/micapsfile?fileMark=js_24_fc&isChina=true&date=201809171150&appid=f63d32&key=zVlS26V8Z8xYEIS6dUKdtPxEOlw%3D";
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
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONArray arr = new JSONArray(result);
								if (arr.length() > 0) {
									JSONObject obj = arr.getJSONObject(0);

									if (!obj.isNull("time")) {
										final long time = obj.getLong("time");
										final int validhour = 24*1000*60*60;
										final int starthour = 0;
										getActivity().runOnUiThread(new Runnable() {
											@Override
											public void run() {
												tvTime.setText(sdf1.format(time+starthour)+" - "+sdf1.format(time+starthour+validhour));
											}
										});
									}

									if (!obj.isNull("areas")) {
										clearPolygons();
										JSONArray array = obj.getJSONArray("areas");
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											String color = itemObj.getString("c");
											if (color.contains("#")) {
												color = color.replace("#", "");
											}
											int r = Integer.parseInt(color.substring(0,2), 16);
											int g = Integer.parseInt(color.substring(2,4), 16);
											int b = Integer.parseInt(color.substring(4,6), 16);
											if (!itemObj.isNull("items")) {
												JSONArray items = itemObj.getJSONArray("items");
												PolygonOptions polygonOption = new PolygonOptions();
												polygonOption.strokeColor(Color.rgb(r, g, b)).fillColor(Color.rgb(r, g, b));
												for (int j = 0; j < items.length(); j++) {
													JSONObject item = items.getJSONObject(j);
													double lat = item.getDouble("y");
													double lng = item.getDouble("x");
													polygonOption.add(new LatLng(lat, lng));
												}
												Polygon polygon = aMap.addPolygon(polygonOption);
												polygon.setVisible(true);
												polygons.add(polygon);
											}
										}
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

	private void clearPolygons() {
		for (int i = 0; i < polygons.size(); i++) {
			polygons.get(i).setVisible(false);
		}
	}
	
}
