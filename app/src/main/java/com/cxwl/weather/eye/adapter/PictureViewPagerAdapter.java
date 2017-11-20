package com.cxwl.weather.eye.adapter;

import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cxwl.weather.eye.view.MyPhotoView;

public class PictureViewPagerAdapter extends PagerAdapter{
	
	private ImageView[] mImageViews; 

	public PictureViewPagerAdapter(ImageView[] imageViews) {  
	    this.mImageViews = imageViews;  
	} 

	@Override
	public int getCount() {
		return mImageViews.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mImageViews[position]);
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		MyPhotoView photoView = new MyPhotoView(container.getContext());
		Drawable drawable = mImageViews[position].getDrawable();
		photoView.setImageDrawable(drawable);
		container.addView(photoView, 0);  
        return photoView;
	}

}
