package com.cxwl.weather.eye.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.SeekBar.OnSeekBarChangeListener
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.adapter.ForePositionAdapter
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.CommonUtil
import com.cxwl.weather.eye.utils.DownloadVideoUtil
import com.cxwl.weather.eye.utils.OkHttpUtil
import com.cxwl.weather.eye.view.RoundMenuView
import com.cxwl.weather.eye.view.wheelview.NumericWheelAdapter
import com.cxwl.weather.eye.view.wheelview.OnWheelScrollListener
import com.cxwl.weather.eye.view.wheelview.WheelView
import com.squareup.picasso.Picasso
import com.tencent.rtmp.ITXLivePlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLivePlayer
import kotlinx.android.synthetic.main.activity_video_setting.*
import kotlinx.android.synthetic.main.activity_video_setting.reTitle
import kotlinx.android.synthetic.main.dialog_fore_position.view.*
import kotlinx.android.synthetic.main.layout_date.*
import kotlinx.android.synthetic.main.layout_delay_video.*
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.layout_video.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 视频操作
 */
class ActivityVideoSetting : BaseActivity(), OnClickListener {

    private var data: EyeDto? = null
    private var configuration: Configuration? = null
    private var mLivePlayer: TXLivePlayer? = null
    private var startDegree = 0f //开始角度
    private var clickDegree = 0f //选中角度
    private var mReceiver: MyBroadCastReceiver? = null
    private var isStart = true
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    //操作类型 0 上，1右上，2右，3右下，4下，5左下，6左，7左上，方向（0-7，0代表上，顺时针方向依次对应）
    //10变倍大，11变倍小，13聚焦近，14聚焦远，17打开光圈，18关闭光圈，19打开雨刷，
    //20关闭雨刷，23巡航开始，24巡航关闭，30亮度，31色度，32对比度，33饱和度，28预位置
    private var orderType = "0"
    private var speed = 50 //速度0~1
    private var iris = 1.0f //光圈速度参数,-1.0~1.0
    private var focus = 0f //变焦速度参数,-1.0~1.0
    private var brightness = 50 //亮度
    private var contrast = 50 //对比度
    private var saturation = 50 //饱和度
    private var chroma = 50 //色度

