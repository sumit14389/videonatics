package com.android.videonatic;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;

import com.android.videonatic.activity.LoginActivity;
import com.android.videonatic.util.RegisterActivities;

public class SplashActivity extends Activity {

	Timer timer;
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		RegisterActivities.registerActivity(this);
		
		timer = new Timer();
		startTimer();
		
		
	
		
		handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
				//Intent intent = new Intent(SplashActivity.this, VideoByLocationListActivity.class);
				//Intent intent = new Intent(SplashActivity.this, VideoByLocationActivity.class);
				startActivity(intent);				
				overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			}
		};
		
		
	}

	private void startTimer() 
	{
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				handler.sendEmptyMessage(0);
				timer.cancel();
			}
		}, 3000, 3000);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

}
