package com.cxwl.weather.eye.adapter;

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

import net.tsz.afinal.FinalBitmap;

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
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<EyeDto> mArrayList;
	private int width;

	private final class ViewHolder{
		ImageView imageView;
		TextView tvLocation;
	}
	
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
				FinalBitmap finalBitmap = FinalBitmap.create(mContext);
				finalBitmap.display(mHolder.imageView, dto.videoThumbUrl, null, 0);
			}else {
				OkHttpImg(dto.facilityUrlTes, mHolder.imageView);
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

	private void OkHttpImg(String facilityUrlTes, final ImageView imageView) {
		final String url = "https://api.bluepi.tianqi.cn/Outdata/other/getNetEyeImage/id/5107_"+facilityUrlTes;
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

									String img = obj.getString("img");
									if (!TextUtils.isEmpty(img)) {
										Picasso.get().load(img).error(R.drawable.eye_bg_thumb).into(imageView);
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
