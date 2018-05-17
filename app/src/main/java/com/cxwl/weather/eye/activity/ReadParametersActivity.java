package com.cxwl.weather.eye.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.MyPagerAdapter;
import com.cxwl.weather.eye.fragment.ReadParamentersFragment1;
import com.cxwl.weather.eye.fragment.ReadParamentersFragment2;
import com.cxwl.weather.eye.fragment.ReadParamentersFragment3;
import com.cxwl.weather.eye.fragment.ReadParamentersFragment4;
import com.cxwl.weather.eye.fragment.ReadParamentersFragment5;
import com.cxwl.weather.eye.fragment.ReadParamentersFragment6;
import com.cxwl.weather.eye.view.MainViewPager;

/**
 * 摄像头参数展示
 * @author shawn_sun
 *
 */

public class ReadParametersActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MainViewPager viewPager = null;
	private MyPagerAdapter pagerAdapter = null;
	private List<Fragment> fragments = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_parameters);
		initWidget();
		initViewPager();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("参数读取");
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		String result = getIntent().getStringExtra("result");
		Bundle bundle = new Bundle();
		bundle.putString("result", result);
		
		Fragment fragment1 = new ReadParamentersFragment1();
		fragment1.setArguments(bundle);
		fragments.add(fragment1);
		Fragment fragment2 = new ReadParamentersFragment2();
		fragment2.setArguments(bundle);
		fragments.add(fragment2);
		Fragment fragment3 = new ReadParamentersFragment3();
		fragment3.setArguments(bundle);
		fragments.add(fragment3);
		Fragment fragment4 = new ReadParamentersFragment4();
		fragment4.setArguments(bundle);
		fragments.add(fragment4);
		Fragment fragment5 = new ReadParamentersFragment5();
		fragment5.setArguments(bundle);
		fragments.add(fragment5);
		Fragment fragment6 = new ReadParamentersFragment6();
		fragment6.setArguments(bundle);
		fragments.add(fragment6);
			
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		pagerAdapter = new MyPagerAdapter(ReadParametersActivity.this, fragments);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
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
