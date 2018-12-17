package com.cxwl.weather.eye.activity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.MyPagerAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.fragment.ShawnMainMapFragment;
import com.cxwl.weather.eye.fragment.ShawnMainListFragment;
import com.cxwl.weather.eye.utils.AutoUpdateUtil;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.view.MainViewPager;
import com.cxwl.weather.eye.view.SemiMenuView;
import com.cxwl.weather.eye.view.SemiMenuView.FinishListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 天气网眼
 */
public class ShawnMainActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private ImageView ivControl;
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private LinearLayout llBack,llContainer;
	private SemiMenuView semiMenuView;
	private MyBroadCastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_main);
		mContext = this;
		MyApplication.addDestoryActivity(this, "ShawnMainActivity");
		initBroadCastReceiver();
		initWidget();
		initViewPager();
	}
	
	private void initBroadCastReceiver() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CONST.SIME_CIRCLE_CONTROLER);
		registerReceiver(mReceiver, intentFilter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}
	
	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (TextUtils.equals(intent.getAction(), CONST.SIME_CIRCLE_CONTROLER)) {//接收半圆形控制器指令
				rotateMenu(llBack, -360);
				semiMenuView.boostThread(false);
				
				Bundle bundle = intent.getExtras();
				int selectId = bundle.getInt("selectId");
//				if (TextUtils.equals(CONST.AUTHORITY, CONST.COMMON)) {//普通用户
//					if (selectId >= 6 && selectId <= 8) {//设置
//						startActivity(new Intent(mContext, ShawnSettingActivity.class));
//					}else if (selectId >= 10 && selectId <= 12) {//分享
//						Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show();
//					}else if (selectId >= 16 && selectId <= 18) {//关于
//						startActivity(new Intent(mContext, ShawnAboutActivity.class));
//					}else if (selectId >= 22 && selectId <= 24) {//个人
//						Toast.makeText(mContext, "个人", Toast.LENGTH_SHORT).show();
//					}
//				}else {
					if (selectId >= 5 && selectId <= 7) {//设置
						startActivity(new Intent(mContext, ShawnSettingActivity.class));
					}else if (selectId >= 9 && selectId <= 11) {//分享
						CommonUtil.share(ShawnMainActivity.this);
					}else if (selectId >= 13 && selectId <= 15) {//关于
						startActivity(new Intent(mContext, ShawnAboutActivity.class));
					}else if (selectId >= 18 && selectId <= 20) {//个人
						startActivity(new Intent(mContext, ShawnUserinfoActivity.class));
					}else if (selectId >= 22 && selectId <= 24) {//设备设置
						startActivity(new Intent(mContext, ShawnFacilityInfoActivity.class));
					}
//				}
			}
		}
	}
	
	/**
	 * 旋转菜单动画
	 * @param layout
	 * @param degree
	 */
	private void rotateMenu(LinearLayout layout, float degree) {
		RotateAnimation rotate = new RotateAnimation(0, degree,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(500);
		rotate.setFillAfter(true);
		layout.startAnimation(rotate);
	}
	
	private void initWidget() {
		AutoUpdateUtil.checkUpdate(this, mContext, "67", getString(R.string.app_name), true);

		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.app_name));
		ivControl = findViewById(R.id.ivControl);
		ivControl.setOnClickListener(this);
		ivControl.setVisibility(View.VISIBLE);
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ImageView ivBack = findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.eye_iv_menu);
		llContainer = findViewById(R.id.llContainer);
		semiMenuView = findViewById(R.id.semiMenuView);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		int width = dm.widthPixels;
		LayoutParams params = semiMenuView.getLayoutParams();
		params.width = width;
		params.height = width;
		semiMenuView.setLayoutParams(params);
		semiMenuView.setRadius(params.width/2);
		semiMenuView.setCenterXY(params.width/2, params.width/2);
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		Fragment fragment1 = new ShawnMainMapFragment();
		fragments.add(fragment1);
		Fragment fragment2 = new ShawnMainListFragment();
//		if (TextUtils.equals(AUTHORITY, CONST.MANAGER)) {
//			fragment2 = new FacilityGroupFragment();
//		}else {
//			fragment2 = new ShawnMainListFragment();
//		}
		fragments.add(fragment2);
			
		viewPager = findViewById(R.id.viewPager);
		MyPagerAdapter pagerAdapter = new MyPagerAdapter(ShawnMainActivity.this, fragments);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setSlipping(false);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (llContainer.getVisibility() == View.VISIBLE) {
				rotateMenu(llBack, -360);
				semiMenuView.boostThread(false);
			}else {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					Toast.makeText(mContext, "再按一次退出"+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
					mExitTime = System.currentTimeMillis();
				} else {
					finish();
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (llContainer.getVisibility() == View.GONE) {
				rotateMenu(llBack, 450);
				semiMenuView.boostThread(true);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						llContainer.setVisibility(View.VISIBLE);
						semiMenuView.setVisibility(View.VISIBLE);
					}
				}, 10);
			}else {
				rotateMenu(llBack, -360);
				semiMenuView.boostThread(false);
			}
			semiMenuView.setFinishListener(new FinishListener() {
				@Override
				public void loadComplete(boolean startDraw) {
					if (startDraw) {
						
					}else {
						semiMenuView.setVisibility(View.GONE);
						llContainer.setVisibility(View.GONE);
					}
				}
			});
			break;
		case R.id.ivControl:
			if (viewPager != null) {
				if (viewPager.getCurrentItem() == 0) {
					ivControl.setImageResource(R.drawable.shawn_icon_switch_map);
					viewPager.setCurrentItem(1, true);
				}else {
					ivControl.setImageResource(R.drawable.shawn_icon_switch_list);
					viewPager.setCurrentItem(0, true);
				}
			}
			break;

		default:
			break;
		}
	}
	
}
