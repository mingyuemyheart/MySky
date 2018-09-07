package com.cxwl.weather.eye.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;

import net.tsz.afinal.FinalBitmap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 照片墙
 */

public class PictureWallAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<EyeDto> mArrayList = new ArrayList<>();
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private int width = 0;
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public PictureWallAdapter(Context context, List<EyeDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_picturewall, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
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
				FinalBitmap finalBitmap = FinalBitmap.create(mContext);
				finalBitmap.display(mHolder.imageView, dto.pictureThumbUrl, null, 0);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width/3, width/3*3/4);
				mHolder.imageView.setLayoutParams(params);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}