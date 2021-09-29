package com.cxwl.weather.eye.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.Toast
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.adapter.AdapterDeviceList
import com.cxwl.weather.eye.adapter.AdapterDeviceSetting
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.CommonUtil
import com.cxwl.weather.eye.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_device_list.*
import kotlinx.android.synthetic.main.fragment_home_list.*
import kotlinx.android.synthetic.main.layout_device_setting.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 设备列表
 */
class ActivityDeviceList : BaseFragmentActivity(), OnClickListener {

    private var data: EyeDto? = null
    private var mAdapter: AdapterDeviceList? = null
    private val dataList: ArrayList<EyeDto> = ArrayList()
    private var settingAdapter: AdapterDeviceSetting? = null
    private val settingList: ArrayList<EyeDto> = ArrayList()
    private var deviceStatus = ""//保存设备列表开关数据
    private var isShowDetail = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        initRefreshLayout()
        initWidget()
        initListView()
        initListViewSetting()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivControl.setImageResource(R.drawable.icon_device_setting)
        ivControl.visibility = View.VISIBLE
        ivControl.setOnClickListener(this)
        llColse.setOnClickListener(this)

        if (intent.hasExtra("data")) {
            data = intent.getParcelableExtra("data")
            if (data != null) {
                if (data!!.fGroupName != null) {
                    tvTitle.text = data!!.fGroupName
                }
            }
        }
        if (intent.hasExtra("isShowDetail")) {
            isShowDetail = intent.getBooleanExtra("isShowDetail", true)
        }

        getDeviceStatus()

        //侧滑页面
        drawerlayout.visibility = View.VISIBLE
        val params1 = clEnd.layoutParams
        params1.width = CommonUtil.widthPixels(this) - CommonUtil.dip2px(this, 50f).toInt()
        clEnd.layoutParams = params1
    }

    /**
     * 初始化下拉刷新布局
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 400)
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener { okHttpList() }
    }

    private fun initListView() {
        mAdapter = AdapterDeviceList(this, dataList)
        listView.adapter = mAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
//            if (CommonUtil.showExperienceTime(this)) {
//                CommonUtil.dialogExpericence(this)
//            } else {
                val dto = dataList[arg2]
                val intent = if (isShowDetail) {
                    Intent(this, ActivityVideoSetting::class.java)
                } else {
                    Intent(this, ActivityCalendarVideo::class.java)
                }
                val bundle = Bundle()
                bundle.putParcelable("data", dto)
                intent.putExtras(bundle)
                startActivity(intent)
//            }
        }
        okHttpList()
    }

    private fun initListViewSetting() {
        settingAdapter = AdapterDeviceSetting(this, settingList)
        listViewSetting.adapter = settingAdapter
        settingAdapter!!.setSwitchChangeListner(object : AdapterDeviceSetting.SwitchChangeListner {
            override fun onChange(dto: EyeDto) {
                Log.e("onChange", dto.location)
                dataList.clear()
                for (i in 0 until settingList.size) {
                    val set = settingList[i]
                    if (set.isShow) {
                        dataList.add(set)
                    }
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    private fun okHttpList() {
        Thread {
            val url = "${CONST.BASE_URL}/sky/system/device/selectPageForAdmin"
            Log.e("selectPageForAdmin", url)
            val param = JSONObject()
            param.put("deviceGroupIds", data!!.fGroupId)
            val json: String = param.toString()
            Log.e("selectPageForAdmin", json)
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    Log.e("selectPageForAdmin", result)
                    runOnUiThread {
                        refreshLayout.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        if (!obj.isNull("data")) {
                                            val dataObj = obj.getJSONObject("data");
                                            if (!dataObj.isNull("list")) {
                                                val array = dataObj.getJSONArray("list")
                                                dataList.clear()
                                                settingList.clear()
                                                for (i in 0 until array.length()) {
                                                    val itemObj = array.getJSONObject(i)
                                                    val dto = EyeDto()
                                                    if (!itemObj.isNull("deviceInfoId")) {
                                                        dto.deviceInfoId = itemObj.getString("deviceInfoId")
                                                    }
                                                    if (!itemObj.isNull("deviceId")) {
                                                        dto.fId = itemObj.getString("deviceId")
                                                    }
                                                    if (!itemObj.isNull("name")) {
                                                        dto.location = itemObj.getString("name")
                                                    }
                                                    if (!itemObj.isNull("iconUrl")) {
                                                        dto.videoThumbUrl = itemObj.getString("iconUrl")
                                                    }
                                                    if (!itemObj.isNull("streamStatus")) {
                                                        dto.streamStatus = itemObj.getString("streamStatus")
                                                    }
                                                    if (!deviceStatus.contains(dto.fId)) {
                                                        dto.isShow = true
                                                        dataList.add(dto)
                                                    } else {
                                                        dto.isShow = false
                                                    }
                                                    settingList.add(dto)
                                                }
                                                if (mAdapter != null) {
                                                    mAdapter!!.notifyDataSetChanged()
                                                }
                                                if (settingAdapter != null) {
                                                    settingAdapter!!.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                    } else {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(this@ActivityDeviceList, errorMessage, Toast.LENGTH_SHORT).show()
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerlayout != null) {
                if (drawerlayout!!.isDrawerOpen(clEnd!!)) {
                    drawerlayout!!.closeDrawer(clEnd!!)
                } else {
                    save()
                    finish()
                }
            } else {
                save()
                finish()
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> {
                if (drawerlayout != null) {
                    if (drawerlayout!!.isDrawerOpen(clEnd!!)) {
                        drawerlayout!!.closeDrawer(clEnd!!)
                    } else {
                        save()
                        finish()
                    }
                } else {
                    save()
                    finish()
                }
            }
            R.id.ivControl, R.id.llColse -> {
                if (drawerlayout!!.isDrawerOpen(clEnd!!)) {
                    drawerlayout!!.closeDrawer(clEnd!!)
                } else {
                    drawerlayout!!.openDrawer(clEnd!!)
                }
            }
        }
    }

    private fun save() {
        clearDeviceStatus()
        for (i in 0 until settingList.size) {
            val set = settingList[i]
            if (!set.isShow) {
                deviceStatus += set.fId+","
            }
        }
        saveDeviceStatus()
    }

    /**
     * 保存设备状态
     */
    private fun clearDeviceStatus() {
        deviceStatus = ""
        val sharedPreferences = getSharedPreferences("DEVICE_STATUS", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()//去掉之前结果
        editor.apply()
    }

    /**
     * 保存设备状态
     */
    private fun saveDeviceStatus() {
        val sharedPreferences = getSharedPreferences("DEVICE_STATUS", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("deviceStatus", deviceStatus)
        editor.apply()
        Log.e("deviceStatus", deviceStatus)
    }

    /**
     * 获取设备状态
     */
    private fun getDeviceStatus() {
        val sharedPreferences = getSharedPreferences("DEVICE_STATUS", MODE_PRIVATE)
        deviceStatus = sharedPreferences.getString("deviceStatus", "")
        Log.e("deviceStatus", deviceStatus)
    }

}
