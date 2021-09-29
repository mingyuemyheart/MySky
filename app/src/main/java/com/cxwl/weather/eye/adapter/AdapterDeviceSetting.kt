package com.cxwl.weather.eye.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Switch
import android.widget.TextView
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.dto.EyeDto
import java.util.*

/**
 * 设备列表-设置
 */
class AdapterDeviceSetting constructor(private var activity: Activity?, private val mArrayList: ArrayList<EyeDto>?) : BaseAdapter() {

    private val mInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var switchChangeListner: SwitchChangeListner? = null

    fun setSwitchChangeListner(switchChangeListner: SwitchChangeListner?) {
        this.switchChangeListner = switchChangeListner
    }

    private class ViewHolder {
        var sw: Switch? = null
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
            convertView = mInflater.inflate(R.layout.adapter_device_settinng, null)
            mHolder = ViewHolder()
            mHolder.sw = convertView.findViewById(R.id.sw)
            mHolder.tvName = convertView.findViewById(R.id.tvName)
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList!![position]

        if (dto.location != null) {
            mHolder.tvName!!.text = dto.location
        }

        mHolder.sw!!.isChecked = dto.isShow
        mHolder.sw!!.setOnCheckedChangeListener { buttonView, isChecked ->
            dto.isShow = isChecked
            switchChangeListner!!.onChange(dto)
        }

        return convertView
    }

    interface SwitchChangeListner {
        fun onChange(dto: EyeDto)
    }

}