    private val HANDLER_SPEED_MINUSE_DOWN = 2
    private val HANDLER_SPEED_MINUSE_UP = 3
    private val HANDLER_SPEED_PLUS_DOWN = 4
    private val HANDLER_SPEED_PLUS_UP = 5
    private var isClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_setting)
        initBroadCastReceiver()
        initWidget()
        initRoundMenuView()
        initWheelView()
    }

    private fun initBroadCastReceiver() {
        mReceiver = MyBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CONST.CIRCLE_CONTROLER)
        registerReceiver(mReceiver, intentFilter)
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivMinuseSpeed.setOnTouchListener(minuseSpeedListener)
        ivPlusSpeed.setOnTouchListener(plusSpeedListener)
        tvSpeed.text = (speed/100.0f).toString()
        ivGQOff.setOnClickListener(this)
        ivGQOn.setOnClickListener(this)
        ivMinuseJJ.setOnClickListener(this)
        ivPlusJJ.setOnClickListener(this)
        seekBar1.setOnSeekBarChangeListener(seekBarListener1)
        seekBar2.setOnSeekBarChangeListener(seekBarListener2)
        seekBar3.setOnSeekBarChangeListener(seekBarListener3)
        seekBar4.setOnSeekBarChangeListener(seekBarListener4)
        seekBar1.progress = brightness
        seekBar2.progress = contrast
        seekBar3.progress = saturation
        seekBar4.progress = chroma
        tvSeekBar1.text = brightness.toString()
        tvSeekBar2.text = contrast.toString()
        tvSeekBar3.text = saturation.toString()
        tvSeekBar4.text = chroma.toString()
        tvForePosition.setOnClickListener(this)
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

    @SuppressLint("ClickableViewAccessibility")
    private val minuseSpeedListener = OnTouchListener { arg0, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isClick = true
                val mThread: Thread = object : Thread() {
                    override fun run() {
                        while (isClick) {
                            if (speed <= 0) {
                                speed = 0
                            } else {
                                speed -= 10
                            }
                            handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_DOWN)
                            try {
                                sleep(200)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                mThread.start()
            }
            MotionEvent.ACTION_UP -> {
                isClick = false
                handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_UP)
            }
            MotionEvent.ACTION_CANCEL -> {
                isClick = false
                handler.sendEmptyMessage(HANDLER_SPEED_MINUSE_UP)
            }
        }
        true
    }

    @SuppressLint("ClickableViewAccessibility")
    private val plusSpeedListener = OnTouchListener { arg0, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isClick = true
                val mThread: Thread = object : Thread() {
                    override fun run() {
                        while (isClick) {
                            if (speed >= 100) {
                                speed = 100
                            } else {
                                speed += 10
                            }
                            handler.sendEmptyMessage(HANDLER_SPEED_PLUS_DOWN)
                            try {
                                sleep(200)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                mThread.start()
            }
            MotionEvent.ACTION_UP -> {
                isClick = false
                handler.sendEmptyMessage(HANDLER_SPEED_PLUS_UP)
            }
            MotionEvent.ACTION_CANCEL -> {
                isClick = false
                handler.sendEmptyMessage(HANDLER_SPEED_PLUS_UP)
            }
        }
        true
    }

    /**
     * 获取摄像头移动速度、亮度、饱和度、对比度、色度
     */
//    private fun okHttpParameter(url: String) {
//        val builder = FormBody.Builder()
//        builder.add("FID", data!!.fId) //设备id
//        val body: RequestBody = builder.build()
//        Thread {
//            OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
//                override fun onFailure(call: Call, e: IOException) {}
//
//                @Throws(IOException::class)
//                override fun onResponse(call: Call, response: Response) {
//                    if (!response.isSuccessful) {
//                        return
//                    }
//                    val result = response.body!!.string()
//                    runOnUiThread {
//                        if (!TextUtils.isEmpty(result)) {
//                            try {
//                                val obje = JSONObject(result)
//                                if (!obje.isNull("code")) {
//                                    val code = obje.getString("code")
//                                    if (TextUtils.equals(code, "200") || TextUtils.equals(code, "2000")) { //成功
//                                        if (!obje.isNull("list")) {
//                                            val array = obje.getJSONArray("list")
//                                            val obj = array.getJSONObject(0)
//                                            if (!obj.isNull("speed")) {
//                                                speed = obj.getInt("speed")
//                                                tvSpeed.text = speed.toString()
//                                            }
//                                            if (!obj.isNull("brightness")) {
//                                                brightness = obj.getInt("brightness")
//                                                tvSeekBar1!!.text = brightness.toString()
//                                                seekBar1!!.progress = brightness
//                                            }
//                                            if (!obj.isNull("contrast")) {
//                                                contrast = obj.getInt("contrast")
//                                                tvSeekBar2.text = contrast.toString()
//                                                seekBar2.progress = contrast
//                                            }
//                                            if (!obj.isNull("saturation")) {
//                                                saturation = obj.getInt("saturation")
//                                                tvSeekBar3.text = saturation.toString()
//                                                seekBar3.progress = saturation
//                                            }
//                                            if (!obj.isNull("chroma")) {
//                                                chroma = obj.getInt("chroma")
//                                                tvSeekBar4.text = chroma.toString()
//                                                seekBar4.progress = chroma
//                                            }
//                                        }
//                                    }
//                                }
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//                }
//            })
//        }.start()
//    }

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
                        ivCamera.setOnClickListener(this@ActivityVideoSetting)
                        ivCamera.visibility = View.VISIBLE
                        ivDelay.setOnClickListener(this@ActivityVideoSetting)
                        ivDelay.visibility = View.VISIBLE
                        ivExpand.setOnClickListener(this@ActivityVideoSetting)
                        ivExpand.visibility = View.VISIBLE
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

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                HANDLER_SPEED_MINUSE_DOWN -> {
                    tvSpeed.text = (speed/100.0f).toString()
                    setDirection()
                    ivMinuseSpeed!!.setImageResource(R.drawable.eye_btn_minuse_press)
                }
                HANDLER_SPEED_MINUSE_UP -> ivMinuseSpeed!!.setImageResource(R.drawable.eye_btn_minuse)
                HANDLER_SPEED_PLUS_DOWN -> {
                    tvSpeed.text = (speed/100.0f).toString()
                    setDirection()
                    ivPlusSpeed.setImageResource(R.drawable.eye_btn_plus_press)
                }
                HANDLER_SPEED_PLUS_UP -> ivPlusSpeed.setImageResource(R.drawable.eye_btn_plus)
            }
        }
    }

    /**
     * 初始化圆形菜单按钮
     */
    private fun initRoundMenuView() {
        val roundMenuView: RoundMenuView = findViewById(R.id.roundMenuView)
        val width = CommonUtil.widthPixels(this)
        val params = roundMenuView.layoutParams
        params.width = width * 3 / 5
        params.height = width * 3 / 5
        roundMenuView.layoutParams = params
        roundMenuView.setRadius(params.width / 2)
        roundMenuView.setCenterXY(params.width / 2, params.height / 2)
        val params2 = ivMenuDir!!.layoutParams
        params2.width = params.width / 3
        params2.height = params.height / 3
        ivMenuDir!!.layoutParams = params2
    }

    /**
     * 旋转菜单动画
     */
    private fun rotateMenu(image: ImageView, startDegree: Float, endDegree: Float) {
        val rotate = RotateAnimation(startDegree, endDegree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 500
        rotate.fillAfter = true
        image.startAnimation(rotate)
    }

    private inner class MyBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(arg0: Context, intent: Intent) {
            if (TextUtils.equals(intent.action, CONST.CIRCLE_CONTROLER)) { //接收圆形控制器指令
                val bundle = intent.extras
                orderType = bundle.getString("orderType")
                if (TextUtils.equals(orderType, "3")) { //右下
                    clickDegree = 135f
                } else if (TextUtils.equals(orderType, "4")) { //下
                    clickDegree = 180f
                } else if (TextUtils.equals(orderType, "5")) { //左下
                    clickDegree = 225f
                } else if (TextUtils.equals(orderType, "6")) { //左
                    clickDegree = 270f
                } else if (TextUtils.equals(orderType, "7")) { //左上
                    clickDegree = 315f
                } else if (TextUtils.equals(orderType, "0")) { //上
                    clickDegree = 360f
                } else if (TextUtils.equals(orderType, "1")) { //右上
                    clickDegree = 45f
                } else if (TextUtils.equals(orderType, "2")) { //右
                    clickDegree = 90f
                }
                if (startDegree == 0f) {
                    rotateMenu(ivMenuDir, startDegree, clickDegree - 45)
                } else {
                    rotateMenu(ivMenuDir, startDegree - 45, clickDegree - 45)
                }
                startDegree = clickDegree
                setDirection()
            }
        }
    }

    private val seekBarListener1: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStopTrackingTouch(arg0: SeekBar) {}
        override fun onStartTrackingTouch(arg0: SeekBar) {}
        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
            brightness = arg1
            tvSeekBar1!!.text = brightness.toString()
