package com.cxwl.weather.eye.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.manager.SystemStatusManager
import kotlinx.android.synthetic.main.activity_welcome.*

/**
 * 欢迎界面
 */
class ActivityWelcome : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        setTranslucentStatus()
        initWidget()
        initVideoView()
    }

    /**
     * 设置状态栏背景状态
     */
    private fun setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val win = window
            val winParams = win.attributes
            val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            winParams.flags = winParams.flags or bits
            win.attributes = winParams
        }
        val tintManager = SystemStatusManager(this)
        tintManager.isStatusBarTintEnabled = true
        tintManager.setStatusBarTintResource(0) // 状态栏无背景
    }

    private fun initWidget() {
        tvExit.setOnClickListener(this)
        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.duration = 2000
        alphaAnimation.fillAfter = true
        clBg.startAnimation(alphaAnimation)
    }

    private fun initVideoView() {
        videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.welcome_video))
        videoView.setOnPreparedListener { videoView.start() }
        videoView.setOnCompletionListener { intentLogin() }
    }

    private fun intentLogin() {
        startActivity(Intent(this@ActivityWelcome, ActivityLogin::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (videoView != null) {
            videoView!!.stopPlayback()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvExit -> {
                if (videoView.isPlaying) {
                    videoView.stopPlayback()
                }
                intentLogin()
            }
        }
    }

}
