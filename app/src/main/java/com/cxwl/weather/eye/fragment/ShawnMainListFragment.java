package com.cxwl.weather.eye.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.activity.ShawnCityActivity;
import com.cxwl.weather.eye.activity.ShawnVideoDetailActivity;
import com.cxwl.weather.eye.activity.ShawnWebviewActivity;
import com.cxwl.weather.eye.adapter.ShawnMainListAdapter;
import com.cxwl.weather.eye.adapter.ShawnRecmmendAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.ScrollviewListview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.shawn_fragment_main_list, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initGridView(view);
		initListView(view);
	}

	private void initWidget(View view) {
		tvAll = view.findViewById(R.id.tvAll);
		tvAll.setOnClickListener(this);
		scrollView = view.findViewById(R.id.scrollView);
		ImageView ivBanner = view.findViewById(R.id.ivBanner);
		ivBanner.setOnClickListener(this);

		OkHttpTJImgs();
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
						getListData();
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							getListData();
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

						getListData();

					}
				});
			}
		}).start();
	}

	private void getListData() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dataList.clear();
				dataList.addAll(getArguments().<EyeDto>getParcelableArrayList("dataList"));
				for (EyeDto dto : dataList) {
					if (!TextUtils.isEmpty(dto.facilityUrlTes) && imgMap.containsKey(dto.facilityUrlTes)) {
						dto.videoThumbUrl = imgMap.get(dto.facilityUrlTes);
					}
				}

				List<EyeDto> tempList = new ArrayList<>();
				tempList.addAll(dataList);

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
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivBanner:
				Intent intent = new Intent(getActivity(), ShawnWebviewActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, "积水查询");
				intent.putExtra(CONST.WEB_URL, "http://tianjin.welife100.com/Monitor/monitor");
				startActivity(intent);
				break;
			case R.id.tvAll:
				if (CommonUtil.showExperienceTime(getActivity())) {
					CommonUtil.dialogExpericence(getActivity());
				}else {
					intent = new Intent(getActivity(), ShawnCityActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				break;
		}
	}

}
