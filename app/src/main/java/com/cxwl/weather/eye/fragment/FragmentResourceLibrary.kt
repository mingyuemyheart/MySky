package com.cxwl.weather.eye.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.activity.ActivityDeviceList
import com.cxwl.weather.eye.adapter.AdapterHomeList
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.OkHttpUtil
import kotlinx.android.synthetic.main.layout_device_list.*
import kotlinx.android.synthetic.main.layout_title2.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 资源库
 */
class FragmentResourceLibrary : BaseFragment() {

    private var mAdapter: AdapterHomeList? = null
    private val dataList: ArrayList<EyeDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_resource_library, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefreshLayout()
        initWidget()
        initListView()
    }

    private fun initWidget() {
        tvTitle.text = "资源库"
        llBack.visibility = View.GONE
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
        mAdapter = AdapterHomeList(activity, dataList)
        listView.adapter = mAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
//            if (CommonUtil.showExperienceTime(activity)) {
//                CommonUtil.dialogExpericence(activity)
//            } else {
            val dto = dataList[arg2]
            val intent = Intent(activity, ActivityDeviceList::class.java)
            intent.putExtra("isShowDetail", false)
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            intent.putExtras(bundle)
            startActivity(intent)
//            }
        }
        okHttpList()
    }

    private fun okHttpList() {
        Thread {
            val url = "${CONST.BASE_URL}/sky/system/group/pageList"
            Log.e("pageList", url)
            val param = JSONObject()
            val json: String = param.toString()
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    if (!isAdded) {
                        return
                    }
                    val result = response.body!!.string()
                    Log.e("pageList", result)
                    activity!!.runOnUiThread {
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
                                                for (i in 0 until array.length()) {
                                                    val itemObj = array.getJSONObject(i)
                                                    val dto = EyeDto()
                                                    if (!itemObj.isNull("groupId")) {
                                                        dto.fGroupId = itemObj.getString("groupId")
                                                    }
                                                    if (!itemObj.isNull("groupName")) {
                                                        dto.fGroupName = itemObj.getString("groupName")
                                                    }
                                                    if (!itemObj.isNull("iconUrl")) {
                                                        dto.videoThumbUrl = itemObj.getString("iconUrl")
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

}
