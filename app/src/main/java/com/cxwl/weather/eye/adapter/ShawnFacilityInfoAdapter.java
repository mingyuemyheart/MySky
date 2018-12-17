package com.cxwl.weather.eye.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;

import java.util.List;

/**
 * 设备信息
 */
public class ShawnFacilityInfoAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<EyeDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvNumber,tvLocation,tvGroup;
	}
	
	public ShawnFacilityInfoAdapter(Context context, List<EyeDto> mArrayList) {
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_facilityinfo, null);
			mHolder = new ViewHolder();
			mHolder.tvNumber = convertView.findViewById(R.id.tvNumber);
			mHolder.tvLocation = convertView.findViewById(R.id.tvLocation);
			mHolder.tvGroup = convertView.findViewById(R.id.tvGroup);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			EyeDto dto = mArrayList.get(position);
			if (!TextUtils.isEmpty(dto.fNumber)) {
				mHolder.tvNumber.setText("编号："+dto.fNumber);
			}
			if (!TextUtils.isEmpty(dto.location)) {
				mHolder.tvLocation.setText(dto.location);
			}
			if (!TextUtils.isEmpty(dto.fGroupName)) {
				mHolder.tvGroup.setText(dto.fGroupName);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
