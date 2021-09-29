package com.cxwl.weather.eye.adapter

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.CommonUtil
import com.squareup.picasso.Picasso
import java.util.*

/**
 * 设备列表
 */
class AdapterDeviceList constructor(private var activity: Activity?, private val mArrayList: ArrayList<EyeDto>?) : BaseAdapter() {

    private val mInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private class ViewHolder {
        var imageView: ImageView? = null
        var ivStatus: ImageView? = null
        var tvName: TextView? = null
    }

    override fun getCount(): Int {
        return mArrayList!!.size
    }

    override fun getItem(position: Int): Any? {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        val mHolder: ViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_device_list, null)
            mHolder = ViewHolder()
            mHolder.imageView = convertView.findViewById(R.id.imageView)
            mHolder.ivStatus = convertView.findViewById(R.id.ivStatus)
            mHolder.tvName = convertView.findViewById(R.id.tvName)
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList!![position]

        if (dto.location != null) {
            mHolder.tvName!!.text = dto.location
        }

        if (TextUtils.equals(dto.streamStatus, "1")) {
            mHolder.ivStatus!!.setImageResource(R.drawable.point_green)
        } else {
            mHolder.ivStatus!!.setImageResource(R.drawable.point_red)
        }

        if (!TextUtils.isEmpty(dto.videoThumbUrl)) {
            Picasso.get().load(dto.videoThumbUrl).error(R.drawable.icon_seat_bitmap).into(mHolder.imageView)
        } else {
            mHolder.imageView!!.setImageResource(R.drawable.icon_seat_bitmap)
        }
        val params = mHolder.imageView!!.layoutParams
        params.width = CommonUtil.widthPixels(activity)
        params.height = CommonUtil.widthPixels(activity)/2
        mHolder.imageView!!.layoutParams = params
        return convertView
    }

}
