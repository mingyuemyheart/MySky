package com.cxwl.weather.eye.activity;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.MyPagerAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.fragment.ShawnMainListFragment;
import com.cxwl.weather.eye.fragment.ShawnMainMapFragment;
import com.cxwl.weather.eye.manager.DataCleanManager;
import com.cxwl.weather.eye.utils.AutoUpdateUtil;
import com.cxwl.weather.eye.utils.CommonUtil;
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.cxwl.weather.eye.view.MainViewPager;
import com.umeng.socialize.UMShareAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ShawnMainActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private ImageView ivControl;
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();

    //侧拉页面
    private DrawerLayout drawerlayout;
    private RelativeLayout reLeft;
    private TextView tvUserType,tvCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_main);
		mContext = this;
		MyApplication.addDestoryActivity(this, "ShawnMainActivity");
        OkHttpExperienceTime();
		initWidget();
		initViewPager();
	}

	private void initWidget() {
		AutoUpdateUtil.checkUpdate(this, mContext, "108", getString(R.string.app_name), true);

		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.app_name));
		ivControl = findViewById(R.id.ivControl);
		ivControl.setOnClickListener(this);
		ivControl.setVisibility(View.VISIBLE);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ImageView ivBack = findViewById(R.id.ivBack);
		ivBack.setImageResource(R.drawable.shawn_icon_menu);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		int width = dm.widthPixels;

        //侧拉页面
        drawerlayout = findViewById(R.id.drawerlayout);
        drawerlayout.setVisibility(View.VISIBLE);
        reLeft = findViewById(R.id.reLeft);
        LayoutParams params1 = reLeft.getLayoutParams();
        params1.width = width-(int) CommonUtil.dip2px(mContext, 50);
        reLeft.setLayoutParams(params1);
        RelativeLayout rePortrait = findViewById(R.id.rePortrait);
        rePortrait.setOnClickListener(this);
        LinearLayout llFacility = findViewById(R.id.llFacility);
        llFacility.setOnClickListener(this);
        LinearLayout llMember = findViewById(R.id.llMember);
        llMember.setOnClickListener(this);
        tvCache = findViewById(R.id.tvCache);
        getCache();
        LinearLayout llClearCache = findViewById(R.id.llClearCache);
        llClearCache.setOnClickListener(this);
        LinearLayout llVersion = findViewById(R.id.llVersion);
        llVersion.setOnClickListener(this);
        TextView tvVersion = findViewById(R.id.tvVersion);
        tvVersion.setText(CommonUtil.getVersion(mContext));
        LinearLayout llAbout = findViewById(R.id.llAbout);
        llAbout.setOnClickListener(this);
        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText(MyApplication.USERNAME);
        tvUserType = findViewById(R.id.tvUserType);
        if (TextUtils.equals(MyApplication.USERTYPE, "1")) {//决策用户
            tvUserType.setText("决策用户");
            tvUserType.setTextColor(0xff0f0b08);
            tvUserType.setBackgroundResource(R.drawable.eye_corner_user_decision);
        }else {
            tvUserType.setText("普通用户");
            tvUserType.setTextColor(0xff5f6c82);
            tvUserType.setBackgroundResource(R.drawable.eye_corner_user_normal);
        }
        OkHttpUserType();

        if (CommonUtil.showExperienceTime(mContext)) {
            CommonUtil.dialogExpericence(mContext);
        }
	}

	private void OkHttpUserType() {
	    final String url = "https://api.bluepi.tianqi.cn/outdata/other/tjCheckRegister?phone="+MyApplication.USERNAME;
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
                                        if (!obj.isNull("success")) {
                                            String success = obj.getString("success");
                                            if (TextUtils.equals(success, "true")) {
                                                tvUserType.setText("会员用户");
                                                tvUserType.setTextColor(0xff0f0b08);
                                                tvUserType.setBackgroundResource(R.drawable.eye_corner_user_member);
                                            }
                                        }
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
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		Fragment fragment1 = new ShawnMainListFragment();
		fragments.add(fragment1);
		Fragment fragment2 = new ShawnMainMapFragment();
		fragments.add(fragment2);
			
		viewPager = findViewById(R.id.viewPager);
		MyPagerAdapter pagerAdapter = new MyPagerAdapter(this, fragments);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setSlipping(false);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
	}

    private void getCache() {
        try {
            tvCache.setText(DataCleanManager.getCacheSize(mContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除对话框
     * @param message 标题
     * @param content 内容
     */
    private void dialogClearCache(String message, String content) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_dialog_delete, null);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView tvContent = view.findViewById(R.id.tvContent);
        TextView tvNegtive = view.findViewById(R.id.tvNegtive);
        TextView tvPositive = view.findViewById(R.id.tvPositive);

        final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvMessage.setText(message);
        tvContent.setText(content);
        tvContent.setVisibility(View.VISIBLE);
        tvNegtive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        tvPositive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                DataCleanManager.clearCache(mContext);
                getCache();
            }
        });
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerlayout != null) {
                if (drawerlayout.isDrawerOpen(reLeft)) {
                    drawerlayout.closeDrawer(reLeft);
                }else {
                    if ((System.currentTimeMillis() - mExitTime) > 2000) {
                        Toast.makeText(mContext, "再按一次退出"+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
                        mExitTime = System.currentTimeMillis();
                    } else {
                        finish();
                    }
                }
            }else {
                finish();
            }
        }
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
            case R.id.llBack:
                if (drawerlayout.isDrawerOpen(reLeft)) {
                    drawerlayout.closeDrawer(reLeft);
                }else {
                    drawerlayout.openDrawer(reLeft);
                }
                break;
            case R.id.ivControl:
                if (viewPager != null) {
                    if (viewPager.getCurrentItem() == 0) {
                        ivControl.setImageResource(R.drawable.shawn_icon_switch_list);
                        viewPager.setCurrentItem(1, true);
                    }else {
                        ivControl.setImageResource(R.drawable.shawn_icon_switch_map);
                        viewPager.setCurrentItem(0, true);
                    }
                }
                break;

			//侧拉页面
            case R.id.rePortrait:
                startActivity(new Intent(mContext, ShawnSettingActivity.class));
                break;
            case R.id.llFacility:
                startActivity(new Intent(mContext, ShawnFacilityActivity.class));
                break;
            case R.id.llMember:
                startActivity(new Intent(mContext, ShawnMemberActivity.class));
                break;
            case R.id.llClearCache:
                dialogClearCache("清除缓存", "确定要清除缓存？");
                break;
            case R.id.llVersion:
                AutoUpdateUtil.checkUpdate(this, mContext, "108", getString(R.string.app_name), false);
                break;
            case R.id.llAbout:
                startActivity(new Intent(mContext, ShawnAboutActivity.class));
                break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
	}

    /**
     * 获取体验时间
     */
	private void OkHttpExperienceTime() {
        final String url = "http://decision-admin.tianqi.cn/home/api/experienceTime";
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
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject obj = new JSONObject(result);
                                if (!obj.isNull("total")) {
                                    long total = obj.getLong("total");
                                    CONST.EXPERIENCETIME = CONST.min*total;
                                    long refresh = obj.getLong("refresh");
                                    CONST.EXPERIENCEREFRESH = CONST.min*refresh;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

}
