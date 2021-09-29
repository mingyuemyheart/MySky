package com.cxwl.weather.eye.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.Toast
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.CommonUtil
import com.cxwl.weather.eye.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import com.tencent.rtmp.ITXLivePlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLivePlayer
import kotlinx.android.synthetic.main.activity_video_detail.*
import kotlinx.android.synthetic.main.activity_video_detail.reTitle
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.layout_video.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 视频详情
 */
class ActivityVideoDetail : BaseActivity(), OnClickListener {

    private var data: EyeDto? = null
    private var configuration: Configuration? = null
    private var mLivePlayer: TXLivePlayer? = null
    private var expericenceTime: Long = 0
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        initWidget()
    }

    private fun initTimer() {
        if (TextUtils.equals(MyApplication.USERTYPE, CONST.DECISION_USER) || TextUtils.equals(MyApplication.AUTHORITY, CONST.MEMBER_USER)) { //决策用户或会员用户就不计时了
            return
        }
        if (timer == null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    handler.sendEmptyMessage(10001)
                }
            }, 0, CONST.EXPERIENCEREFRESH) //一分钟刷新一次
        }
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 10001) {
                CommonUtil.saveExperienceTime(this@ActivityVideoDetail, System.currentTimeMillis() - expericenceTime) //保存体验时间
                if (CommonUtil.showExperienceTime(this@ActivityVideoDetail)) {
                    CommonUtil.dialogExpericence(this@ActivityVideoDetail)
                    resetTimer()
                    if (mLivePlayer != null) {
                        mLivePlayer!!.stopPlay(true) // true代表清除最后一帧画面
                        mLivePlayer = null
                    }
                    if (txCloudVideoView != null) {
                        txCloudVideoView!!.onDestroy()
                    }
                }
            }
        }
    }

    private fun resetTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvWeather.setOnClickListener(this)

        txCloudVideoView.setOnClickListener(this)
        mLivePlayer = TXLivePlayer(this)
        mLivePlayer!!.setPlayerView(txCloudVideoView)
        showPort()

        if (intent.hasExtra("data")) {
            data = intent.getParcelableExtra("data")
            if (data != null) {
                if (data!!.location != null) {
                    tvTitle.text = data!!.location
                }
                okHttpDetail(data!!.deviceInfoId)
            }
        }
    }

    /**
     * 初始化播放器
     */
    private fun initTXCloudVideoView(streamUrl: String, isRtmp: Boolean) {
        if (!TextUtils.isEmpty(streamUrl) && mLivePlayer != null) {
            if (isRtmp) {
                mLivePlayer!!.startPlay(streamUrl, TXLivePlayer.PLAY_TYPE_LIVE_RTMP)
            } else {
                mLivePlayer!!.startPlay(streamUrl, TXLivePlayer.PLAY_TYPE_VOD_HLS)
            }
            mLivePlayer!!.setPlayListener(object : ITXLivePlayListener {
                override fun onPlayEvent(arg0: Int, arg1: Bundle) {
                    if (arg0 == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) { //视频播放开始
                        progressBar!!.visibility = View.GONE
                        ivCamera.setOnClickListener(this@ActivityVideoDetail)
                        ivCamera.visibility = View.VISIBLE
                        ivDelay.setOnClickListener(this@ActivityVideoDetail)
                        ivDelay.visibility = View.VISIBLE
                        ivExpand.setOnClickListener(this@ActivityVideoDetail)
                        ivExpand.visibility = View.VISIBLE
                        expericenceTime = System.currentTimeMillis()
                        initTimer()
                    }
                }

                override fun onNetStatus(status: Bundle) {
                    tv.text = "Current status, CPU:" + status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE) +
                            ", RES:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) +
                            ", SPD:" + status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" +
                            ", FPS:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) +
                            ", ARA:" + status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" +
                            ", VRA:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps"
                }
            })
        }
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
        reTitle!!.visibility = View.VISIBLE
        fullScreen(false)
        switchVideo()
    }

    /**
     * 显示横屏，隐藏竖屏
     */
    private fun showLand() {
        reTitle!!.visibility = View.GONE
        fullScreen(true)
        switchVideo()
    }

    /**
     * 横竖屏切换视频窗口
     */
    private fun switchVideo() {
        if (txCloudVideoView != null) {
            val width = CommonUtil.widthPixels(this)
            val height = width * 9 / 16
            val params = txCloudVideoView!!.layoutParams
            params.width = width
            params.height = height
            txCloudVideoView!!.layoutParams = params
            val params1 = ivLogo!!.layoutParams
            params1.width = width / 3
            ivLogo!!.layoutParams = params1
        }
        if (mLivePlayer != null) {
            mLivePlayer!!.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION)
            mLivePlayer!!.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        resetTimer()
        if (mLivePlayer != null) {
            mLivePlayer!!.stopPlay(true) // true代表清除最后一帧画面
            mLivePlayer = null
        }
        if (txCloudVideoView != null) {
            txCloudVideoView!!.onDestroy()
        }
    }

    override fun onClick(v: View) {
        val intent: Intent
        val bundle: Bundle
        when (v.id) {
            R.id.llBack -> exit()
            R.id.ivCamera -> {
                val bitmap1 = CommonUtil.captureMyView(txCloudVideoView)
                CommonUtil.share(this, bitmap1)
            }
            R.id.ivExpand -> {
                if (configuration == null) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    if (configuration!!.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
            R.id.tvWeather -> {
                intent = Intent(this, ShawnForecastActivity::class.java)
                bundle = Bundle()
                bundle.putParcelable("data", data)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.ivDelay -> {
                intent = Intent(this, ShawnDelayActivity::class.java)
                bundle = Bundle()
                bundle.putParcelable("data", data)
                intent.putExtras(bundle)
                startActivity(intent)
                overridePendingTransition(R.anim.shawn_in_downtoup, R.anim.shawn_out_downtoup)
            }
        }
    }

    /**
     * 获取直播详情信息
     */
    private fun okHttpDetail(deviceInfoId: String) {
        Thread {
            val url = "${CONST.BASE_URL}/sky/system/media/live?deviceInfoId=$deviceInfoId"
            OkHttpUtil.enqueue(Request.Builder().url(url).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        if (!obj.isNull("data")) {
                                            val dataObj = obj.getJSONObject("data")
                                            if (!dataObj.isNull("rtmpUrl")) {
                                                val rtmpUrl = dataObj.getString("rtmpUrl")
                                                if (!TextUtils.isEmpty(rtmpUrl)) {
                                                    initTXCloudVideoView(rtmpUrl, true)
                                                } else if (!dataObj.isNull("streamUrl")) {
                                                    val streamUrl = dataObj.getString("streamUrl")
                                                    initTXCloudVideoView(streamUrl, false)
                                                }
                                            }
                                            if (!dataObj.isNull("watermark")) {
                                                val watermark = dataObj.getString("watermark")
                                                if (!TextUtils.isEmpty(watermark)) {
                                                    Picasso.get().load(watermark).into(ivLogo)
                                                }
                                            }
                                        }
                                    } else {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(this@ActivityVideoDetail, errorMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

}
