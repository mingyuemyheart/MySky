package com.cxwl.weather.eye;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class WelcomeActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		startIntentMain();
	}
	
	/**
	 * 启动线程进入主界面
	 */
	private void startIntentMain() {
		Handler handler = new Handler();
		handler.postDelayed(new MainRunnable(), 1500);
	}
	
	private class MainRunnable implements Runnable{
		@Override
		public void run() {
			startActivity(new Intent(WelcomeActivity.this, WelcomeVideoActivity.class));
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}

}
