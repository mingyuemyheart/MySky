package com.cxwl.weather.eye.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.dto.EyeDto;
import com.cxwl.weather.eye.view.ShawnRotateLineView;
import com.squareup.picasso.Picasso;

/**
 * 主界面-列表
 */
public class ShawnCalendarFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.shawn_fragment_calendar, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}

	private void initWidget(View view) {
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		ImageView imageView = view.findViewById(R.id.imageView);
		ShawnRotateLineView rotateLineView = view.findViewById(R.id.rotateLineView);
		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(width, width*9/16);
		imageView.setLayoutParams(params1);
		ViewGroup.LayoutParams params2 = rotateLineView.getLayoutParams();
		params2.width = params1.height/2;
		params2.height = params1.height/2;
		rotateLineView.setLayoutParams(params2);

		EyeDto data = getArguments().getParcelable("data");
		if (data != null) {
			if (!TextUtils.isEmpty(data.pictureUrl)) {
				Picasso.get().load(data.pictureUrl).error(R.drawable.icon_seat_bitmap).into(imageView);
			}else {
				imageView.setImageResource(R.drawable.icon_seat_bitmap);
			}

			if (!TextUtils.isEmpty(data.time)) {
				rotateLineView.setData(data.time);
			}

		}
	}
	
}
