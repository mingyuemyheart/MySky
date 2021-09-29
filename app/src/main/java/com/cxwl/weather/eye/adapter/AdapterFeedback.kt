package com.cxwl.weather.eye.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.dto.RemoveListener
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

/**
 * 意见反馈
 */
class AdapterFeedback(private val context: Context?, private val mArrayList: ArrayList<EyeDto>?) : BaseAdapter() {

    private var mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var removeListener: RemoveListener? = null

    fun setRemoveListener(removeListener: RemoveListener?) {
        this.removeListener = removeListener
    }

    private class ViewHolder {
        var imageView: ImageView? = null
        var ivRemove: ImageView? = null
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
            convertView = mInflater!!.inflate(R.layout.adapter_feedback, null)
            mHolder = ViewHolder()
            mHolder.imageView = convertView.findViewById(R.id.imageView)
            mHolder.ivRemove = convertView.findViewById(R.id.ivRemove)
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList!![position]
        if (TextUtils.isEmpty(dto.pictureUrl)) {
            mHolder.imageView!!.setImageResource(R.drawable.icon_add_pic)
            mHolder.ivRemove!!.visibility = View.INVISIBLE
        } else {
            if (dto.pictureUrl.startsWith("http")) {
                Picasso.get().load(dto.pictureUrl).centerCrop().resize(200, 200).into(mHolder.imageView)
            } else {
                val file = File(dto.pictureUrl)
                if (file.exists()) {
                    Picasso.get().load(file).centerCrop().resize(200, 200).into(mHolder.imageView)
                }
            }
            mHolder.ivRemove!!.visibility = View.VISIBLE
            mHolder.ivRemove!!.setOnClickListener {
                if (removeListener != null) {
                    removeListener!!.removeComplete(position)
                }
            }
        }

        return convertView
    }

}
