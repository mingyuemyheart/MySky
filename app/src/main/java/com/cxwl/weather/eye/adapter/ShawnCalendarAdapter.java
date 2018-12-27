package com.cxwl.weather.eye.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.view.ShawnRotateLineView;
import com.cxwl.weather.eye.view.ShawnRotateLineView2;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 天气日历
 */
public class ShawnCalendarAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<EyeDto> mArrayList;
	private int width;

	private final class ViewHolder{
		ImageView imageView;
		ShawnRotateLineView2 rotateLineView;
	}

	public ShawnCalendarAdapter(Context context, List<EyeDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getHeight();
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_calendar, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.rotateLineView = convertView.findViewById(R.id.rotateLineView);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			EyeDto dto = mArrayList.get(position);

			int itemWidth = width/6;
			ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = itemWidth;
			params.height = itemWidth*9/16;
			mHolder.imageView.setLayoutParams(params);
			if (!TextUtils.isEmpty(dto.pictureUrl)) {
				Picasso.get().load(dto.pictureUrl).error(R.drawable.shawn_icon_seat_bitmap).into(mHolder.imageView);
			}else {
				mHolder.imageView.setImageResource(R.drawable.shawn_icon_seat_bitmap);
			}

			ViewGroup.LayoutParams params2 = mHolder.rotateLineView.getLayoutParams();
			params2.width = itemWidth/2;
			params2.height = itemWidth/2;
			mHolder.rotateLineView.setLayoutParams(params2);
			if (!TextUtils.isEmpty(dto.time)) {
				mHolder.rotateLineView.setData(dto.time);
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
