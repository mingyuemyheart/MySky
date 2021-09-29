package com.cxwl.weather.eye.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnMarkerClickListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.ScaleAnimation
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.activity.ActivityVideoSetting
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_home_map.*
import kotlinx.android.synthetic.main.layout_marker_icon.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 首页-地图
 */
class FragmentHomeMap : BaseFragment(), OnClickListener, OnMarkerClickListener {

    private var aMap: AMap? = null
    private val dataList: MutableList<EyeDto> = ArrayList()
    private val markerList: MutableList<Marker> = ArrayList()
    private var zoom = 3.7f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_map, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap(savedInstanceState)
        initWidget()
    }

    private fun initWidget() {
        ivRefresh.setOnClickListener(this)
        ivZoomIn.setOnClickListener(this)
        ivZoomOut.setOnClickListener(this)
        ivLocation.setOnClickListener(this)
    }

    /**
     * 初始化地图
     */
    private fun initMap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(CONST.locationLat, CONST.locationLng), zoom))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapLoadedListener {
            okHttpList()
        }
    }

    private fun okHttpList() {
        Thread {
            val url = "${CONST.BASE_URL}/sky/system/media/listAll"
            Log.e("listAll", url)
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
                    Log.e("listAll", result)
                    activity!!.runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        if (!obj.isNull("data")) {
                                            val array = obj.getJSONArray("data")
                                            dataList.clear()
                                            for (i in 0 until array.length()) {
                                                val itemObj = array.getJSONObject(i)
                                                val dto = EyeDto()
                                                if (!itemObj.isNull("deviceInfoId")) {
                                                    dto.deviceInfoId = itemObj.getString("deviceInfoId")
                                                }
                                                if (!itemObj.isNull("deviceId")) {
                                                    dto.fId = itemObj.getString("deviceId")
                                                }
                                                if (!itemObj.isNull("latitude")) {
                                                    dto.lat = itemObj.getString("latitude")
                                                }
                                                if (!itemObj.isNull("longitude")) {
                                                    dto.lng = itemObj.getString("longitude")
                                                }
                                                if (!itemObj.isNull("name")) {
                                                    dto.location = itemObj.getString("name")
                                                }
                                                dataList.add(dto)
                                            }
                                            addMarkers()
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

    private fun removeMarkers() {
        for (marker in markerList) {
            marker.remove()
        }
        markerList.clear()
    }

    private fun addMarkers() {
        Thread {
            removeMarkers()
            val builder = LatLngBounds.Builder()
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            for (i in dataList.indices) {
                val dto = dataList[i]
                val options = MarkerOptions()
                if (!TextUtils.isEmpty(dto.fId)) {
                    options.snippet(dto.fId)
                }
                options.anchor(0.5f, 0.5f)
                if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
                    val latLng = LatLng(java.lang.Double.valueOf(dto.lat), java.lang.Double.valueOf(dto.lng))
                    builder.include(latLng)
                    options.position(latLng)
                }
                val view = inflater.inflate(R.layout.layout_marker_icon, null)
                if (!TextUtils.isEmpty(dto.location)) {
                    view.tvMarker.text = dto.location.trim { it <= ' ' }
                    view.tvMarker.visibility = View.VISIBLE
                }
                options.icon(BitmapDescriptorFactory.fromView(view))
                val marker = aMap!!.addMarker(options)
                if (marker != null) {
                    markerList.add(marker)
                    val animation: Animation = ScaleAnimation(0f, 1f, 0f, 1f)
                    animation.setInterpolator(LinearInterpolator())
                    animation.setDuration(400)
                    marker.setAnimation(animation)
                    marker.startAnimation()
                }
            }
            if (dataList.size > 0) {
                aMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
            }
        }.start()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
//        if (CommonUtil.showExperienceTime(activity)) {
//            CommonUtil.dialogExpericence(activity)
//        } else {
            for (i in dataList.indices) {
                val dto = dataList[i]
                if (TextUtils.equals(dto.fId, marker.snippet)) {
                    val intent = Intent(activity, ActivityVideoSetting::class.java)
                    val bundle = Bundle()
                    bundle.putParcelable("data", dto)
                    intent.putExtras(bundle)
                    startActivity(intent)
                    break
                }
            }
//        }
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivRefresh -> okHttpList()
            R.id.ivZoomIn -> aMap!!.animateCamera(CameraUpdateFactory.zoomIn())
            R.id.ivZoomOut -> aMap!!.animateCamera(CameraUpdateFactory.zoomOut())
            R.id.ivLocation -> if (zoom < 10f) {
                ivLocation!!.setImageResource(R.drawable.icon_warning_location_press)
                zoom = 10.0f
                aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(CONST.locationLat, CONST.locationLng), zoom))
            } else {
                ivLocation!!.setImageResource(R.drawable.icon_warning_location)
                zoom = 3.7f
                aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(CONST.locationLat, CONST.locationLng), zoom))
            }
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

}
