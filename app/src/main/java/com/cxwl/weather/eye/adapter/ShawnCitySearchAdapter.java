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
 * 城市搜索
 */
public class ShawnCitySearchAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<EyeDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvName;
	}
	
	public ShawnCitySearchAdapter(Context context, List<EyeDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_city_search, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			EyeDto dto = mArrayList.get(position);
			String pro = "";
			if (!TextUtils.isEmpty(dto.provinceName)) {
				pro = dto.provinceName+"-";
			}
			String city = "";
			if (!TextUtils.isEmpty(dto.cityName)) {
				city = dto.cityName+"-";
			}
			String dis = "";
			if (!TextUtils.isEmpty(dto.disName)) {
				dis = dto.disName+"-";
			}
			mHolder.tvName.setText(pro+city+dis+dto.location);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
