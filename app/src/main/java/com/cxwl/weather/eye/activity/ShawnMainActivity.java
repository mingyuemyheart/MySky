package com.cxwl.weather.eye.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.MyPagerAdapter;
import com.cxwl.weather.eye.common.CONST;
import com.cxwl.weather.eye.common.MyApplication;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.fragment.ShawnMainListFragment;
import com.cxwl.weather.eye.fragment.ShawnMainMapFragment;
import com.cxwl.weather.eye.manager.DataCleanManager;
import com.cxwl.weather.eye.utils.AuthorityUtil;
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

public class ShawnMainActivity extends ShawnBaseActivity implements OnClickListener, AMapLocationListener {
	
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
        checkAuthority();
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
        if (TextUtils.equals(MyApplication.USERTYPE, CONST.DECISION_USER)) {//决策用户
            tvUserType.setText("决策用户");
            tvUserType.setTextColor(0xff0f0b08);
            tvUserType.setBackgroundResource(R.drawable.eye_corner_user_decision);
        }else {
            if (TextUtils.equals(MyApplication.AUTHORITY, CONST.MEMBER_USER)) {//会员用户
                tvUserType.setText("会员用户");
                tvUserType.setTextColor(0xff0f0b08);
                tvUserType.setBackgroundResource(R.drawable.eye_corner_user_member);
            }else {
                tvUserType.setText("普通用户");
                tvUserType.setTextColor(0xff5f6c82);
                tvUserType.setBackgroundResource(R.drawable.eye_corner_user_normal);
            }
        }

        OkHttpUserType();
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
                                            MyApplication.AUTHORITY = success;//"true"为会员用户，false为非会员用户
                                            MyApplication.saveUserInfo(mContext);
                                            if (TextUtils.equals(success, "true")) {
                                                tvUserType.setText("会员用户");
                                                tvUserType.setTextColor(0xff0f0b08);
                                                tvUserType.setBackgroundResource(R.drawable.eye_corner_user_member);
                                            }

                                            if (CommonUtil.showExperienceTime(mContext)) {
                                                CommonUtil.dialogExpericence(mContext);
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
	    List<EyeDto> dataList = new ArrayList<>();
	    dataList.addAll(getIntent().getExtras().<EyeDto>getParcelableArrayList("dataList"));
		Fragment fragment1 = new ShawnMainListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
        fragment1.setArguments(bundle);
		fragments.add(fragment1);
		Fragment fragment2 = new ShawnMainMapFragment();
        bundle = new Bundle();
        bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
        fragment2.setArguments(bundle);
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

    //需要申请的所有权限
    private String[] allPermissions = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    //拒绝的权限集合
    public static List<String> deniedList = new ArrayList<>();
    /**
     * 申请定位权限
     */
    private void checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            startLocation();
        }else {
            deniedList.clear();
            for (String permission : allPermissions) {
                if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permission);
                }
            }
            if (deniedList.isEmpty()) {//所有权限都授予
                startLocation();
            }else {
                String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
                ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AuthorityUtil.AUTHOR_LOCATION:
                if (grantResults.length > 0) {
                    boolean isAllGranted = true;//是否全部授权
                    for (int gResult : grantResults) {
                        if (gResult != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false;
                            break;
                        }
                    }
                    if (isAllGranted) {//所有权限都授予
                        startLocation();
                    }else {//只要有一个没有授权，就提示进入设置界面设置
                        checkAuthority();
                    }
                }else {
                    for (String permission : permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            checkAuthority();
                            break;
                        }
                    }
                }
                break;
        }
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        if (CommonUtil.isLocationOpen(this)) {
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//初始化定位参数
            AMapLocationClient mLocationClient = new AMapLocationClient(this);//初始化定位
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
            mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
            mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
            mLocationClient.setLocationListener(this);
            mLocationClient.startLocation();//启动定位
        }else {
            CONST.locationLat = 39.084158;
            CONST.locationLng = 117.200983;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            CONST.locationLat = amapLocation.getLatitude();
            CONST.locationLng = amapLocation.getLongitude();
        }
    }

}