//            OkHttpCommand(commandBaseUrl)
        }
    }

    private val seekBarListener2: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStopTrackingTouch(arg0: SeekBar) {}
        override fun onStartTrackingTouch(arg0: SeekBar) {}
        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
            contrast = arg1
            tvSeekBar2.text = contrast.toString()
//            OkHttpCommand(commandBaseUrl)
        }
    }

    private val seekBarListener3: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStopTrackingTouch(arg0: SeekBar) {}
        override fun onStartTrackingTouch(arg0: SeekBar) {}
        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
            saturation = arg1
            tvSeekBar3.text = saturation.toString()
//            OkHttpCommand(commandBaseUrl)
        }
    }

    private val seekBarListener4: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStopTrackingTouch(arg0: SeekBar) {}
        override fun onStartTrackingTouch(arg0: SeekBar) {}
        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
            chroma = arg1
            tvSeekBar4.text = chroma.toString()
//            OkHttpCommand(commandBaseUrl)
        }
    }

    /**
     * 设置预位置
     */
    private fun dialogForePosition() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_fore_position, null)
        view.tvMessage.text = "设置预设位"
        val foreList: MutableList<EyeDto> = ArrayList()
        foreList.clear()
        for (i in 1..7) {
            val dto = EyeDto()
            dto.forePosition = i.toString()
            foreList.add(dto)
        }
        val foreAdapter = ForePositionAdapter(this, foreList)
        view.listView.adapter = foreAdapter
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvNegtive.setOnClickListener { dialog.dismiss() }
        view.listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            dialog.dismiss()
            val dto = foreList[arg2]
            tvForePosition.text = dto.forePosition
            setForePosition()
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
        if (mLivePlayer != null) {
            mLivePlayer!!.stopPlay(true) // true代表清除最后一帧画面
            mLivePlayer = null
        }
        if (txCloudVideoView != null) {
            txCloudVideoView!!.onDestroy()
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> exit()
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
            R.id.ivCamera -> {
                val bitmap1 = CommonUtil.captureMyView(txCloudVideoView)
                CommonUtil.share(this, bitmap1)
            }
            R.id.ivDelay, R.id.ivCloseDelay -> {
                if (layoutDelayVideo.visibility == View.VISIBLE) {
                    layoutDelayVideo.visibility = View.GONE
                } else {
                    layoutDelayVideo.visibility = View.VISIBLE
                }
            }
            R.id.tvStartTime -> {
                isStart = true
                bootTimeLayoutAnimation(layoutDate)
            }
            R.id.tvEndTime -> {
                isStart = false
                bootTimeLayoutAnimation(layoutDate)
            }
            R.id.tvNegtive -> {
                bootTimeLayoutAnimation(layoutDate)
            }
            R.id.tvPositive -> {
                setTextViewValue()
                bootTimeLayoutAnimation(layoutDate)
            }
            R.id.tvSave -> {
                val start = sdf1.parse(tvStartTime.text.toString())
                val end = sdf1.parse(tvEndTime.text.toString())
                if (start > end) {
                    Toast.makeText(this, "开始时间不能大于结束时间", Toast.LENGTH_SHORT).show()
                } else {
                    okHttpMergeDelayVideo()
                }
            }
            R.id.ivGQOff -> {
                iris = -1.0f
                setIris()
            }
            R.id.ivGQOn -> {
                iris = 1.0f
                setIris()
            }
            R.id.ivMinuseJJ -> {
                if (focus <= -100) {
                    focus = -100f
                } else {
                    focus -= 10f
                }
                setIris()
            }
            R.id.ivPlusJJ -> {
                if (focus >= 100) {
                    focus = 100f
                } else {
                    focus += 10f
                }
                setIris()
            }
            R.id.tvForePosition -> dialogForePosition()
        }
    }

    /**
     * 获取直播详情信息
     */
    private fun okHttpDetail(deviceInfoId: String) {
        if (TextUtils.isEmpty(deviceInfoId)) {
            return
        }
        Thread {
            val url = "${CONST.BASE_URL}/sky/system/media/live?deviceInfoId=$deviceInfoId"
            OkHttpUtil.enqueue(Request.Builder().url(url).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
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
                                                Toast.makeText(this@ActivityVideoSetting, errorMessage, Toast.LENGTH_SHORT).show()
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

    /**
     * 设置方向
     * @param orderType //操作类型 0 上，1右上，2右，3右下，4下，5左下，6左，7左上，方向（0-7，0代表上，顺时针方向依次对应）
     * @param speed 速度0~1
     */
    private fun setDirection() {
        val url = "${CONST.BASE_URL}/sky/system/device/cameraLensMove"
        Log.e("cameraLensMove", url)
        val param = JSONObject()
        param.put("deviceId", data!!.fId)
        param.put("direction", orderType)
        param.put("speed", tvSpeed.text.toString())
        val json: String = param.toString()
        Log.e("cameraLensMove", json)
        okHttpSetCamera(url, json)
    }

    /**
     * 设置光圈
     * iris：光圈速度参数,-1.0~1.0
     * focus：变焦速度参数,-1.0~1.0
     */
    private fun setIris() {
        val url = "${CONST.BASE_URL}/sky/system/device/cameraLensAdjust"
        Log.e("cameraLensAdjust", url)
        val param = JSONObject()
        param.put("deviceId", data!!.fId)
        param.put("iris", iris)
        param.put("focus", (focus/100.0f).toString())
        val json: String = param.toString()
        Log.e("cameraLensAdjust", json)
        okHttpSetCamera(url, json)
    }

    /**
     * 设置摄像机预设位置
     */
    private fun setForePosition() {
        val url = "${CONST.BASE_URL}/sky/system/device/gotoPreset"
        Log.e("gotoPreset", url)
        val param = JSONObject()
        param.put("deviceId", data!!.fId)
        param.put("presetId", tvForePosition.text.toString())
        val json: String = param.toString()
        Log.e("gotoPreset", json)
        okHttpSetCamera(url, json)
    }

    /**
     * 设置摄像头
     */
    private fun okHttpSetCamera(url: String, json: String) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(json)) {
            return
        }
        Thread {
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        Log.e("okHttpSetCamera", result)
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (!obj.getBoolean("result")) {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(this@ActivityVideoSetting, errorMessage, Toast.LENGTH_SHORT).show()
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

    private fun initWheelView() {
        ivCloseDelay.setOnClickListener(this)
        tvStartTime.setOnClickListener(this)
        tvEndTime.setOnClickListener(this)
        tvSave.setOnClickListener(this)
        tvNegtive.setOnClickListener(this)
        tvPositive.setOnClickListener(this)
        tvStartTime.text = sdf1.format(Date().time-1000*60*60)
        tvEndTime.text = sdf1.format(Date())

        val c = Calendar.getInstance()
        val curYear = c[Calendar.YEAR]
        val curMonth = c[Calendar.MONTH] + 1 //通过Calendar算出的月数要+1
        val curDate = c[Calendar.DATE]
        val curHour = c[Calendar.HOUR_OF_DAY]
        val curMinute = c[Calendar.MINUTE]
        val curSecond = c[Calendar.SECOND]

        val numericWheelAdapter1 = NumericWheelAdapter(this, 1950, curYear)
        numericWheelAdapter1.setLabel("年")
        year.viewAdapter = numericWheelAdapter1
        year.isCyclic = false //是否可循环滑动
        year.addScrollingListener(scrollListener)
        year.visibleItems = 7
        year.visibility = View.VISIBLE

        val numericWheelAdapter2 = NumericWheelAdapter(this, 1, 12, "%02d")
        numericWheelAdapter2.setLabel("月")
        month.viewAdapter = numericWheelAdapter2
        month.isCyclic = false
        month.addScrollingListener(scrollListener)
        month.visibleItems = 7
        month.visibility = View.VISIBLE

        initDay(curYear, curMonth)
        day.isCyclic = false
        day.visibleItems = 7
        day.visibility = View.VISIBLE

        val numericWheelAdapter3 = NumericWheelAdapter(this, 0, 23, "%02d")
        numericWheelAdapter3.setLabel("时")
        hour.viewAdapter = numericWheelAdapter3
        hour.isCyclic = false
        hour.addScrollingListener(scrollListener)
        hour.visibleItems = 7
        hour.visibility = View.VISIBLE

        val numericWheelAdapter4 = NumericWheelAdapter(this, 0, 59, "%02d")
        numericWheelAdapter4.setLabel("分")
        minute.viewAdapter = numericWheelAdapter4
        minute.isCyclic = false
        minute.addScrollingListener(scrollListener)
        minute.visibleItems = 7
        minute.visibility = View.VISIBLE

        val numericWheelAdapter5 = NumericWheelAdapter(this, 0, 59, "%02d")
        numericWheelAdapter5.setLabel("秒")
        second.viewAdapter = numericWheelAdapter5
        second.isCyclic = false
        second.addScrollingListener(scrollListener)
        second.visibleItems = 7
        second.visibility = View.VISIBLE

        year.currentItem = curYear - 1950
        month.currentItem = curMonth - 1
        day.currentItem = curDate - 1
        hour.currentItem = curHour
        minute.currentItem = curMinute
        second.currentItem = curSecond
    }

    private val scrollListener: OnWheelScrollListener = object : OnWheelScrollListener {
        override fun onScrollingStarted(wheel: WheelView) {}
        override fun onScrollingFinished(wheel: WheelView) {
            val nYear = year!!.currentItem + 1950 //年
            val nMonth: Int = month.currentItem + 1 //月
            initDay(nYear, nMonth)
        }
    }

    /**
     */
    private fun initDay(arg1: Int, arg2: Int) {
        val numericWheelAdapter = NumericWheelAdapter(this, 1, getDay(arg1, arg2), "%02d")
        numericWheelAdapter.setLabel("日")
        day.viewAdapter = numericWheelAdapter
    }

    /**
     * @param year
     * @param month
     * @return
     */
    private fun getDay(year: Int, month: Int): Int {
        var day = 30
        var flag = false
        flag = when (year % 4) {
            0 -> true
            else -> false
        }
        day = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (flag) 29 else 28
            else -> 30
        }
        return day
    }

    /**
     */
    private fun setTextViewValue() {
        val yearStr = (year!!.currentItem + 1950).toString()
        val monthStr = if (month.currentItem + 1 < 10) "0" + (month.currentItem + 1) else (month.currentItem + 1).toString()
        val dayStr = if (day.currentItem + 1 < 10) "0" + (day.currentItem + 1) else (day.currentItem + 1).toString()
        val hourStr = if (hour.currentItem + 1 < 10) "0" + (hour.currentItem) else (hour.currentItem).toString()
        val minuteStr = if (minute.currentItem + 1 < 10) "0" + (minute.currentItem) else (minute.currentItem).toString()
        val secondStr = if (second.currentItem + 1 < 10) "0" + (second.currentItem) else (second.currentItem).toString()
        if (isStart) {
            tvStartTime.text = "$yearStr-$monthStr-$dayStr $hourStr:$minuteStr:$secondStr"
        } else {
            tvEndTime.text = "$yearStr-$monthStr-$dayStr $hourStr:$minuteStr:$secondStr"
        }
    }

    private fun bootTimeLayoutAnimation(view: View?) {
        if (view!!.visibility == View.GONE) {
            timeLayoutAnimation(true, view)
            view!!.visibility = View.VISIBLE
        } else {
            timeLayoutAnimation(false, view)
            view!!.visibility = View.GONE
        }
    }

    /**
     * 时间图层动画
     * @param flag
     * @param view
     */
    private fun timeLayoutAnimation(flag: Boolean, view: View?) {
        //列表动画
        val animationSet = AnimationSet(true)
        val animation: TranslateAnimation = if (!flag) {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f)
        } else {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f)
        }
        animation.duration = 200
        animationSet.addAnimation(animation)
        animationSet.fillAfter = true
        view!!.startAnimation(animationSet)
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                view.clearAnimation()
            }
        })
    }

    /**
     * 合并延时视频
     */
    private fun okHttpMergeDelayVideo() {
        showDialog()
        Thread {
            val url = "${CONST.BASE_URL}/jinyun/device/videoSynthesis"
            Log.e("videoSynthesis", url)
            val param = JSONObject()
            param.put("deviceInfoId", data!!.deviceInfoId.toLong())
            param.put("startTime", tvStartTime.text.toString())
            param.put("endTime", tvEndTime.text.toString())
            param.put("watermarkUrl", "")
            val json: String = param.toString()
            Log.e("videoSynthesis", json)
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    Log.e("videoSynthesis", result)
                    runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        layoutDelayVideo.visibility = View.GONE
                                        if (!obj.isNull("data")) {
                                            val dataStr = obj.getString("data")
                                            DownloadVideoUtil.intoDownloadManager(this@ActivityVideoSetting, dataStr)
                                        }
                                    } else {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(this@ActivityVideoSetting, errorMessage, Toast.LENGTH_SHORT).show()
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
