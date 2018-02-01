package com.cxwl.weather.eye.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;

/**
 * 视频列表
 */

public class VideoListAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<EyeDto> mArrayList = new ArrayList<>();
	private int width = 0;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvLocation;
		TextView tvErectTime;
	}
	
	private ViewHolder mHolder = null;
	
	public VideoListAdapter(Context context, List<EyeDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_videolist, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
			mHolder.tvErectTime = (TextView) convertView.findViewById(R.id.tvErectTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			EyeDto dto = mArrayList.get(position);
			if (!TextUtils.isEmpty(dto.location)) {
				mHolder.tvLocation.setText(dto.location);
			}
			if (!TextUtils.isEmpty(dto.erectTime)) {
				try {
					mHolder.tvErectTime.setText("架设时间："+sdf1.format(sdf2.parse(dto.erectTime)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (!TextUtils.isEmpty(dto.videoThumbUrl)) {
				FinalBitmap finalBitmap = FinalBitmap.create(mContext);
				finalBitmap.display(mHolder.imageView, dto.videoThumbUrl, null, 0);
			}else {
				mHolder.imageView.setImageResource(R.drawable.eye_bg_thumb);
			}
			int height = width*9/16;
			LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = width;
			params.height = height;
			mHolder.imageView.setLayoutParams(params);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
