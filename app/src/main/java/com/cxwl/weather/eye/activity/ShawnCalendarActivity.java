package com.cxwl.weather.eye.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.MyPagerAdapter;
import com.cxwl.weather.eye.adapter.ShawnCalendarAdapter;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.fragment.ShawnCalendarFragment;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.MainViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 天气日历
 */
public class ShawnCalendarActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
    private ImageView ivControl;
    private RelativeLayout reTitle;
	private Configuration configuration;//方向监听器
    private List<EyeDto> dataList = new ArrayList<>();
    private LinearLayout llContainer1,llContainer2;
    private int itemWidth,itemHeight;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("dd", Locale.CHINA);
    private HorizontalScrollView hScrollView1;
    private MainViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<>();
    private GridView gridView;
    private ShawnCalendarAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_calendar);
		mContext = this;
        initGridView();
        initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
        reTitle = findViewById(R.id.reTitle);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("天气日历");
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivControl = findViewById(R.id.ivControl);
		ivControl.setOnClickListener(this);
		ivControl.setVisibility(View.VISIBLE);
        ivControl.setImageResource(R.drawable.shawn_icon_ore_landscape);
        llContainer1 = findViewById(R.id.llContainer1);
        llContainer2 = findViewById(R.id.llContainer2);
        hScrollView1 = findViewById(R.id.hScrollView1);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        itemWidth = width/7;
        itemHeight = height/7;

		showPort();

		EyeDto data = getIntent().getParcelableExtra("data");
		if (data != null) {
		    if (!TextUtils.isEmpty(data.fNumber)) {
                OkHttpList(data.fNumber);
            }
        }
	}

    private void initGridView() {
        gridView = findViewById(R.id.gridView);
        mAdapter = new ShawnCalendarAdapter(mContext, dataList);
        gridView.setAdapter(mAdapter);
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configuration = newConfig;
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPort();
		}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			showLand();
		}
	}
	
	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
        reTitle.setVisibility(View.VISIBLE);
        hScrollView1.setVisibility(View.VISIBLE);
        if (viewPager != null) {
            viewPager.setVisibility(View.VISIBLE);
        }
        llContainer2.setVisibility(View.GONE);
        gridView.setVisibility(View.GONE);
		fullScreen(false);
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
        reTitle.setVisibility(View.GONE);
        hScrollView1.setVisibility(View.GONE);
        if (viewPager != null) {
            viewPager.setVisibility(View.GONE);
        }
        llContainer2.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.VISIBLE);
		fullScreen(true);
	}
	
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
	
	private void exit() {
		if (configuration == null) {
	        finish();
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
		        finish();
			}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ivControl.setImageResource(R.drawable.shawn_icon_ore_landscape);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				exit();
				break;
			case R.id.ivControl:
				if (configuration == null) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					ivControl.setImageResource(R.drawable.shawn_icon_ore_protrait);
				}else {
					if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        ivControl.setImageResource(R.drawable.shawn_icon_ore_protrait);
					}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        ivControl.setImageResource(R.drawable.shawn_icon_ore_landscape);
					}
				}
				break;

			default:
				break;
			}
	}

    /**
     * 获取天气日历数据
     */
	private void OkHttpList(String fNumber) {
        showDialog();
	    final String url = String.format("https://api.bluepi.tianqi.cn/Outdata/Extra/bluepi_tqwy_30D/FacilityNumber/%s/norType/2", fNumber);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject obj = new JSONObject(result);
                                        if (!obj.isNull("data")) {
                                            dataList.clear();
                                            JSONArray array = obj.getJSONArray("data");
                                            for (int i = 0; i < array.length(); i++) {
                                                EyeDto dto = new EyeDto();
                                                JSONObject itemObj = array.getJSONObject(i);
                                                if (!itemObj.isNull("time")) {
                                                    dto.time = itemObj.getString("time");
                                                }
                                                if (!itemObj.isNull("picUrl")) {
                                                    dto.pictureUrl = itemObj.getString("picUrl");
                                                }
                                                dataList.add(dto);
                                            }

                                            initViewPager();
                                            addLandscapeWeek();
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

    /**
     * 初始化viewPager
     */
    private void initViewPager() {
        llContainer1.removeAllViews();
        for (int i = 0; i < dataList.size(); i++) {
            EyeDto dto = dataList.get(i);

            final LinearLayout llItem = new LinearLayout(mContext);
            llItem.setOrientation(LinearLayout.VERTICAL);
            llItem.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
            llItem.setTag(i);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.width = itemWidth;
            params.setMargins(0, (int)CommonUtil.dip2px(mContext, 10), 0, (int)CommonUtil.dip2px(mContext, 10));

            TextView tvWeek = new TextView(mContext);
            tvWeek.setGravity(Gravity.CENTER);
            tvWeek.setTextColor(Color.WHITE);
            tvWeek.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            if (!TextUtils.isEmpty(dto.time)) {
                tvWeek.setText(CommonUtil.getWeek(dto.time));
            }else {
                tvWeek.setText("");
            }
            tvWeek.setLayoutParams(params);
            llItem.addView(tvWeek);

            TextView tvDate = new TextView(mContext);
            tvDate.setGravity(Gravity.CENTER);
            tvDate.setTextColor(Color.WHITE);
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            if (!TextUtils.isEmpty(dto.time)) {
                try {
                    tvDate.setText(sdf2.format(sdf1.parse(dto.time)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else {
                tvDate.setText("");
            }
            tvDate.setPadding((int)CommonUtil.dip2px(mContext, 3), (int)CommonUtil.dip2px(mContext, 1), (int)CommonUtil.dip2px(mContext, 3), (int)CommonUtil.dip2px(mContext, 1));
            if (i == 0) {
                tvDate.setBackgroundResource(R.drawable.shawn_bg_corner_red);
            }else {
                tvDate.setBackgroundColor(Color.TRANSPARENT);
            }
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.setMargins(0, 0, 0, 0);
            tvDate.setLayoutParams(params1);
            llItem.addView(tvDate);

            if (tvWeek.getText().toString().contains("六") || tvWeek.getText().toString().contains("日")) {
                tvWeek.setTextColor(getResources().getColor(R.color.text_color4));
                tvDate.setTextColor(getResources().getColor(R.color.text_color4));
            }else {
                tvWeek.setTextColor(Color.WHITE);
                tvDate.setTextColor(Color.WHITE);
            }

            llContainer1.addView(llItem);

            llItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int tag = (int)v.getTag();
                    setColumn(tag);
                }
            });

            Fragment fragment = new ShawnCalendarFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("data", dto);
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }

        viewPager = findViewById(R.id.viewPager);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(this, fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setSlipping(true);//设置ViewPager是否可以滑动
        viewPager.setOffscreenPageLimit(fragments.size());
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
            setColumn(arg0);
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    /**
     * 点击日历或滑动图片
     * @param tag
     */
    private void setColumn(final int tag) {
        for (int j = 0; j < llContainer1.getChildCount(); j++) {
            LinearLayout llItem = (LinearLayout) llContainer1.getChildAt(j);
            TextView tvDate = (TextView) llItem.getChildAt(1);
            if (tag == (int)llItem.getTag()) {
                tvDate.setBackgroundResource(R.drawable.shawn_bg_corner_red);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        hScrollView1.smoothScrollTo(itemWidth*(tag-3), 0);
                    }
                });
                if (viewPager != null) {
                    viewPager.setCurrentItem(tag, true);
                }
            }else {
                tvDate.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private void addLandscapeWeek() {
        llContainer2.removeAllViews();
        for (int i = 0; i < 7; i++) {
            EyeDto dto = dataList.get(i);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.width = itemHeight;
            params.setMargins(0, (int)CommonUtil.dip2px(mContext, 10), 0, (int)CommonUtil.dip2px(mContext, 10));

            TextView tvWeek = new TextView(mContext);
            tvWeek.setGravity(Gravity.CENTER);
            tvWeek.setTextColor(Color.WHITE);
            tvWeek.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            if (!TextUtils.isEmpty(dto.time)) {
                tvWeek.setText(CommonUtil.getWeek(dto.time));
            }else {
                tvWeek.setText("");
            }
            tvWeek.setLayoutParams(params);
            llContainer2.addView(tvWeek);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

}
