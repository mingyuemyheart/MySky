package com.cxwl.weather.eye.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.webkit.*
import android.webkit.WebSettings.LayoutAlgorithm
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.adapter.AdapterPictureWall
import com.cxwl.weather.eye.dto.EyeDto
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.layout_title.*

/**
 * 视频预览
 */
class ActivityVideoPreview : BaseActivity(), OnClickListener {

    private var mAdapter: AdapterPictureWall? = null
    private val dataList: ArrayList<EyeDto> = ArrayList()
    private var selectIndex = 0
    private var configuration: Configuration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        initWidget()
        initWebView()
        initGallery()
    }

    /**
     * 初始化控件
     */
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
     * 初始化webview
     */
    private fun initWebView() {
        val webSettings = webView.settings
        //支持javascript
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.domStorageEnabled = true
        webSettings.setGeolocationEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        // 设置可以支持缩放
        webSettings.setSupportZoom(true)
        // 设置出现缩放工具
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        //扩大比例的缩放
        webSettings.useWideViewPort = true
        //自适应屏幕
        webSettings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN
        webSettings.loadWithOverviewMode = true
        val dto = dataList[selectIndex]
        if (dto.time != null) {
            tvTitle.text = dto.time
        }
        loadUrl(dto.videoUrl)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                callback!!.invoke(origin, true, false)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, itemUrl: String?): Boolean {
                loadUrl(itemUrl)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)
                handler!!.proceed()
            }
        }
    }

    private fun loadUrl(dataUrl: String?) {
        if (webView != null && !TextUtils.isEmpty(dataUrl)) {
            webView.loadUrl(dataUrl)
        }
    }

    override fun onPause() {
        super.onPause()
        if (webView != null) {
            webView!!.reload()
        }
    }

    private fun initGallery() {
        mAdapter = AdapterPictureWall(this, dataList)
        gallery.adapter = mAdapter
        gallery.setOnItemClickListener { parent, view, position, id ->
            val dto = dataList[position]
            if (dto.time != null) {
                tvTitle.text = dto.time
            }
            loadUrl(dto.videoUrl)
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
