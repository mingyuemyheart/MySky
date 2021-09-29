package com.cxwl.weather.eye.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.dto.EyeDto

/**
 * 设置预位置
 */
class ForePositionAdapter constructor(private val context: Context?, private var mArrayList: List<EyeDto>?) : BaseAdapter() {

    private val mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private class ViewHolder {
        var tvForePosition: TextView? = null
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
        var mHolder: ViewHolder? = null
        var convertView = view
        if (convertView == null) {
            convertView = mInflater!!.inflate(R.layout.adapter_fore_position, null)
            mHolder = ViewHolder()
            mHolder!!.tvForePosition = convertView.findViewById<View>(R.id.tvForePosition) as TextView
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        try {
            val dto = mArrayList!![position]
            if (!TextUtils.isEmpty(dto.forePosition)) {
                mHolder!!.tvForePosition!!.text = dto.forePosition
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        return convertView
    }

}
