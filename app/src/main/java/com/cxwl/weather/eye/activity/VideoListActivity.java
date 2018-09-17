package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
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

/**
 * 视频列表
 */
public class VideoListActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private TextView tvTitle = null;
	private LinearLayout llBack = null;
	private ListView listView = null;
	private VideoListAdapter videoAdapter = null;
	private List<EyeDto> videoList = new ArrayList<>();
	private int page = 1;
	private int pageCount = 20;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String baseUrl = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videolist);
		mContext = this;
		CONST.addDestoryActivity(VideoListActivity.this, "VideoListActivity");
		showDialog();
		initRefreshLayout();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
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
	
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		
		if (getIntent().hasExtra("groupName")) {
			String groupName = getIntent().getStringExtra("groupName");
			if (!TextUtils.isEmpty(groupName)) {
				tvTitle.setText(groupName);
			}
		}else {
			tvTitle.setText(getString(R.string.app_name));
		}
		
		if (getIntent().hasExtra("groupId")) {//管理员获取组，然后通过设备组id查询设备
			String groupId = getIntent().getStringExtra("groupId");
			if (!TextUtils.isEmpty(groupId)) {
				baseUrl = "https://tqwy.tianqi.cn/tianqixy/userInfo/selectlistzu";
			}
		}else {
			baseUrl = "https://tqwy.tianqi.cn/tianqixy/userInfo/selectlist";
		}
		refresh();
	}
	
	private void initListView() {
		listView = (ListView) findViewById(R.id.listView);
		videoAdapter = new VideoListAdapter(this, videoList);
		listView.setAdapter(videoAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				EyeDto dto = videoList.get(arg2);
				Intent intent = new Intent(mContext, VideoDetailActivity.class);
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
		if (getIntent().hasExtra("groupId")) {//管理员获取组，然后通过设备组id查询设备
			String groupId = getIntent().getStringExtra("groupId");
			if (!TextUtils.isEmpty(groupId)) {
				builder.add("FZID", groupId);
			}
		}
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
															Toast.makeText(mContext, reason, Toast.LENGTH_SHORT).show();
														}
													}
												}
											}
										}
										refreshLayout.setRefreshing(false);
										refreshLayout.setLoading(false);
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
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
