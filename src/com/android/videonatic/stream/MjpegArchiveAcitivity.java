package com.android.videonatic.stream;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TimePicker;

import com.android.videonatic.R;
import com.android.videonatic.activity.DashboardActivity;
import com.android.videonatic.activity.LoginActivity;
import com.android.videonatic.activity.VideoByLocationActivity;
import com.android.videonatic.adapter.DrawerAdapter;
import com.android.videonatic.util.ClientLoginSession;
import com.google.android.gms.maps.GoogleMap;
import com.videonetics.component.VChannel;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.values.VReturn;






public class MjpegArchiveAcitivity extends Activity {

	private ArchiveMjpegView mv;
	private VChannel archiveChannel = null;
	private String vmsIpString = null;
	private VaMjpegArchiveReceiver archiveMpegReciver = null;

	private boolean isOnPause = false;
	Calendar dateTime = null;
	private int IMAGE_WIDTH = 640 , IMAGE_HEIGHT = 480;
	private GoogleMap siteMap;
	private DrawerLayout drawerLayout;
	private ListView leftDrawerList;
	private String[] dashBoardArray;
	private ActionBarDrawerToggle drawerToggle;
	private FrameLayout contentFrame;
	private FrameLayout parentFrame;
	private DatePicker datePicker;
	private TimePicker timePicker;
	private Handler locationHandler;
	private VSmartMobileInterfaceImpl vSmartMobileInterface;

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		
		setContentView(R.layout.archieve_video_player);
	
		
		if(ClientLoginSession.SESSION_ID == 0 ){
			Intent intent = new Intent(MjpegArchiveAcitivity.this, LoginActivity.class); 
			startActivity(intent);	
		}else{

//			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//
//			if(display.getWidth() > display.getHeight()){
//				IMAGE_WIDTH = display.getWidth();
//			}else{
//				IMAGE_WIDTH = display.getHeight();
//			}
//
//			IMAGE_HEIGHT = IMAGE_WIDTH * 3 / 4;

			Bundle bundel = getIntent().getExtras();  
			try{  
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				//				vmsIpString = prefs.getString("ivmslan", null);


				archiveChannel = (VChannel) bundel.get("channel");

				dateTime = (Calendar) bundel.get("archiveDateTime");

				vmsIpString = (String) bundel.get("serverAddress");


				//      	  Log.v("channelList ", "cameraId Id=" +cameraId + "channelList size=" + channelList.size());
			}catch(Exception e){Log.i(" Error in B" , e.toString());}  


		
			initView();

			vSmartMobileInterface = new VSmartMobileInterfaceImpl(vmsIpString);
			mv = new ArchiveMjpegView(this);
			//setContentView(mv);       
			contentFrame.addView(mv);
			mv.setDisplayMode(ArchiveMjpegView.SIZE_BEST_FIT);
			mv.setLiveChannel(archiveChannel);
			mv.setServerIp(vmsIpString);
			archiveMpegReciver = new VaMjpegArchiveReceiver( vmsIpString , archiveChannel ,dateTime.getTimeInMillis(), this , IMAGE_WIDTH , IMAGE_HEIGHT);
			mv.setSource(archiveMpegReciver);

		}


	}


	private void initView() 
	{
		
		
		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		leftDrawerList = (ListView)findViewById(R.id.left_drawer);
		dashBoardArray = getResources().getStringArray(R.array.dashboard_array);		
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		leftDrawerList.setAdapter(new DrawerAdapter(this, R.layout.list_item_title_navigation, dashBoardArray));
		leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());		
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#513125")));
		
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 
				R.string.drawer_open,R.string.drawer_close)
		{
			@Override
			public void onDrawerClosed(View drawerView) {
				// TODO Auto-generated method stub
				getActionBar().setTitle("");
				super.onDrawerClosed(drawerView);
			}
			
			@Override
			public void onDrawerOpened(View drawerView) {
				// TODO Auto-generated method stub
				getActionBar().setTitle("");
				super.onDrawerOpened(drawerView);
			}
		};
		
		drawerLayout.setDrawerListener(drawerToggle);
		
		contentFrame = (FrameLayout)findViewById(R.id.player_frame);
		parentFrame = (FrameLayout)findViewById(R.id.map_frame);
		
		datePicker = (DatePicker)findViewById(R.id.datePicker1);
		timePicker = (TimePicker)findViewById(R.id.timePicker1);
		
		
		
		datePicker.init(dateTime.get(Calendar.YEAR), dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH), null);
		timePicker.setCurrentHour(dateTime.get(Calendar.HOUR));
		timePicker.setCurrentMinute(dateTime.get(Calendar.MINUTE));
		
		Button done = (Button)findViewById(R.id.done_btn);
		
		done.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dateTime=Calendar.getInstance();
				dateTime.set(Calendar.YEAR,datePicker.getYear());
				dateTime.set(Calendar.MONTH, datePicker.getMonth());
				dateTime.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
				dateTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
				dateTime.set(Calendar.MINUTE,timePicker.getCurrentMinute());
				
			
				
				mv = new ArchiveMjpegView(MjpegArchiveAcitivity.this);
				//setContentView(mv);       
				contentFrame.removeAllViews();
				contentFrame.addView(mv);
				mv.setDisplayMode(ArchiveMjpegView.SIZE_BEST_FIT);
				mv.setLiveChannel(archiveChannel);
				mv.setServerIp(vmsIpString);
				archiveMpegReciver = new VaMjpegArchiveReceiver( vmsIpString , archiveChannel ,dateTime.getTimeInMillis(),MjpegArchiveAcitivity. this , IMAGE_WIDTH , IMAGE_HEIGHT);
				mv.setSource(archiveMpegReciver);
				
			}
		});
	}


	@Override
	public  void onPause() {
		super.onPause();

		isOnPause = true;
		try {
			if(mv != null){
				mv.stopPlayback();
				archiveMpegReciver.closeConnection();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("Error", e.getMessage());
		}

	}
	@Override
	public void onResume() {
		super.onResume();
		if(isOnPause){
			isOnPause = false;
//			if(ClientLoginSession.SESSION_ID == 0 ){
//				Intent intent = new Intent(MjpegArchiveAcitivity.this, LoginActivity.class); 
//				startActivity(intent);	
//			}else{
				mv = new ArchiveMjpegView(this);				
				contentFrame.addView(mv);
				mv.setDisplayMode(ArchiveMjpegView.SIZE_BEST_FIT);
				mv.setLiveChannel(archiveChannel);
				mv.setServerIp(vmsIpString);
				archiveMpegReciver = new VaMjpegArchiveReceiver( vmsIpString , archiveChannel , dateTime.getTimeInMillis() , this , IMAGE_WIDTH , IMAGE_HEIGHT);
				mv.setSource(archiveMpegReciver);

//			}



		}


	}

	@Override
	public void onUserLeaveHint() {
		super.onUserLeaveHint();
		isOnPause = true;
		try {
			if(mv != null){
				mv.stopPlayback();
				archiveMpegReciver.closeConnection();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("Error", e.getMessage());
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Log.d(this.getClass().getName(), "back button pressed2222222222222222");
			try {
				if(mv != null){
					archiveMpegReciver.closeConnection();
					mv.stopPlayback();

				}
				return super.onKeyDown(keyCode, event);
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("Error", e.getMessage());
				return false;
			}


		}else{
			return super.onKeyDown(keyCode, event);
		}



	}
	
	 /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        return super.onOptionsItemSelected(item);
     
     
    }
    
    public void selectItem(int position) 
	   {
		   // update selected item and title, then close the drawer
		   leftDrawerList.setItemChecked(position, true);	       
		   drawerLayout.closeDrawer(leftDrawerList);
		   
		   switch(position)
		   {
		   case 0:
			   callDasboardActivity();
			   break;
		   case 1:
			   callVideoByGroupActivity();
			   break;
		   case 2:
			   //callSiteMapActivity();
			   break;
		   case 3:
			   callVideoByLocation();
			   break;
		   case 4:
			   callUploadActivity();
			   break;
		   case 5:
			   callLogout(ClientLoginSession.SESSION_ID);
			   break;
		   default:
			   break;
			   
		   
		   }
		
	   }
    
    protected void callLogout(final long loginSession) 
	{
		//final CustomProgress progress = new CustomProgress(this, "");
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
				
				//progress.dismiss();
				Intent intent = new Intent(MjpegArchiveAcitivity.this,LoginActivity.class);
				startActivity(intent);
			}
			
			
		};


		//progress.show();
		new Thread(new Runnable() {			
			@Override
			public void run() {
				vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);
				VReturn vReturn = vSmartMobileInterface.mLogOut(loginSession);
				locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
			}
		}).start();

	}
	private void callUploadActivity() {
		// TODO Auto-generated method stub
		
	}


	private void callVideoByLocation() {
		Intent intent = new Intent(this,VideoByLocationActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);		
		
	}


	private void callVideoByGroupActivity() {
		// TODO Auto-generated method stub
		
	}


	private void callDasboardActivity() {
		Intent intent = new Intent(this,DashboardActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);		
		
	}


	@Override
	    protected void onPostCreate(Bundle savedInstanceState) {
	        super.onPostCreate(savedInstanceState);
	        // Sync the toggle state after onRestoreInstanceState has occurred.
	        drawerToggle.syncState();
	    }

	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	        super.onConfigurationChanged(newConfig);
	        // Pass any configuration change to the drawer toggls
	        drawerToggle.onConfigurationChanged(newConfig);
	    }

}
