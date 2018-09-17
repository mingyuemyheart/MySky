package com.cxwl.weather.eye.adapter;

import android.app.Activity;
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
import com.cxwl.weather.eye.utils.OkHttpUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 视频列表
 */
public class VideoListAdapter extends BaseAdapter{
	
	private Activity activity;
	private LayoutInflater mInflater;
	private List<EyeDto> mArrayList;
	private int width;

	private final class ViewHolder{
		ImageView imageView;
		TextView tvLocation;
	}
	
	public VideoListAdapter(Activity activity, List<EyeDto> mArrayList) {
		this.activity = activity;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_videolist, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.tvLocation = convertView.findViewById(R.id.tvLocation);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			EyeDto dto = mArrayList.get(position);
			if (!TextUtils.isEmpty(dto.location)) {
				mHolder.tvLocation.setText(dto.location);
			}

			if (!TextUtils.isEmpty(dto.videoThumbUrl)) {
				Picasso.get().load(dto.videoThumbUrl).error(R.drawable.eye_bg_thumb).into(mHolder.imageView);
			}else {
				if (TextUtils.equals(dto.videoThumbUrl, "")) {
					mHolder.imageView.setImageResource(R.drawable.eye_bg_thumb);
				}else {
					OkHttpImg(dto.facilityUrlTes, mHolder.imageView, dto);
				}
			}
			LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = width/3;
			params.height = width/3*3/4;
			mHolder.imageView.setLayoutParams(params);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

	private void OkHttpImg(String facilityUrlTes, final ImageView imageView, final EyeDto dto) {
		if (TextUtils.isEmpty(facilityUrlTes)) {
			return;
		}
		final String url = "https://api.bluepi.tianqi.cn/Outdata/other/getNetEyeImage/id/"+facilityUrlTes;
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("img")) {
									final String img = obj.getString("img");
									if (!TextUtils.isEmpty(img)) {
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Picasso.get().load(img).error(R.drawable.eye_bg_thumb).into(imageView);
												dto.videoThumbUrl = img;
//												notifyDataSetChanged();
											}
										});
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

}
