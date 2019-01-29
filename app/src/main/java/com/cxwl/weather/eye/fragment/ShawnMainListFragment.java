package com.cxwl.weather.eye.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.activity.ShawnCityActivity;
import com.cxwl.weather.eye.activity.ShawnVideoDetailActivity;
import com.cxwl.weather.eye.adapter.ShawnMainListAdapter;
import com.cxwl.weather.eye.adapter.ShawnRecmmendAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.ScrollviewListview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.Request;
import okhttp3.Response;

import static com.cxwl.weather.eye.common.MyApplication.USERNAME;

/**
 * 主界面-列表
 */
public class ShawnMainListFragment extends Fragment implements View.OnClickListener {

	private Map<String, String> imgMap = new LinkedHashMap<>();//存放天津缩略图
	private ScrollView scrollView;
	private TextView tvAll;
	private ShawnMainListAdapter mAdapter;
	private List<EyeDto> dataList = new ArrayList<>();
	private ShawnRecmmendAdapter recmmendAdapter;
	private List<EyeDto> recommendList = new ArrayList<>();
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.shawn_fragment_main_list, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
		initGridView(view);
		initListView(view);
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
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
		dataList.clear();
		OkHttpTJImgs();
	}
	
	private void initWidget(View view) {
		tvAll = view.findViewById(R.id.tvAll);
		tvAll.setOnClickListener(this);
		scrollView = view.findViewById(R.id.scrollView);

		refresh();
	}

	private void initGridView(View view) {
		GridView gridView = view.findViewById(R.id.gridView);
		recmmendAdapter = new ShawnRecmmendAdapter(getActivity(), recommendList);
		gridView.setAdapter(recmmendAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (CommonUtil.showExperienceTime(getActivity())) {
					CommonUtil.dialogExpericence(getActivity());
				}else {
					EyeDto dto = recommendList.get(arg2);
					Intent intent = new Intent(getActivity(), ShawnVideoDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}
	
	private void initListView(View view) {
		ScrollviewListview listView = view.findViewById(R.id.listView);
		mAdapter = new ShawnMainListAdapter(getActivity(), dataList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (CommonUtil.showExperienceTime(getActivity())) {
					CommonUtil.dialogExpericence(getActivity());
				}else {
					EyeDto dto = dataList.get(arg2);
					Intent intent = new Intent(getActivity(), ShawnVideoDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}

	/**
	 * 获取天津独有缩略图
	 */
	private void OkHttpTJImgs() {
		final String url = "https://api.bluepi.tianqi.cn/outdata/other/tqwyTjCovers";
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						OkHttpList();
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							OkHttpList();
							return;
						}
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							if (result.contains("{")) {
								result = result.replace("{", "");
							}
							if (result.contains("}")) {
								result = result.replace("}", "");
							}
							String[] array = result.split(",");
							imgMap.clear();
							for (int i = 0; i < array.length; i++) {
								String[] imgs = array[i].split("\":\"");
								String img0 = imgs[0];
								if (img0.contains("\"")) {
									img0 = img0.replace("\"", "");
								}
								String img1 = imgs[1];
								if (img0.contains("\"")) {
									img1 = img1.replace("\"", "");
								}
								imgMap.put(img0, img1);
							}
						}
						OkHttpList();
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取视频列表
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
		final String url = String.format("https://api.bluepi.tianqi.cn/outdata/other/newselmallf/cookie/%s&UserNo=%s", c, USERNAME);
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
													List<EyeDto> tempList = new ArrayList<>();
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
														if (!itemObj.isNull("Province")) {
															dto.provinceName = itemObj.getString("Province");
														}
														if (!itemObj.isNull("City")) {
															dto.cityName = itemObj.getString("City");
														}
														if (!itemObj.isNull("County")) {
															dto.disName = itemObj.getString("County");
														}
														if (!itemObj.isNull("Location")) {
															dto.location = itemObj.getString("Location").trim();
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
														if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
															dto.distance = AMapUtils.calculateLineDistance(new LatLng(CONST.locationLat, CONST.locationLng), new LatLng(Double.valueOf(dto.lat), Double.valueOf(dto.lng)));
														}
														if (!itemObj.isNull("small")) {
														    dto.videoThumbUrl = itemObj.getString("small");
                                                        }
                                                        if (!itemObj.isNull("FacilityUrlTes")) {
                                                            dto.facilityUrlTes = itemObj.getString("FacilityUrlTes");
                                                            if (!TextUtils.isEmpty(dto.facilityUrlTes) && imgMap.containsKey(dto.facilityUrlTes)) {
                                                            	dto.videoThumbUrl = imgMap.get(dto.facilityUrlTes);
															}
                                                        }
														if (!itemObj.isNull("ErectTime")) {
															dto.erectTime = itemObj.getString("ErectTime");
														}
														dataList.add(dto);
														tempList.add(dto);
													}

													if (dataList.size() > 0) {
														tvAll.setVisibility(View.VISIBLE);
														scrollView.setVisibility(View.VISIBLE);
													}else {
														tvAll.setVisibility(View.INVISIBLE);
														scrollView.setVisibility(View.INVISIBLE);
													}

													if (mAdapter != null) {
														mAdapter.notifyDataSetChanged();
													}

													Collections.sort(tempList, new Comparator<EyeDto>() {
														@Override
														public int compare(EyeDto arg0, EyeDto arg1) {
															Log.e("distance", arg0.distance+"");
															return Double.valueOf(arg0.distance).compareTo(Double.valueOf(arg1.distance));
														}
													});

													recommendList.clear();
													int size = tempList.size();
													if (tempList.size() >= 2) {
														size = 2;
													}
													for (int i = 0; i < size; i++) {
														EyeDto data = tempList.get(i);
														recommendList.add(data);
													}
													if (recmmendAdapter != null) {
														recmmendAdapter.notifyDataSetChanged();
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
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								refreshLayout.setRefreshing(false);
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
			case R.id.tvAll:
				if (CommonUtil.showExperienceTime(getActivity())) {
					CommonUtil.dialogExpericence(getActivity());
				}else {
					Intent intent = new Intent(getActivity(), ShawnCityActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				break;
		}
	}

}
