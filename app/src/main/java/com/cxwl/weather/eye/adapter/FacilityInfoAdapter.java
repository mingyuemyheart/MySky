package com.cxwl.weather.eye.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;

public class FacilityInfoAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<EyeDto> mArrayList = new ArrayList<EyeDto>();
	
	private final class ViewHolder{
		TextView tvNumber;
		TextView tvLocation;
		TextView tvGroup;
	}
	
	private ViewHolder mHolder = null;
	
	public FacilityInfoAdapter(Context context, List<EyeDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_facilityinfo, null);
			mHolder = new ViewHolder();
			mHolder.tvNumber = (TextView) convertView.findViewById(R.id.tvNumber);
			mHolder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
			mHolder.tvGroup = (TextView) convertView.findViewById(R.id.tvGroup);
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
