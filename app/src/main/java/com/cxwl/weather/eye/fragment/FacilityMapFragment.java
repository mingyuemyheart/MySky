package com.cxwl.weather.eye.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.activity.VideoDetailActivity;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.MyDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 地图
 * @author shawn_sun
 *
 */

public class FacilityMapFragment extends Fragment implements OnClickListener, OnMarkerClickListener{
	
	private MapView mMapView = null;
	private AMap aMap = null;
	private float zoom = 3.7f;
	private List<EyeDto> eyeList = new ArrayList<>();
	private MyDialog mDialog = null;
	private ImageView ivRefresh = null;
	private List<Marker> markerList = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_facilitymap, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showDialog();
		initMap(view, savedInstanceState);
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
	
	/**
	 * 初始化地图
	 */
	private void initMap(View view, Bundle bundle) {
		mMapView = (MapView) view.findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		
		ivRefresh = (ImageView) view.findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		
		refresh();
	}
	
	private void refresh() {
		OkHttpList("https://tqwy.tianqi.cn/tianqixy/userInfo/selmallf");
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		for (int i = 0; i < eyeList.size(); i++) {
			EyeDto dto = eyeList.get(i);
			if (TextUtils.equals(dto.fId, arg0.getSnippet())) {
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
	
	/**
	 * 获取地图上所有设备信息
	 */
	private void OkHttpList(final String url) {
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
										if (object != null) {
											if (!object.isNull("code")) {
												String code  = object.getString("code");
												if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
													if (!object.isNull("list")) {
														eyeList.clear();
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
															eyeList.add(dto);
														}

														aMap.clear();
														markerList.clear();
														LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
														for (int i = 0; i < eyeList.size(); i++) {
															EyeDto dto = eyeList.get(i);
															MarkerOptions options = new MarkerOptions();
															if (!TextUtils.isEmpty(dto.fId)) {
																options.snippet(dto.fId);
															}
															options.anchor(0.5f, 0.5f);
															if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
																options.position(new LatLng(Double.valueOf(dto.lat), Double.valueOf(dto.lng)));
															}
															View view = inflater.inflate(R.layout.view_eye_marker, null);
															TextView tvMarker = (TextView) view.findViewById(R.id.tvMarker);
															if (!TextUtils.isEmpty(dto.location)) {
																tvMarker.setText(dto.location.trim());
																tvMarker.setVisibility(View.VISIBLE);
															}
															options.icon(BitmapDescriptorFactory.fromView(view));
															Marker marker = aMap.addMarker(options);
															if (marker != null) {
																markerList.add(marker);
																Animation animation = new ScaleAnimation(0,1,0,1);
																animation.setInterpolator(new LinearInterpolator());
																//整个移动所需要的时间
																animation.setDuration(1000);
																//设置动画
																marker.setAnimation(animation);
																//开始动画
																marker.startAnimation();
															}
														}

//									if (markerList.size() > 0) {
//										double leftLat = markerList.get(0).getPosition().latitude;
//										double leftLng = markerList.get(0).getPosition().longitude;
//										double rightLat = markerList.get(0).getPosition().latitude;
//										double rightLng = markerList.get(0).getPosition().longitude;
//										for (int i = 0; i < markerList.size(); i++) {
//											if (leftLat >= markerList.get(i).getPosition().latitude) {
//												leftLat = markerList.get(i).getPosition().latitude;
//											}
//											if (leftLng >= markerList.get(i).getPosition().longitude) {
//												leftLng = markerList.get(i).getPosition().longitude;
//											}
//											if (rightLat <= markerList.get(i).getPosition().latitude) {
//												rightLat = markerList.get(i).getPosition().latitude;
//											}
//											if (rightLng <= markerList.get(i).getPosition().longitude) {
//												rightLng = markerList.get(i).getPosition().longitude;
//											}
//										}
//
//										final LatLng left = new LatLng(leftLat, leftLng);
//										final LatLng right = new LatLng(rightLat, rightLng);
//										//延时1秒开始地图动画
//										new Handler().postDelayed(new Runnable() {
//											@Override
//											public void run() {
//												try {
//													LatLngBounds bounds = new LatLngBounds.Builder()
//															.include(left)
//															.include(right).build();
//													aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
//												} catch (ArrayIndexOutOfBoundsException e) {
//													e.printStackTrace();
//												}
//											}
//										}, 500);
//									}

														new Handler().postDelayed(new Runnable() {
															@Override
															public void run() {
																try {
																	LatLngBounds bounds = new LatLngBounds.Builder()
																			.include(new LatLng(1, 75))
																			.include(new LatLng(60, 135)).build();
																	aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
																} catch (ArrayIndexOutOfBoundsException e) {
																	e.printStackTrace();
																}
															}
														}, 500);

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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivRefresh:
			showDialog();
			refresh();
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
	
}
