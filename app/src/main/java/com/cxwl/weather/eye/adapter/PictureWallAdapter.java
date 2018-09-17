package com.cxwl.weather.eye.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 照片墙
 */
public class PictureWallAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<EyeDto> mArrayList;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
	private int width;
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvTime;
	}
	
	public PictureWallAdapter(Context context, List<EyeDto> mArrayList) {
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_picturewall, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			EyeDto dto = mArrayList.get(position);
			if (!TextUtils.isEmpty(dto.pictureTime)) {
				mHolder.tvTime.setText(sdf.format(new Date(Long.valueOf(dto.pictureTime)*1000)));
			}
			if (!TextUtils.isEmpty(dto.pictureThumbUrl)) {
				Picasso.get().load(dto.pictureThumbUrl).into(mHolder.imageView);
				ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
				params.width = width/3;
				params.height = width/3*3/4;
				mHolder.imageView.setLayoutParams(params);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
