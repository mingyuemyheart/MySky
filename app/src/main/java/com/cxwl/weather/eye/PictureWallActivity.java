package com.cxwl.weather.eye;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.tsz.afinal.FinalBitmap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.adapter.PictureViewPagerAdapter;
import com.cxwl.weather.eye.adapter.PictureWallAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CustomHttpClient;
import com.cxwl.weather.eye.view.MyPhotoView;
import com.cxwl.weather.eye.view.RefreshLayout;
import com.cxwl.weather.eye.view.RefreshLayout.OnRefreshListener;

/**
 * 图片墙
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class PictureWallActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private EyeDto data = null;
	private TextView tvTitle = null;
	private LinearLayout llBack = null;
	private ImageView ivBack = null;
	private TextView tvTime = null;
	private ImageView imageView = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
	private GridView gridView = null;
	private PictureWallAdapter picAdapter = null;
	private List<EyeDto> picList = new ArrayList<EyeDto>();
	private ViewPager viewPager = null;
	private PictureViewPagerAdapter pagerAdapter = null;
	private ImageView[] imageArray = null;//装载图片的数组
	private RefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picturewall);
		mContext = this;
		showDialog();
		initRefreshLayout();
		initWidget();
		initGridView();
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
	}
	
	private void refresh() {
		data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			asyncQuery("https://tqwy.tianqi.cn/tianqixy/userInfo/getpngs");
		}
	}
	
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("图集展示");
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.eye_btn_close);
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setOnClickListener(this);
		tvTime = (TextView) findViewById(R.id.tvTime);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		LayoutParams params = imageView.getLayoutParams();
		params.width = dm.widthPixels;
		params.height = dm.widthPixels*9/16;
		imageView.setLayoutParams(params);
		
		refresh();
	}
	
	private void initGridView() {
		gridView = (GridView) findViewById(R.id.gridView);
		picAdapter = new PictureWallAdapter(mContext, picList);
		gridView.setAdapter(picAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				previewViewPager(arg2+1);
			}
		});
		gridView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				boolean enable = false;
				if (gridView != null && gridView.getChildCount() / 3 > 0) {
					// check if the first item of the list is visible
					boolean firstItemVisible = gridView.getFirstVisiblePosition() == 0;
					// check if the top of the first item is visible
					boolean topOfFirstItemVisible = gridView.getChildAt(0).getTop() == 0;
					// enabling or disabling the refresh layout
					enable = firstItemVisible && topOfFirstItemVisible;
				}
				if (refreshLayout != null) {
					refreshLayout.setEnabled(enable);
				}
			}
		});
	}
	
	/**
	 * 预览图片
	 * @param index 选中图片的下标
	 */
	private void previewViewPager(int index) {
		if (viewPager != null) {
			fullScreen(true);
			viewPager.setCurrentItem(index, false);
			viewPager.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager(final List<EyeDto> list) {
		if (list.size() <= 0) {
			return;
		}
		imageArray = new ImageView[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ImageView image = new ImageView(mContext);
			image.setScaleType(ScaleType.CENTER_CROP);
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(image, list.get(i).pictureUrl, null, 0);
			imageArray[i] = image;
		}
		
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		pagerAdapter = new PictureViewPagerAdapter(imageArray);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				for (int i = 0; i < list.size(); i++) {
					View childAt = viewPager.getChildAt(i);
                    try {
                        if (childAt != null && childAt instanceof MyPhotoView) {
                        	MyPhotoView  photoView = (MyPhotoView) childAt;//得到viewPager里面的页面
                        	PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);//把得到的photoView放到这个负责变形的类当中
                            mAttacher.getDisplayMatrix().reset();//得到这个页面的显示状态，然后重置为默认状态
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/**
	 * 异步请求
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
			NameValuePair pair1 = new BasicNameValuePair("fid", data.fId);
	        
			nvpList.add(pair1);
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
			cancelDialog();
			refreshLayout.setRefreshing(false);
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("code")) {
							String code  = object.getString("code");
							if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
								if (!object.isNull("small")) {
									JSONArray array = object.getJSONArray("small");
									picList.clear();
									for (int i = 0; i < array.length(); i++) {
										String imgUrl = array.getString(i);
										if (!TextUtils.isEmpty(imgUrl) && imgUrl.contains(".png")) {
											String time = imgUrl.substring(imgUrl.length()-14, imgUrl.length()-4);
											
											EyeDto dto = new EyeDto();
											dto.pictureThumbUrl = imgUrl;
											dto.pictureTime = time;
											
											if (i == 0) {
												FinalBitmap finalBitmap = FinalBitmap.create(mContext);
												finalBitmap.display(imageView, imgUrl, null, 0);
												
												if (!TextUtils.isEmpty(time)) {
													tvTime.setText(sdf.format(new Date(Long.valueOf(time)*1000)));
												}
											}else {
												picList.add(dto);
											}
										}
									}
									if (picAdapter != null) {
										picAdapter.notifyDataSetChanged();
									}
								}
								if (!object.isNull("list")) {
									JSONArray array = object.getJSONArray("list");
									List<EyeDto> pagerList = new ArrayList<EyeDto>();
									pagerList.clear();
									for (int i = 0; i < array.length(); i++) {
										String imgUrl = array.getString(i);
										if (!TextUtils.isEmpty(imgUrl) && imgUrl.contains(".png")) {
											String time = imgUrl.substring(imgUrl.length()-14, imgUrl.length()-4);
											
											EyeDto dto = new EyeDto();
											dto.pictureUrl = imgUrl;
											dto.pictureTime = time;
											pagerList.add(dto);
										}
									}
									
									initViewPager(pagerList);
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
	
	/**
	 * 是否全屏显示
	 * @param enable
	 */
	private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (viewPager != null && viewPager.getVisibility() == View.VISIBLE) {
				fullScreen(false);
				viewPager.setVisibility(View.GONE);
			}else {
				finish();
		        overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
	        overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
			break;
		case R.id.imageView:
			previewViewPager(0);
			break;

		default:
			break;
		}
	}
	
}
