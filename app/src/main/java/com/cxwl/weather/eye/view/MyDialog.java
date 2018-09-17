package com.cxwl.weather.eye.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

import com.cxwl.weather.eye.R;


public class MyDialog extends Dialog {
	
	public MyDialog(Context context) {
		super(context);
	}
	
	public MyDialog(Context context, String msg) {
		super(context);
	}
	
	public void setStyle(int featureId) {
		requestWindowFeature(featureId);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(R.color.transparent);
		setContentView(R.layout.layout_loading);
		
		ImageView ivLoading = findViewById(R.id.ivLoading);
		AnimationDrawable animationDrawable = (AnimationDrawable) ivLoading.getDrawable();
		animationDrawable.start();
	}
	
}
