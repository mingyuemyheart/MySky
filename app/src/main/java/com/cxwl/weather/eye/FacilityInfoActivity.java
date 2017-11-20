package com.cxwl.weather.eye;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.adapter.FacilityInfoAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CustomHttpClient;
import com.cxwl.weather.eye.view.RefreshLayout;
import com.cxwl.weather.eye.view.RefreshLayout.OnLoadListener;
import com.cxwl.weather.eye.view.RefreshLayout.OnRefreshListener;

/**
 * 设备设置
 * @author shawn_sun
 *
 */

public class FacilityInfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView listView = null;
	private FacilityInfoAdapter mAdapter = null;
	private List<EyeDto> facilityList = new ArrayList<EyeDto>();
	private int page = 1;
	private int pageCount = 20;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private LinearLayout llSelect = null;
	private LinearLayout llBottom = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facilityinfo);
		mContext = this;
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
				asyncQuery("https://tqwy.tianqi.cn/tianqixy/userInfo/selfacility");
			}
		});
	}
	
	private void refresh() {
		page = 1;
		facilityList.clear();
		asyncQuery("https://tqwy.tianqi.cn/tianqixy/userInfo/selfacility");
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("设备信息");
		llSelect = (LinearLayout) findViewById(R.id.llSelect);
		llBottom = (LinearLayout) findViewById(R.id.llBottom);
		
		refresh();
	}
	
	private void initListView() {
		listView = (ListView) findViewById(R.id.listView);
		mAdapter = new FacilityInfoAdapter(mContext, facilityList);
		listView.setAdapter(mAdapter);
//		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				if (!TextUtils.equals(CONST.AUTHORITY, CONST.COMMON)) {
//					llSelect.setVisibility(View.VISIBLE);
//					llBottom.setVisibility(View.VISIBLE);
//				}
//				return false;
//			}
//		});
	}
	
	/**
	 * 获取视频列表
	 */
	private void asyncQuery(String requestUrl) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("intpage", page+"");
	        NameValuePair pair2 = new BasicNameValuePair("pagerow", pageCount+"");
	        
			nvpList.add(pair1);
			nvpList.add(pair2);
		}
		
		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			refreshLayout.setRefreshing(false);
			refreshLayout.setLoading(false);
			cancelDialog();
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
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
										if (!itemObj.isNull("Fzname")) {
											dto.fGroupName = itemObj.getString("Fzname");
										}
										if (!itemObj.isNull("Location")) {
											dto.location = itemObj.getString("Location");
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
										facilityList.add(dto);
									}
									if (mAdapter != null) {
										mAdapter.notifyDataSetChanged();
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
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
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
