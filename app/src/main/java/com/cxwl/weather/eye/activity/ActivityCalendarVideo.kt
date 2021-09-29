package com.cxwl.weather.eye.activity

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.adapter.MyPagerAdapter
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.fragment.BaseFragment
import com.cxwl.weather.eye.fragment.FragmentCalendar
import com.cxwl.weather.eye.fragment.FragmentHomeList
import com.cxwl.weather.eye.fragment.FragmentHomeMap
import com.cxwl.weather.eye.utils.CommonUtil
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_title.*
import java.util.*

/**
 * 天气日历、延时视频
 */
class ActivityCalendarVideo : BaseFragmentActivity(), View.OnClickListener {

    private var data: EyeDto? = null
    private val fragments = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_video)
        initWidget()
        initViewPager()
    }
    
    private fun initWidget() {
        llBack.setOnClickListener(this)

        if (intent.hasExtra("data")) {
            data = intent.getParcelableExtra("data")
            if (data != null) {
                if (data!!.location != null) {
                    tvTitle.text = data!!.location
                }
            }
        }
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        llContainer.removeAllViews()
        fragments.clear()
        for (i in 0 until 2) {
            val llItem = LinearLayout(this)
            llItem.orientation = LinearLayout.VERTICAL
            llItem.gravity = Gravity.CENTER_VERTICAL
            val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            param.weight = 1f
            llItem.layoutParams = param

            val tv = TextView(this)
            tv.setTextColor(Color.WHITE)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            tv.gravity = Gravity.CENTER
            tv.setPadding(0, CommonUtil.dip2px(this, 8f).toInt(), 0, CommonUtil.dip2px(this, 8f).toInt())
            tv.setOnClickListener(MyOnClickListener(i))
            llItem.addView(tv)

            val tvBar = TextView(this)
            val paramBar = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            paramBar.width = tv.layoutParams.width
            paramBar.height = CommonUtil.dip2px(this, 2f).toInt()
            tvBar.layoutParams = paramBar
            llItem.addView(tvBar)

            val fragment = FragmentCalendar()
            val bundle = Bundle()
            when(i) {
                0 -> {
                    tv.text = "天气日历"
                    tv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                    tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                    bundle.putString("fileType", "0")//文件类型(0 图片,1视频)
                }
                else -> {
                    tv.text = "延时视频"
                    tv.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                    tvBar.setBackgroundColor(Color.TRANSPARENT)
                    bundle.putString("fileType", "1")//文件类型(0 图片,1视频)
                }
            }
            bundle.putParcelable("data", data)
            fragment.arguments = bundle
            llContainer.addView(llItem, i)
            fragments.add(fragment)
        }
        viewPager.setSlipping(false) //设置ViewPager是否可以滑动
        viewPager.offscreenPageLimit = fragments.size
        viewPager.setOnPageChangeListener(MyOnPageChangeListener())
        viewPager.adapter = MyPagerAdapter(supportFragmentManager, fragments)
    }

    private inner class MyOnPageChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            if (llContainer != null) {
                for (i in 0 until llContainer.childCount) {
                    val llItem = llContainer.getChildAt(i) as LinearLayout
                    val tv = llItem.getChildAt(0) as TextView
                    val tvBar = llItem.getChildAt(1) as TextView
                    if (i == arg0) {
                        tv.setTextColor(ContextCompat.getColor(this@ActivityCalendarVideo, R.color.colorAccent))
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@ActivityCalendarVideo, R.color.colorAccent))
                    } else {
                        tv.setTextColor(ContextCompat.getColor(this@ActivityCalendarVideo, R.color.text_color4))
                        tvBar.setBackgroundColor(Color.TRANSPARENT)
                    }
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
    private inner class MyOnClickListener(private val index: Int) : View.OnClickListener {
        override fun onClick(v: View) {
            if (viewPager != null) {
                viewPager.setCurrentItem(index, true)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
