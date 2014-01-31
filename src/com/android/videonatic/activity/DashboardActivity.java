package com.android.videonatic.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;

import com.android.videonatic.R;
import com.android.videonatic.screen.DisplayScreen;
import com.android.videonatic.util.ClientLoginSession;
import com.android.videonatic.util.RegisterActivities;
import com.videonetics.controller.VSmartMobileInterface;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.values.VReturn;

public class DashboardActivity extends DisplayScreen implements AnimationListener{
	
	
	private FrameLayout videoByLocation;
	private FrameLayout videoByGroup;
	private FrameLayout siteMap;
	private FrameLayout upload;
	private final int DEGREE = 360;
	private final int NO_OF_FIELD = 4;
	private final int FST_DURATION = 500;
	private final int SEC_DURATION = 1000;
	private final int THRD_DURATION = 1500;
	private FrameLayout wheelFrame;
	private RotateAnimation anim;
	private Handler handler;
	private final int VIDEO_BY_LOCATION = 0;
	private final int SITE_MAP = 1;
	private final int VIDEO_BY_GROUP = 2;
	private final int UPLOAD = 3;
	private int currentIndex = -1;
	private Handler locationHandler;
	public VSmartMobileInterface vSmartMobileInterface;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		RegisterActivities.registerActivity(this);
		initView();
		
		
		 handler = new Handler()
	        {
	        	@Override
	        	public void handleMessage(Message msg)
	        	{
	        		startActivity();
	        		
	        	}
	        };
	}

	protected void startActivity() 
	{
		switch(currentIndex)
		{
		case 0:
			Intent intent = new Intent(this, VideoByLocationActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			break;
		case 1:
			Intent intent2 = new Intent(this, SiteMapActivity.class);
			startActivity(intent2);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			break;
		case 2:
			Intent intent3 = new Intent(this, VideoByGroupActivity.class);
			startActivity(intent3);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
			break;
		case 3:
			break;
		default:
			break;
		}
	}

	private void initView() 
	{
		videoByLocation = (FrameLayout)findViewById(R.id.dummy_video_by_location);		
		videoByLocation.setOnClickListener(this);
		videoByGroup = (FrameLayout)findViewById(R.id.dummy_video_by_group);
		videoByGroup.setOnClickListener(this);
		siteMap = (FrameLayout)findViewById(R.id.dummy_site_map);
		siteMap.setOnClickListener(this);
		upload = (FrameLayout)findViewById(R.id.dummy_upload);
		upload.setOnClickListener(this);
		wheelFrame = (FrameLayout)findViewById(R.id.wheel_frame);
	}

	@Override
	public void onClick(View v) 
	{
		float reqDegree = DEGREE/ NO_OF_FIELD ;	
		switch(v.getId())
		{
		case R.id.dummy_video_by_location:
			currentIndex = 0;
			setAnimation(reqDegree*3+10, THRD_DURATION);
			break;
		case R.id.dummy_site_map:
			currentIndex = 1;
			setAnimation(reqDegree*3+10, THRD_DURATION);
			break;
		case R.id.dummy_video_by_group:
			currentIndex = 2;
			setAnimation(reqDegree*3+10, THRD_DURATION);
			break;
		case R.id.dummy_upload:
			currentIndex = 3;
			break;
		}
	}
	
	private void setAnimation(float degree, long duration)
	{
		anim = new RotateAnimation(0,degree,wheelFrame.getWidth()>>1, (wheelFrame.getHeight())>>1 );
		anim.setDuration(duration);			
		//wheelFrame.setAnimation(anim);
		wheelFrame.startAnimation(anim);
		anim.setAnimationListener(this);
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		handler.sendMessage(handler.obtainMessage(0));
		
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	
		if(keyCode == event.KEYCODE_BACK)
		{
			new AlertDialog.Builder(DashboardActivity.this)
			.setMessage("Do you want to exit?")			
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					callLogout(ClientLoginSession.SESSION_ID);
					RegisterActivities.removeAllActivities();
					
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).show();
		}
		
		return super.onKeyDown(keyCode, event);
}
	
	protected void callLogout(final long loginSession) 
	{
		locationHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				VReturn vReturn = (VReturn)msg.obj;
				if(vReturn.getReturnValue() == VReturn.SUCCESS){
					LoginActivity.exitHeartBeat = true;
					
				}else{
					System.err.println("Problem in log out");
				}
				RegisterActivities.removeAllActivities();
			}
			
			
		};



		new Thread(new Runnable() {			
			@Override
			public void run() {
				vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);
				VReturn vReturn = vSmartMobileInterface.mLogOut(loginSession);
				locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
			}
		}).start();

	}

}
