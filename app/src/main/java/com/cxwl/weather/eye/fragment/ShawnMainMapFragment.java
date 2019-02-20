package com.cxwl.weather.eye.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.cxwl.weather.eye.activity.ShawnVideoDetailActivity;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面-地图
 */
public class ShawnMainMapFragment extends Fragment implements OnClickListener, OnMarkerClickListener{
	
	private MapView mMapView;
	private AMap aMap;
	private List<EyeDto> dataList = new ArrayList<>();
	private List<Marker> markerList = new ArrayList<>();
	private LatLng locationLatLng = new LatLng(35.926628, 105.178100);
	private float zoom = 3.7f;
	private ImageView ivLocation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.shawn_fragment_main_map, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initMap(view, savedInstanceState);
		initWidget(view);
	}

	private void initWidget(View view) {
		ImageView ivRefresh = view.findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		ImageView ivZoomIn = view.findViewById(R.id.ivZoomIn);
		ivZoomIn.setOnClickListener(this);
		ImageView ivZoomOut = view.findViewById(R.id.ivZoomOut);
		ivZoomOut.setOnClickListener(this);
		ivLocation = view.findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);

		getListData();
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
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
	}

	private void getListData() {
		dataList.clear();
		dataList.addAll(getArguments().<EyeDto>getParcelableArrayList("dataList"));
		addMarkers();
	}
	
	private void removeMarkers() {
		for (Marker marker : markerList) {
			marker.remove();
		}
		markerList.clear();
	}

	private void addMarkers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				removeMarkers();
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
					View view = inflater.inflate(R.layout.shawn_layout_marker_icon, null);
					TextView tvMarker = view.findViewById(R.id.tvMarker);
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
						animation.setDuration(400);
						marker.setAnimation(animation);
						marker.startAnimation();
					}
				}
				if (dataList.size() > 0) {
					aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
				}
			}
		}).start();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (CommonUtil.showExperienceTime(getActivity())) {
			CommonUtil.dialogExpericence(getActivity());
		}else {
			for (int i = 0; i < dataList.size(); i++) {
				EyeDto dto = dataList.get(i);
				if (TextUtils.equals(dto.fId, marker.getSnippet())) {
					Intent intent = new Intent(getActivity(), ShawnVideoDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
			}
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivRefresh:
				getListData();
				break;
			case R.id.ivZoomIn:
				aMap.animateCamera(CameraUpdateFactory.zoomIn());
				break;
			case R.id.ivZoomOut:
				aMap.animateCamera(CameraUpdateFactory.zoomOut());
				break;
			case R.id.ivLocation:
				if (zoom < 10.f) {
					ivLocation.setImageResource(R.drawable.shawn_icon_warning_location_press);
					zoom = 10.0f;
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom));
				} else {
					ivLocation.setImageResource(R.drawable.shawn_icon_warning_location);
					zoom = 3.7f;
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom));
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

}
