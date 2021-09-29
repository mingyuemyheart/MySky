package com.cxwl.weather.eye.adapter

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

/**
 * 图集展示
 */
class AdapterPictureWall constructor(private val context: Context?, private val mArrayList: ArrayList<EyeDto>?) : BaseAdapter() {

	private var mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

	private class ViewHolder {
		var imageView: ImageView? = null
		var tvDuration: TextView? = null
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
			convertView = mInflater!!.inflate(R.layout.adapter_calendar, null)
			mHolder = ViewHolder()
			mHolder.imageView = convertView.findViewById(R.id.imageView)
			mHolder.tvDuration = convertView.findViewById(R.id.tvDuration)
			convertView.tag = mHolder
		} else {
			mHolder = convertView.tag as ViewHolder
		}

		val dto = mArrayList!![position]
		val itemWidth = CommonUtil.widthPixels(context)/4
		val params = mHolder.imageView!!.layoutParams
		params.width = itemWidth
		params.height = itemWidth * 10 / 16
		mHolder.imageView!!.layoutParams = params

		when(dto.fileType) {
			"0" -> {//图片
				if (!TextUtils.isEmpty(dto.pictureUrl)) {
					Picasso.get().load(dto.pictureUrl).error(R.drawable.icon_seat_bitmap).into(mHolder.imageView)
				} else {
					mHolder.imageView!!.setImageResource(R.drawable.icon_seat_bitmap)
				}
			}
			"1" -> {//视频
				if (!TextUtils.isEmpty(dto.videoThumbUrl)) {
					Picasso.get().load(dto.videoThumbUrl).error(R.drawable.icon_seat_bitmap).into(mHolder.imageView)
				} else {
					mHolder.imageView!!.setImageResource(R.drawable.icon_seat_bitmap)
				}
				if (dto.videoDuration != null) {
					mHolder.tvDuration!!.text = dto.videoDuration
				}
			}
		}

		return convertView
	}

}
