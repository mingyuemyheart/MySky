package com.cxwl.weather.eye.fragment

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
import com.cxwl.weather.eye.utils.CommonUtil
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

/**
 * 首页
 */
class FragmentHome : BaseFragment() {

    private val fragments = ArrayList<Fragment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        llContainer.removeAllViews()
        fragments.clear()
        for (i in 0 until 2) {
            val llItem = LinearLayout(activity)
            llItem.orientation = LinearLayout.VERTICAL
            llItem.gravity = Gravity.CENTER_VERTICAL
            val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            param.weight = 1f
            llItem.layoutParams = param

            val tv = TextView(activity)
            tv.setTextColor(Color.WHITE)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tv.gravity = Gravity.CENTER
            tv.setPadding(0, CommonUtil.dip2px(activity!!, 15f).toInt(), 0, CommonUtil.dip2px(activity!!, 10f).toInt())
            tv.setOnClickListener(MyOnClickListener(i))
            llItem.addView(tv)

            val tvBar = TextView(activity)
            val paramBar = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            paramBar.width = tv.layoutParams.width
            paramBar.height = CommonUtil.dip2px(activity, 2f).toInt()
            tvBar.layoutParams = paramBar
            llItem.addView(tvBar)

            var fragment: BaseFragment? = null
            when(i) {
                0 -> {
                    tv.text = "地图"
                    tv.setTextColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
                    tvBar.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
                    fragment = FragmentHomeMap()
                }
                else -> {
                    tv.text = "列表"
                    tv.setTextColor(ContextCompat.getColor(activity!!, R.color.text_color4))
                    tvBar.setBackgroundColor(Color.TRANSPARENT)
                    fragment = FragmentHomeList()
                }
            }
            llContainer.addView(llItem, i)
            fragments.add(fragment)
        }
        viewPager.setSlipping(false) //设置ViewPager是否可以滑动
        viewPager.offscreenPageLimit = fragments.size
        viewPager.setOnPageChangeListener(MyOnPageChangeListener())
        viewPager.adapter = MyPagerAdapter(childFragmentManager, fragments)
    }

    private inner class MyOnPageChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            if (llContainer != null) {
                for (i in 0 until llContainer.childCount) {
                    val llItem = llContainer.getChildAt(i) as LinearLayout
                    val tv = llItem.getChildAt(0) as TextView
                    val tvBar = llItem.getChildAt(1) as TextView
                    if (i == arg0) {
                        tv.setTextColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
                        tvBar.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
                    } else {
                        tv.setTextColor(ContextCompat.getColor(activity!!, R.color.text_color4))
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

}
