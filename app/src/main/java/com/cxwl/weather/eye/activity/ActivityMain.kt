package com.cxwl.weather.eye.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.adapter.MyPagerAdapter
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.fragment.*
import com.cxwl.weather.eye.utils.AuthorityUtil
import com.cxwl.weather.eye.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class ActivityMain : BaseFragmentActivity(), OnClickListener, AMapLocationListener {

    private var mExitTime: Long = 0
    private val fragments = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyApplication.addDestoryActivity(this, "MainActivity")
        checkAuthority()
        initWidget()
        initViewPager()
    }

    private fun initWidget() {
        ll1.setOnClickListener(MyOnClickListener(0))
        ll2.setOnClickListener(MyOnClickListener(1))
        ll3.setOnClickListener(MyOnClickListener(2))
        ll4.setOnClickListener(MyOnClickListener(3))
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        fragments.clear()
        fragments.add(FragmentHome())
        fragments.add(FragmentResourceLibrary())
        fragments.add(FragmentMsg())
        fragments.add(FragmentMy())
        viewPager.setSlipping(false) //设置ViewPager是否可以滑动
        viewPager.offscreenPageLimit = fragments.size
        viewPager.setOnPageChangeListener(MyOnPageChangeListener())
        viewPager.adapter = MyPagerAdapter(supportFragmentManager, fragments)
    }

    private inner class MyOnPageChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            when(arg0) {
                0 -> {
                    iv1.setImageResource(R.drawable.icon_home_press)
                    iv2.setImageResource(R.drawable.icon_device)
                    iv3.setImageResource(R.drawable.icon_msg)
                    iv4.setImageResource(R.drawable.icon_my)
                    tv1.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.colorAccent))
                    tv2.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                }
                1 -> {
                    iv1.setImageResource(R.drawable.icon_home)
                    iv2.setImageResource(R.drawable.icon_device_press)
                    iv3.setImageResource(R.drawable.icon_msg)
                    iv4.setImageResource(R.drawable.icon_my)
                    tv1.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.colorAccent))
                    tv3.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                }
                2 -> {
                    iv1.setImageResource(R.drawable.icon_home)
                    iv2.setImageResource(R.drawable.icon_device)
                    iv3.setImageResource(R.drawable.icon_msg_press)
                    iv4.setImageResource(R.drawable.icon_my)
                    tv1.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.colorAccent))
                    tv4.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                }
                3 -> {
                    iv1.setImageResource(R.drawable.icon_home)
                    iv2.setImageResource(R.drawable.icon_device)
                    iv3.setImageResource(R.drawable.icon_msg)
                    iv4.setImageResource(R.drawable.icon_my_press)
                    tv1.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@ActivityMain, R.color.colorAccent))
                }
            }
        }
        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * 头标点击监听
     * @author shawn_sun
     */
    private inner class MyOnClickListener(private val index: Int) : OnClickListener {
        override fun onClick(v: View) {
            if (viewPager != null) {
                viewPager.setCurrentItem(index, true)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                Toast.makeText(this, "再按一次退出" + getString(R.string.app_name), Toast.LENGTH_SHORT).show()
                mExitTime = System.currentTimeMillis()
            } else {
                finish()
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> {
                if (System.currentTimeMillis() - mExitTime > 2000) {
                    Toast.makeText(this, "再按一次退出" + getString(R.string.app_name), Toast.LENGTH_SHORT).show()
                    mExitTime = System.currentTimeMillis()
                } else {
                    finish()
                }
            }
        }
    }

    //需要申请的所有权限
    private val allPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //拒绝的权限集合
    private var deniedList: MutableList<String> = ArrayList()

    /**
     * 申请定位权限
     */
    private fun checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            startLocation()
        } else {
            deniedList.clear()
            for (permission in allPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permission)
                }
            }
            if (deniedList.isEmpty()) { //所有权限都授予
                startLocation()
            } else {
                val permissions = deniedList.toTypedArray() //将list转成数组
                ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AuthorityUtil.AUTHOR_LOCATION -> if (grantResults.isNotEmpty()) {
                var isAllGranted = true //是否全部授权
                for (gResult in grantResults) {
                    if (gResult != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false
                        break
                    }
                }
                if (isAllGranted) { //所有权限都授予
                    startLocation()
                } else { //只要有一个没有授权，就提示进入设置界面设置
                    checkAuthority()
                }
            } else {
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)) {
                        checkAuthority()
                        break
                    }
                }
            }
        }
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        if (CommonUtil.isLocationOpen(this)) {
            val mLocationOption = AMapLocationClientOption() //初始化定位参数
            val mLocationClient = AMapLocationClient(this) //初始化定位
            mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.isNeedAddress = true //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.isOnceLocation = true //设置是否只定位一次,默认为false
            mLocationOption.isMockEnable = false //设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.interval = 2000 //设置定位间隔,单位毫秒,默认为2000ms
            mLocationClient.setLocationOption(mLocationOption) //给定位客户端对象设置定位参数
            mLocationClient.setLocationListener(this)
            mLocationClient.startLocation() //启动定位
        } else {
            CONST.locationLat = 39.084158
            CONST.locationLng = 117.200983
        }
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            CONST.locationLat = amapLocation.latitude
            CONST.locationLng = amapLocation.longitude
            Log.e("center", "locationLat="+CONST.locationLat+", locationLng="+CONST.locationLng)
        }
    }

}
