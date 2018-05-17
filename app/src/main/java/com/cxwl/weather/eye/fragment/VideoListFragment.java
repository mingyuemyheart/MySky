package com.cxwl.weather.eye.fragment;

/**
 * 视频列表
 */

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.activity.VideoDetailActivity;
import com.cxwl.weather.eye.adapter.VideoListAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.RefreshLayout;
import com.cxwl.weather.eye.view.RefreshLayout.OnLoadListener;
import com.cxwl.weather.eye.view.RefreshLayout.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoListFragment extends Fragment {
	
	private ListView listView = null;
	private VideoListAdapter videoAdapter = null;
	private List<EyeDto> videoList = new ArrayList<>();
	private int page = 1;
	private int pageCount = 20;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String baseUrl = "https://tqwy.tianqi.cn/tianqixy/userInfo/selectlist";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_videolist, null);
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
		refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
		refreshLayout.setColor(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.BOTH);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
		refreshLayout.setOnLoadListener(new OnLoadListener() {
			@Override
			public void onLoad() {
				page++;
				OkHttpList(baseUrl);
			}
		});
	}
	
	private void refresh() {
		page = 1;
		videoList.clear();
		OkHttpList(baseUrl);
	}
	
	private void initWidget(View view) {
		refresh();
	}
	
	private void initListView(View view) {
		listView = (ListView) view.findViewById(R.id.listView);
		videoAdapter = new VideoListAdapter(getActivity(), videoList);
		listView.setAdapter(videoAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				EyeDto dto = videoList.get(arg2);
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
	private void OkHttpList(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("intpage", page+"");
		builder.add("pagerow", pageCount+"");
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
															if (!itemObj.isNull("ErectTime")) {
																dto.erectTime = itemObj.getString("ErectTime");
															}
															if (!itemObj.isNull("FacilityUrlWithin")) {
																dto.streamPrivate = itemObj.getString("FacilityUrlWithin");
															}
															if (!itemObj.isNull("FacilityUrl")) {
																dto.streamPublic = itemObj.getString("FacilityUrl");
															}
															if (!itemObj.isNull("small")) {
																dto.videoThumbUrl = itemObj.getString("small");
															}
															videoList.add(dto);
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
										}
										refreshLayout.setRefreshing(false);
										refreshLayout.setLoading(false);
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
