package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.PictureWallAdapter;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 图片墙
 */
public class PictureWallActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private EyeDto data;
	private TextView tvTime;
	private ImageView imageView;
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.CHINA);
	private GridView gridView;
	private PictureWallAdapter picAdapter;
	private List<EyeDto> picList = new ArrayList<>();

	private ViewPager mViewPager;
	private ImageView[] ivTips;//装载点的数组
	private ViewGroup viewGroup;
	private RelativeLayout rePager;
	private List<String> urlList = new ArrayList<>();//存放图片的list
	private int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picturewall);
		mContext = this;
		showDialog();
		initWidget();
		initGridView();
	}
	
	private void refresh() {
		data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			OkHttpList();
		}
	}
	
	private void initWidget() {
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("图集展示");
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ImageView ivBack = findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.eye_btn_close);
		imageView = findViewById(R.id.imageView);
		imageView.setOnClickListener(this);
		imageView.requestFocus();
		imageView.setFocusable(true);
		imageView.setFocusableInTouchMode(true);
		tvTime = findViewById(R.id.tvTime);
		rePager = findViewById(R.id.rePager);
		viewGroup = findViewById(R.id.viewGroup);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dm.widthPixels, dm.widthPixels*9/16);
		imageView.setLayoutParams(params);

		refresh();
	}
	
	private void initGridView() {
		gridView = findViewById(R.id.gridView);
		picAdapter = new PictureWallAdapter(mContext, picList);
		gridView.setAdapter(picAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (rePager.getVisibility() == View.GONE) {
					if (mViewPager != null) {
						mViewPager.setCurrentItem(arg2+1);
					}
					scaleExpandAnimation(rePager, arg2+1);
					rePager.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		ImageView[] imageArray = new ImageView[urlList.size()];
		for (int i = 0; i < urlList.size(); i++) {
			ImageView image = new ImageView(mContext);
			Picasso.get().load(urlList.get(i)).into(image);
			imageArray[i] = image;
		}

		ivTips = new ImageView[urlList.size()];
		viewGroup.removeAllViews();
		for (int i = 0; i < urlList.size(); i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new ViewGroup.LayoutParams(5, 5));
			ivTips[i] = imageView;
			if(i == 0){
				ivTips[i].setBackgroundResource(R.drawable.point_white);
			}else{
				ivTips[i].setBackgroundResource(R.drawable.point_gray);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			viewGroup.addView(imageView, layoutParams);
		}

		mViewPager = findViewById(R.id.viewPager);
		MyViewPagerAdapter pagerAdapter = new MyViewPagerAdapter(imageArray);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				index = arg0;
				for (int i = 0; i < urlList.size(); i++) {
					if(i == arg0){
						ivTips[i].setBackgroundResource(R.drawable.point_white);
					}else{
						ivTips[i].setBackgroundResource(R.drawable.point_gray);
					}

//					View childAt = mViewPager.getChildAt(i);
//                    try {
//                        if (childAt != null && childAt instanceof PhotoView) {
//                        	PhotoView  photoView = (PhotoView) childAt;//得到viewPager里面的页面
//                        	PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);//把得到的photoView放到这个负责变形的类当中
//                            mAttacher.getDisplayMatrix().reset();//得到这个页面的显示状态，然后重置为默认状态
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	private class MyViewPagerAdapter extends PagerAdapter {

		private ImageView[] mImageViews;

		public MyViewPagerAdapter(ImageView[] imageViews) {
			this.mImageViews = imageViews;
		}

		@Override
		public int getCount() {
			return mImageViews.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mImageViews[position]);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			Drawable drawable = mImageViews[position].getDrawable();
			photoView.setImageDrawable(drawable);
			container.addView(photoView, 0);
			photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
				@Override
				public void onPhotoTap(View view, float v, float v1) {
					scaleColloseAnimation(rePager, index);
					rePager.setVisibility(View.GONE);
				}
			});
			return photoView;
		}

	}

	/**
	 * 放大动画
	 * @param view
	 */
	private void scaleExpandAnimation(View view, int index) {
		AnimationSet animationSet = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
				Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
//		if (index == 0) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 1) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 2) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 3) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 4) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.2f);
//		}else if (index == 5) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.2f);
//		}else if (index == 6) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.2f);
//		}else if (index == 7) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.4f);
//		}else if (index == 8) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.4f);
//		}else if (index == 9) {
//			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
//					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.4f);
//		}
		scaleAnimation.setInterpolator(new LinearInterpolator());
		scaleAnimation.setDuration(300);
		animationSet.addAnimation(scaleAnimation);

		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1.0f);
		alphaAnimation.setDuration(300);
		animationSet.addAnimation(alphaAnimation);

		view.startAnimation(animationSet);
	}

	/**
	 * 缩小动画
	 * @param view
	 */
	private void scaleColloseAnimation(View view, int index) {
		AnimationSet animationSet = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
				Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
//		if (index == 0) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 1) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 2) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 3) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.0f);
//		}else if (index == 4) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.2f);
//		}else if (index == 5) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.2f);
//		}else if (index == 6) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.2f);
//		}else if (index == 7) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.4f);
//		}else if (index == 8) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.4f);
//		}else if (index == 9) {
//			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.4f);
//		}
		scaleAnimation.setInterpolator(new LinearInterpolator());
		scaleAnimation.setDuration(300);
		animationSet.addAnimation(scaleAnimation);

		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
		alphaAnimation.setDuration(300);
		animationSet.addAnimation(alphaAnimation);

		view.startAnimation(animationSet);
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpList() {
		if (TextUtils.isEmpty(data.fId)) {
			return;
		}
		final String url = "https://tqwy.tianqi.cn/tianqixy/userInfo/getpngs";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("fid", data.fId);
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
										if (!object.isNull("code")) {
											String code  = object.getString("code");
											if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) {//成功
												if (!object.isNull("small")) {//缩略图
													JSONArray array = object.getJSONArray("small");
													picList.clear();
													for (int i = 0; i < array.length(); i++) {
														String imgUrl = array.getString(i);
														if (!TextUtils.isEmpty(imgUrl)) {
															String time = imgUrl.substring(imgUrl.length()-14, imgUrl.length()-4);

															EyeDto dto = new EyeDto();
															dto.pictureThumbUrl = imgUrl;
															dto.pictureTime = time;

															if (i == 0) {
																Picasso.get().load(imgUrl).into(imageView);
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
														if (gridView != null) {
															CommonUtil.setGridViewHeightBasedOnChildren(gridView);
														}
													}
												}

												if (!object.isNull("list")) {//原图
													JSONArray array = object.getJSONArray("list");
													urlList.clear();
													for (int i = 0; i < array.length(); i++) {
														String imgUrl = array.getString(i);
														if (!TextUtils.isEmpty(imgUrl)) {
															urlList.add(imgUrl);
														}
													}
													initViewPager();
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
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								cancelDialog();
							}
						});
					}
				});
			}
		}).start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (rePager.getVisibility() == View.VISIBLE) {
			scaleColloseAnimation(rePager, index);
			rePager.setVisibility(View.GONE);
			return false;
		}else {
			finish();
			overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (rePager.getVisibility() == View.VISIBLE) {
				scaleColloseAnimation(rePager, index);
				rePager.setVisibility(View.GONE);
			}else {
				finish();
				overridePendingTransition(R.anim.in_uptodown, R.anim.out_uptodown);
			}
			break;
		case R.id.imageView:
			if (rePager.getVisibility() == View.GONE) {
				if (mViewPager != null) {
					mViewPager.setCurrentItem(0);
				}
				scaleExpandAnimation(rePager, 0);
				rePager.setVisibility(View.VISIBLE);
			}
			break;

		default:
			break;
		}
	}
	
}
