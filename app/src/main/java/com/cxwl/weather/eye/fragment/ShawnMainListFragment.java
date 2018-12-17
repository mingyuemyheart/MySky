package com.cxwl.weather.eye.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.activity.VideoDetailActivity;
import com.cxwl.weather.eye.adapter.VideoListAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.Request;
import okhttp3.Response;

import static com.cxwl.weather.eye.common.MyApplication.USERNAME;

/**
 * 主界面-列表
 */
public class ShawnMainListFragment extends Fragment {
	
	private VideoListAdapter videoAdapter;
	private List<EyeDto> dataList = new ArrayList<>();
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
		initListView(view);
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
        refreshLayout.setProgressViewEndTarget(true, 300);
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
		OkHttpList();
	}
	
	private void initWidget(View view) {
		refresh();
	}
	
	private void initListView(View view) {
		GridView gridView = view.findViewById(R.id.gridView);
		videoAdapter = new VideoListAdapter(getActivity(), dataList);
		gridView.setAdapter(videoAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				EyeDto dto = dataList.get(arg2);
				Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
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
														if (!itemObj.isNull("small")) {
														    dto.videoThumbUrl = itemObj.getString("small");
                                                        }
                                                        if (!itemObj.isNull("FacilityUrlTes")) {
                                                            dto.facilityUrlTes = itemObj.getString("FacilityUrlTes");
                                                        }
														dataList.add(dto);
													}

													if (videoAdapter != null) {
														videoAdapter.notifyDataSetChanged();
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
										refreshLayout.setRefreshing(false);
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
	
}
