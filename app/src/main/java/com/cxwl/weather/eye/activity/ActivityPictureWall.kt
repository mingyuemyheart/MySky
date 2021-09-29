package com.cxwl.weather.eye.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.adapter.AdapterPictureWall
import com.cxwl.weather.eye.dto.EyeDto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_picture_wall.*
import kotlinx.android.synthetic.main.layout_title.*
import uk.co.senab.photoview.PhotoView

/**
 * 图集展示
 */
class ActivityPictureWall : BaseActivity(), OnClickListener {

    private var mAdapter: AdapterPictureWall? = null
    private val dataList: ArrayList<EyeDto> = ArrayList()
    private var selectIndex = 0
    private var configuration: Configuration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_wall)
        initWidget()
        initViewPager()
        initGallery()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)

        if (intent.hasExtra("dataList")) {
            dataList.clear()
            dataList.addAll(intent.getParcelableArrayListExtra("dataList"))
            if (mAdapter != null) {
                mAdapter!!.notifyDataSetChanged()
            }
        }

        if (intent.hasExtra("selectIndex")) {
            selectIndex = intent.getIntExtra("selectIndex", 0)
        }
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        val imageArray = arrayOfNulls<ImageView>(dataList.size)
        for (i in dataList.indices) {
            val dto = dataList[i]
            if (!TextUtils.isEmpty(dto.pictureUrl)) {
                val imageView = ImageView(this)
                imageView.adjustViewBounds = true
                Picasso.get().load(dto.pictureUrl).into(imageView)
                imageArray[i] = imageView
            }
            if (i == selectIndex) {
                tvTitle.text = dto.time
            }
        }
        val pagerAdapter = MyViewPagerAdapter(imageArray)
        viewPager.adapter = pagerAdapter
        viewPager.setOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageSelected(arg0: Int) {
                gallery.setSelection(arg0, true)
                val dd = dataList[arg0]
                tvTitle.text = dd.time
            }
            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })
        viewPager.setCurrentItem(selectIndex, true)
    }

    private class MyViewPagerAdapter(private val mImageViews: Array<ImageView?>) : PagerAdapter() {
        override fun getCount(): Int {
            return mImageViews.size
        }

        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(mImageViews[position])
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = PhotoView(container.context)
            val drawable = mImageViews[position]!!.drawable
            photoView.setImageDrawable(drawable)
            container.addView(photoView, 0)
            return photoView
        }
    }

    private fun initGallery() {
        mAdapter = AdapterPictureWall(this, dataList)
        gallery.adapter = mAdapter
        gallery.setOnItemClickListener { parent, view, position, id ->
            viewPager.setCurrentItem(position, true)
        }
        gallery.setSelection(selectIndex, true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configuration = newConfig
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showPort()
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showLand()
        }
    }

    /**
     * 显示竖屏，隐藏横屏
     */
    private fun showPort() {
        fullScreen(false)
    }

    /**
     * 显示横屏，隐藏竖屏
     */
    private fun showLand() {
        fullScreen(true)
    }

    private fun fullScreen(enable: Boolean) {
        if (enable) {
            val lp = window.attributes
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes = lp
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        } else {
            val attr = window.attributes
            attr.flags = attr.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            window.attributes = attr
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    private fun exit() {
        if (configuration == null) {
            finish()
        } else {
            if (configuration!!.orientation == Configuration.ORIENTATION_PORTRAIT) {
                finish()
            } else if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit()
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> exit()
        }
    }
	
}
