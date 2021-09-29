package com.cxwl.weather.eye.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.activity.ActivityPictureWall
import com.cxwl.weather.eye.activity.ActivityVideoPreview
import com.cxwl.weather.eye.adapter.AdapterCalendar
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.CommonUtil
import com.cxwl.weather.eye.utils.OkHttpUtil
import com.cxwl.weather.eye.view.wheelview.NumericWheelAdapter
import com.cxwl.weather.eye.view.wheelview.OnWheelScrollListener
import com.cxwl.weather.eye.view.wheelview.WheelView
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.layout_date.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 天气日历
 */
class FragmentCalendar : BaseFragment(), OnClickListener {

    private var data: EyeDto? = null
    private var mAdapter: AdapterCalendar? = null
    private val dataList: ArrayList<EyeDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("MM月dd日", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("MM月", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyy年", Locale.CHINA)
    private var timeType = "0" //时间类型(年2,月1,日0)
    private var startTime = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        initWheelView()
        initGridView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        tvTime.setOnClickListener(this)
        tvNegtive.setOnClickListener(this)
        tvPositive.setOnClickListener(this)

        llContainer!!.removeAllViews()
        val timeList: ArrayList<String> = ArrayList()
        timeList.add("日")
        timeList.add("月")
        timeList.add("年")
        for (i in 0 until timeList.size) {
            val tvName = TextView(activity)
            tvName.text = timeList[i]
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            if (i == 0) {
                timeType = i.toString()
                startTime = sdf1.format(Date())
                tvTime.text = "${sdf2.format(Date())} >"
                tvName.setTextColor(Color.WHITE)
                tvName.setBackgroundResource(R.drawable.bg_corner_select)
            } else {
                tvName.setTextColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
                tvName.setBackgroundColor(Color.TRANSPARENT)
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.width = CommonUtil.dip2px(activity, 80f).toInt()
            params.height = CommonUtil.dip2px(activity, 30f).toInt()
            tvName.layoutParams = params
            llContainer.addView(tvName)
            tvName.setOnClickListener {
                for (j in 0 until llContainer.childCount) {
                    val name = llContainer.getChildAt(j) as TextView
                    if (TextUtils.equals(name.text.toString(), tvName.text.toString())) {
                        name.setTextColor(Color.WHITE)
                        name.setBackgroundResource(R.drawable.bg_corner_select)
                        timeType = j.toString()
                        when(timeType) {
                            "0" -> {
                                year.visibility = View.VISIBLE
                                month.visibility = View.VISIBLE
                                day.visibility = View.VISIBLE
                                tvTime.text = "${sdf2.format(sdf1.parse(startTime))} >"
                            }
                            "1" -> {
                                year.visibility = View.VISIBLE
                                month.visibility = View.VISIBLE
                                day.visibility = View.GONE
                                tvTime.text = "${sdf3.format(sdf1.parse(startTime))} >"
                            }
                            "2" -> {
                                year.visibility = View.VISIBLE
                                month.visibility = View.GONE
                                day.visibility = View.GONE
                                tvTime.text = "${sdf4.format(sdf1.parse(startTime))} >"
                            }
                        }
                    } else {
                        name.setTextColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
                        name.setBackgroundColor(Color.TRANSPARENT)
                    }
                }
            }
        }

        data = arguments!!.getParcelable("data")
        if (data != null) {
            okHttpList()
        }
    }

    private fun initGridView() {
        mAdapter = AdapterCalendar(activity, dataList)
        gridView!!.adapter = mAdapter
        gridView.setOnItemClickListener { parent, view, position, id ->
            val dto = dataList[position]
            var intent: Intent? = null
            if (TextUtils.equals(dto.fileType, "0")) {//图片
                intent = Intent(activity, ActivityPictureWall::class.java)
            } else {
                intent = Intent(activity, ActivityVideoPreview::class.java)
            }
            intent.putExtra("selectIndex", position)
            val bundle = Bundle()
            bundle.putParcelableArrayList("dataList", dataList)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvTime, R.id.tvNegtive -> {
                bootTimeLayoutAnimation()
            }
            R.id.tvPositive -> {
                setTextViewValue()
                bootTimeLayoutAnimation()
            }
        }
    }

    /**
     * 获取天气日历数据
     */
    private fun okHttpList() {
        showDialog()
        Thread {
            val fileType = arguments!!.getString("fileType")
            val url = "${CONST.BASE_URL}/sky/system/media/getListByTimeType"
            Log.e("getListByTimeType", url)
            val param = JSONObject()
            param.put("deviceInfoId", data!!.deviceInfoId)
            param.put("fileType", fileType)//文件类型(0 图片,1视频)
            param.put("saveType", "0")//存储类型（0：自动生成，1：用户存储，2：水印）
            param.put("timeType", timeType)//时间类型(年2,月1,日0)
            param.put("startTime", startTime)
//            param.put("pageNo", "1")
//            param.put("pageSize", "20")
            val json: String = param.toString()
            Log.e("getListByTimeType", json)
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    if (!isAdded) {
                        return
                    }
                    val result = response.body!!.string()
                    Log.e("getListByTimeType", result)
                    activity!!.runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        if (!obj.isNull("data")) {
                                            val dataObj = obj.getJSONObject("data")
                                            if (!dataObj.isNull("list")) {
                                                dataList.clear()
                                                val array = dataObj.getJSONArray("list")
                                                for (i in 0 until array.length()) {
                                                    val dto = EyeDto()
                                                    val itemObj = array.getJSONObject(i)
                                                    if (!itemObj.isNull("fileType")) {
                                                        dto.fileType = itemObj.getString("fileType")
                                                    }
                                                    if (!itemObj.isNull("createTime")) {
                                                        dto.time = itemObj.getString("createTime")
                                                    }
                                                    if (!itemObj.isNull("deviceName")) {
                                                        dto.location = itemObj.getString("deviceName")
                                                    }
                                                    when(fileType) {
                                                        "0" -> {//图片
                                                            if (!itemObj.isNull("imageUrl")) {
                                                                dto.pictureUrl = itemObj.getString("imageUrl")
                                                            }
                                                        }
                                                        "1" -> {//视频
                                                            if (!itemObj.isNull("iconUrl")) {
                                                                dto.videoThumbUrl = itemObj.getString("iconUrl")
                                                            }
                                                            if (!itemObj.isNull("videoUrl")) {
                                                                dto.videoUrl = itemObj.getString("videoUrl")
                                                            }
                                                            if (!itemObj.isNull("videoDuration")) {
                                                                dto.videoDuration = itemObj.getString("videoDuration")
                                                            }
                                                        }
                                                    }
                                                    dataList.add(dto)
                                                }
                                                if (mAdapter != null) {
                                                    mAdapter!!.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                    } else {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
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
        val c = Calendar.getInstance()
        val curYear = c[Calendar.YEAR]
        val curMonth = c[Calendar.MONTH] + 1 //通过Calendar算出的月数要+1
        val curDate = c[Calendar.DATE]
        val curHour = c[Calendar.HOUR_OF_DAY]
        val curMinute = c[Calendar.MINUTE]
        val curSecond = c[Calendar.SECOND]

        val numericWheelAdapter1 = NumericWheelAdapter(activity, 1950, curYear)
        numericWheelAdapter1.setLabel("年")
        year.viewAdapter = numericWheelAdapter1
        year.isCyclic = false //是否可循环滑动
        year.addScrollingListener(scrollListener)
        year.visibleItems = 7
        year.visibility = View.VISIBLE

        val numericWheelAdapter2 = NumericWheelAdapter(activity, 1, 12, "%02d")
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

        val numericWheelAdapter3 = NumericWheelAdapter(activity, 0, 23, "%02d")
        numericWheelAdapter3.setLabel("时")
        hour.viewAdapter = numericWheelAdapter3
        hour.isCyclic = false
        hour.addScrollingListener(scrollListener)
        hour.visibleItems = 7
//        hour.visibility = View.VISIBLE

        val numericWheelAdapter4 = NumericWheelAdapter(activity, 0, 59, "%02d")
        numericWheelAdapter4.setLabel("分")
        minute.viewAdapter = numericWheelAdapter4
        minute.isCyclic = false
        minute.addScrollingListener(scrollListener)
        minute.visibleItems = 7
//        minute.visibility = View.VISIBLE

        val numericWheelAdapter5 = NumericWheelAdapter(activity, 0, 59, "%02d")
        numericWheelAdapter5.setLabel("秒")
        second.viewAdapter = numericWheelAdapter5
        second.isCyclic = false
        second.addScrollingListener(scrollListener)
        second.visibleItems = 7
//        second.visibility = View.VISIBLE

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
        val numericWheelAdapter = NumericWheelAdapter(activity, 1, getDay(arg1, arg2), "%02d")
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
        when(timeType) {
            "0" -> {
                tvTime.text = "${monthStr}月${dayStr}日 >"
                startTime = "$yearStr-$monthStr-$dayStr 00:00:00"
            }
            "1" -> {
                tvTime.text = "${monthStr}月 >"
                startTime = "$yearStr-$monthStr-$dayStr 00:00:00"
            }
            "2" -> {
                tvTime.text = "${yearStr}年 >"
                startTime = "$yearStr-$monthStr-$dayStr ${hourStr}:${minuteStr}:${secondStr}"
            }
        }
        okHttpList()
    }

    private fun bootTimeLayoutAnimation() {
        if (layoutDate!!.visibility == View.GONE) {
            timeLayoutAnimation(true, layoutDate)
            layoutDate!!.visibility = View.VISIBLE
        } else {
            timeLayoutAnimation(false, layoutDate)
            layoutDate!!.visibility = View.GONE
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

}
