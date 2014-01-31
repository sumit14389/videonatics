package com.android.videonatic.stream;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.videonatic.R;
import com.android.videonatic.activity.DashboardActivity;
import com.android.videonatic.activity.LoginActivity;
import com.android.videonatic.activity.VideoByLocationActivity;
import com.android.videonatic.adapter.DrawerAdapter;
import com.android.videonatic.adapter.MenuAdapter;
import com.android.videonatic.barchart.SalesStackedBarChart;
import com.android.videonatic.constant.Constant;
import com.android.videonatic.util.ClientLoginSession;
import com.android.videonatic.util.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.videonetics.component.VChannel;
import com.videonetics.controller.VSmartMobileInterface;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.model.action.PtzAction;
import com.videonetics.model.input.BarchartInput;
import com.videonetics.model.output.BarchartOutput;
import com.videonetics.model.output.VEvent;
import com.videonetics.values.VReturn;
import com.videonetics.values.VReturnMessage;






public class MjpegLiveView extends Activity implements OnInfoWindowClickListener {

	private MatrixMjpegView mv;
	private VChannel liveChannel = null;
	private String vmsIpString = null;
	private VaMjpegLiveReceiver liveMpegReciver = null;
	
	private DrawerLayout drawerLayout;
	String[] dashBoardArray;
	private ListView leftDrawerList;
	private ActionBarDrawerToggle drwawerToggle;
	private ActionBarDrawerToggle drawerToggle;
	public VSmartMobileInterface vSmartMobileInterface;

	
	private boolean isOnPause = false;
	private int IMAGE_WIDTH = 640 , IMAGE_HEIGHT = 480;
	private FrameLayout contentFrame;
	private PopupWindow pw;
	private DatePicker datePicker;
	private TimePicker timePicker;
	private GoogleMap siteMap;
	private FrameLayout parentFrame;
	private Handler imageHandler;
	private Handler locationHandler;
	private Util util;
	private FrameLayout frameLayout;
	ArrayList<BarchartOutput> chartDataList = new ArrayList<BarchartOutput>();
	
	private int orientation_default;

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		
		setContentView(R.layout.video_player);
		
		util = new Util(this);
		
		orientation_default = getResources().getConfiguration().orientation;
		
		
		initView();
		
		if(orientation_default == Configuration.ORIENTATION_PORTRAIT)
		{
			initializeMap();
		}
		
//		initializeMap();
		
		
		if(ClientLoginSession.SESSION_ID == 0 ){
			Intent intent = new Intent(MjpegLiveView.this, LoginActivity.class); 
			startActivity(intent);	
		}else{
//			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//		    
//			if(display.getWidth() > display.getHeight()){
//				 IMAGE_WIDTH = display.getWidth();
//			}else{
//				IMAGE_WIDTH = display.getHeight();
//			}
//		    
//		    IMAGE_HEIGHT = IMAGE_WIDTH * 3 / 4;
			Bundle bundel = getIntent().getExtras();  
			try{  
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

				liveChannel = (VChannel) bundel.get("channel");
				vmsIpString = (String) bundel.get("serverAddress");
				
//				Log.d("vmsIpString--", vmsIpString+"success");

				//      	  Log.v("channelList ", "cameraId Id=" +cameraId + "channelList size=" + channelList.size());
			}catch(Exception e){Log.i(" Error in B" , e.toString());}  


			if(vSmartMobileInterface == null ){
				vSmartMobileInterface =  new VSmartMobileInterfaceImpl(vmsIpString); 
			}

			vSmartMobileInterface = new VSmartMobileInterfaceImpl(vmsIpString);
			
			mv = new MatrixMjpegView(this);
			//setContentView(mv);
			contentFrame.addView(mv);
			mv.setDisplayMode(MatrixMjpegView.SIZE_FULLSCREEN);
			mv.setLiveChannel(liveChannel);
			mv.setServerIp(vmsIpString);
			liveMpegReciver = new VaMjpegLiveReceiver( vmsIpString , liveChannel , this , IMAGE_WIDTH , IMAGE_HEIGHT);
			mv.setSource(liveMpegReciver);
			mv.setMobileInterface(vSmartMobileInterface);
			
			//setTitle 
			getActionBar().setTitle(liveChannel.getName());
			
			invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			
		}
		

	}


	private void initializeMap() 
	{
	
		
		addMarker();
		zoomMap();
		
		
		siteMap.setOnInfoWindowClickListener(this);
		siteMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			View view;
			@Override
			public View getInfoWindow(Marker marker) {
				view = getLayoutInflater().inflate(R.layout.map_popup_windows,
						null);

				TextView txtView = (TextView)view.findViewById(R.id.loc_txt_view);
				txtView.setText(marker.getTitle());
				
				
				CameraPosition cameraPosition = new CameraPosition.Builder().target(
		                marker.getPosition()).zoom(12).build();
		 
				siteMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 20));
				//siteMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
				siteMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

				
				return view;
		
			}
			
			@Override
			public View getInfoContents(Marker arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	
		
	}

	private void zoomMap() 
	{
		VChannel vChannel = Constant.vChannels.get(0);
		LatLng zoomPlace = new LatLng(vChannel.getLatitude(), vChannel.getLongitude());
		siteMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomPlace, 20));
		siteMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
	}

	private void addMarker() 
	{
		 
		for (int i = 0; i < Constant.vChannels.size(); i++) {
			
			VChannel vChannel  = (VChannel)Constant.vChannels.get(i);
			 double lat = vChannel.getLatitude();
			 double lon = vChannel.getLongitude();
			 String title = vChannel.getName();
			 MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon)).title(title).snippet(String.valueOf(i));
			 marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin));
			 siteMap.addMarker(marker);
			
		}
		
		
		
	}

	private void initView() 
	{
		
		
		frameLayout = (FrameLayout)findViewById(R.id.map_frame);
		orientation_default = getResources().getConfiguration().orientation;
		
		if(orientation_default == Configuration.ORIENTATION_PORTRAIT)
		{
			frameLayout.setVisibility(View.VISIBLE);
		}
		else if(orientation_default == Configuration.ORIENTATION_LANDSCAPE)
		{
			frameLayout.setVisibility(View.GONE);
		}
		
		
		siteMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		siteMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		siteMap.getUiSettings().setCompassEnabled(true);
		siteMap.getUiSettings().setRotateGesturesEnabled(true);
		
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
	}

	

	
   

	@Override
	public  void onPause() {
		super.onPause();

		isOnPause = true;
		try {
			if(mv != null){
				mv.stopPlayback();
				if(liveMpegReciver.isConnected()){
					liveMpegReciver.closeConnection();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("Error", e.toString());
		}

	}
	@Override
	public void onResume() {
		super.onResume();
		if(isOnPause){
			isOnPause = false;
//			if(ClientLoginSession.SESSION_ID == 0 ){
//				Intent intent = new Intent(MjpegLiveView.this, LoginActivity.class); 
//				startActivity(intent);	
//			}else{
				mv = new MatrixMjpegView(this);
				contentFrame.addView(mv);
				mv.setDisplayMode(MatrixMjpegView.SIZE_BEST_FIT);
				mv.setLiveChannel(liveChannel);
				mv.setServerIp(vmsIpString);
				liveMpegReciver = new VaMjpegLiveReceiver( vmsIpString , liveChannel , this , IMAGE_WIDTH , IMAGE_HEIGHT);
				mv.setSource(liveMpegReciver);
				mv.setMobileInterface(vSmartMobileInterface);
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
				liveMpegReciver.closeConnection();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("Error", e.toString());
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Log.d(this.getClass().getName(), "back button pressed2222222222222222");
			try {
				if(mv != null){
					mv.stopPlayback();
					liveMpegReciver.closeConnection();
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("Error onKeyDown", e.toString());
			}


		}


		return super.onKeyDown(keyCode, event);
	}
	
	 /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = drawerLayout.isDrawerOpen(leftDrawerList);
    	if(liveChannel.getIsPTZ() == VChannel.PTZ){
    		 menu.findItem(R.id.ptz).setVisible(true);
    	  }else{
    		  menu.findItem(R.id.ptz).setVisible(false);
    	  }
       
        return super.onPrepareOptionsMenu(menu);
    }
    
	   @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	         // The action bar home/up action should open or close the drawer.
	         // ActionBarDrawerToggle will take care of this.
	        if (drawerToggle.onOptionsItemSelected(item)) {
	            return true;
	        }
	        
	        switch(item.getItemId())
	        {
	        case R.id.menu:
	        	callPopUp();
	        	break;
	        case R.id.ptz:
	        	callPtzDialog();
	        	break;
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
					Intent intent = new Intent(MjpegLiveView.this,LoginActivity.class);
					startActivity(intent);
				}
				
				
			};


			//progress.show();
			new Thread(new Runnable() {			
				@Override
				public void run() {
					if(vSmartMobileInterface == null ){
						vSmartMobileInterface =  new VSmartMobileInterfaceImpl(vmsIpString); 
					}
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
	        
	        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
	            frameLayout.setVisibility(View.GONE);
	              
	           }else{

	            frameLayout.setVisibility(View.VISIBLE);
	           }
	        
	        drawerToggle.onConfigurationChanged(newConfig);
	    }
	    
	    private void callPopUp() {

	    	System.out.println();
			pw = new PopupWindow(dropDownMenu(R.layout.pop_up_menu, new Vector()),200,300, true);
			pw.setBackgroundDrawable(new BitmapDrawable());
			pw.setOutsideTouchable(true);
			pw.showAtLocation(parentFrame, Gravity.RIGHT|Gravity.TOP, 15, 80);			
			pw.update();		
		}
	    
	    private View dropDownMenu(int layout, Vector menuItem)
		{
			View view = null;
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(MjpegLiveView.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(layout, null, false);	
			String[] values = new String[] { "Archive", "Event Statistics", "Log",
					  "Setting"};
			ArrayAdapter<String> adapter = new MenuAdapter(MjpegLiveView.this,R.layout.list_item_title_navigation,values);
			
			ListView listView = (ListView)view.findViewById(R.id.pop_up_menu_list);
			listView.setAdapter(adapter);
			
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position,
						long arg3) {
					
					switch(position)
					{
					case 0:
						//Toast.makeText(MjpegLiveView.this, "Please select date and time", Toast.LENGTH_LONG).show();
						callDateDialog();						
						break;
					case 1:				
						callEventStatistics();
						break;
					case 2:
						
						break;
					case 3:
						callSettingDialog();
						break;
					case 4:
						
						
						break;
					}
					pw.dismiss();
					
				}
			});
			
			return view;
		}


		protected void callEventStatistics() 
		{
			callBarcahtOutPut(liveChannel,null);			
		}
		
		protected void callBarcahtOutPut(final VChannel vChannel, final Calendar dateTime) 
		{
			locationHandler = new Handler()
			{
				public void handleMessage(Message msg)
				{
					VReturn vReturn = (VReturn)msg.obj;
					
					if (vReturn.getReturnValue() == VReturn.SUCCESS && vReturn.getResult() != null) { 
						chartDataList = (ArrayList<BarchartOutput>) vReturn.getResult();
						
						Intent intent = new SalesStackedBarChart(chartDataList).execute(MjpegLiveView.this);
						startActivity(intent);
						
						


					}else {
						util.showDialog("Problem in getting in statistics from server, please try again!");
					}
					
					
				}
			};



			new Thread(new Runnable() {			
//				private VSmartMobileInterfaceImpl vSmartMobileInterface;

				@Override
				public void run() {

					Calendar calendar = new GregorianCalendar();
					calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) , calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
					
					long currentStartTime = calendar.getTimeInMillis();
					long currentEndTime = currentStartTime + 24 * 60 * 60 * 1000;
					
					//System.out.println(new Date(currentStartTime) + " " + new Date(currentEndTime));
					
					BarchartInput barchartInput = new BarchartInput();
					barchartInput.setAcknowledge(VEvent.APPROVED);
					barchartInput.setApplicationId(BarchartInput.ALL_APPLICATION);
					barchartInput.setChannelId(vChannel.getId());
					barchartInput.setConsicutiveDays(1);
					barchartInput.setDontDeleteOnly(false);
					barchartInput.setEndTime(currentEndTime);
					barchartInput.setPriority(BarchartInput.ALL_PRIORITY);
					barchartInput.setPulseTime(60*60*1000);
					barchartInput.setStartTime(currentStartTime);
					barchartInput.setZoneId(BarchartInput.ALL_ZONE);

					ArrayList<BarchartInput> searchParameter = new ArrayList<BarchartInput>();
					searchParameter.add(barchartInput);

//					vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);	
					

					
					VReturn vReturn = vSmartMobileInterface.mGetBarChartOutputs(ClientLoginSession.SESSION_ID, searchParameter);
					locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
				}
			}).start();

		}


		protected void callPtzDialog() 
		{
			final Dialog dialog = new Dialog(MjpegLiveView.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.dialog_ptz);
			dialog.show();		
			
			 Window window = dialog.getWindow();
			 WindowManager.LayoutParams wlp = window.getAttributes();

			 wlp.y = -200;
			 
			 wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			 window.setAttributes(wlp);
			 
			 
				Button btnUpPan =  (Button)dialog.findViewById(R.id.button3);

				btnUpPan.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ptzActionPerform(PtzAction.CONTINIOUS_UP_TILT);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.d("Error", e.toString());
						}
						ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
					}
				});

				Button btnDownPan =  (Button)dialog.findViewById(R.id.button4);

				btnDownPan.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ptzActionPerform(PtzAction.CONTINIOUS_DOWN_TILT);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.d("Error", e.toString());
						}
						ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
					}
				});

				Button btnLeftPan =  (Button)dialog.findViewById(R.id.button1);

				btnLeftPan.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ptzActionPerform(PtzAction.CONTINIOUS_LEFT_PAN);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.d("Error", e.toString());
						}
						ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
					}
				});

				Button btnRightPan =  (Button)dialog.findViewById(R.id.button2);

				btnRightPan.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ptzActionPerform(PtzAction.CONTINIOUS_RIGHT_PAN);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.d("Error", e.toString());
						}
						ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
					}
				});

				Button btnZoomIn =  (Button)dialog.findViewById(R.id.button5);

				btnZoomIn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ptzActionPerform(PtzAction.CONTINIOUS_ZOOM_IN);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.d("Error", e.toString());
						}
						ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
					}
				});

				Button btnZoomOut =  (Button)dialog.findViewById(R.id.button6);

				btnZoomOut.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ptzActionPerform(PtzAction.CONTINIOUS_ZOOM_OUT);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.d("Error", e.toString());
						}
						ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
					}
				});
				
				
				Button btnClose =  (Button)dialog.findViewById(R.id.button7);

				btnClose.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				
		}


		public  void ptzActionPerform(final short ptzAction) {


			locationHandler = new Handler()
			{
				public void handleMessage(Message msg)
				{
					VReturn vReturn = (VReturn)msg.obj;
					if (vReturn.getReturnValue() != VReturn.SUCCESS) { 
						Toast.makeText(MjpegLiveView.this, VReturnMessage.returnMessage.get(vReturn.getReturnValue()), Toast.LENGTH_SHORT).show();
					} 
				}
			};



			new Thread(new Runnable() {			
				@Override
				public void run() {

					if(vSmartMobileInterface == null ){
						vSmartMobileInterface =  new VSmartMobileInterfaceImpl(vmsIpString); 
					}
					VReturn vReturn = vSmartMobileInterface.doPtzControl(ClientLoginSession.SESSION_ID, ptzAction, liveChannel);
					locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
				}
			}).start();

		}
		
		protected void callSettingDialog() 
		{
			final Dialog dialog = new Dialog(MjpegLiveView.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.dialog_setting);
			dialog.show();
			
			Button btn = (Button)dialog.findViewById(R.id.done_btn);
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					
				}
			});
			
			Button cancel = (Button)dialog.findViewById(R.id.cancel_btn);
			cancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					
				}
			});
			
			
		}


		protected void callDateDialog() 
		{
			
			final Dialog dialog = new Dialog(MjpegLiveView.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.dialog_date_picker);
			dialog.show();
			
			datePicker = (DatePicker)dialog.findViewById(R.id.datePicker1);
			timePicker = (TimePicker)dialog.findViewById(R.id.timePicker1);
			Button done = (Button)dialog.findViewById(R.id.done_btn);
			done.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					Calendar dateTime=Calendar.getInstance();
					dateTime.set(Calendar.YEAR,datePicker.getYear());
					dateTime.set(Calendar.MONTH, datePicker.getMonth());
					dateTime.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
					dateTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
					dateTime.set(Calendar.MINUTE,timePicker.getCurrentMinute());
					Bundle bundle = new Bundle();    
					bundle.putSerializable("channel", liveChannel); 
					bundle.putSerializable("serverAddress",ClientLoginSession.SERVER_IP);
					bundle.putSerializable("archiveDateTime", dateTime);
					
					Intent intent = new Intent(MjpegLiveView.this, MjpegArchiveAcitivity.class); 
					intent.putExtras(bundle);
					startActivity(intent);
					
					dialog.dismiss();
					
				}
			});
			
			
			
			
			
			
			
			
			
			
		}


		@Override
		public void onInfoWindowClick(Marker marker) {
			
			int index = Integer.parseInt( marker.getSnippet());
			liveChannel  = Constant.vChannels.get(index);
//			mv = new MatrixMjpegView(this);
//			//setContentView(mv);
//			contentFrame.removeAllViews();
//			contentFrame.addView(mv);
//			mv.setDisplayMode(MatrixMjpegView.SIZE_FULLSCREEN);
//			mv.setLiveChannel(liveChannel);
//			mv.setServerIp(vmsIpString);
//			liveMpegReciver = new VaMjpegLiveReceiver( vmsIpString , liveChannel , this , IMAGE_WIDTH , IMAGE_HEIGHT);
//			mv.setSource(liveMpegReciver);
			
			showImageDialog(liveChannel);
		}
		
		private void showImageDialog(final VChannel vChannel) 
		 {
			
			 final Dialog dialog = new Dialog(this);		
			 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			 dialog.setContentView(R.layout.dialaog_image);
			 dialog.show();
			 
			 Window window = dialog.getWindow();
			 WindowManager.LayoutParams wlp = window.getAttributes();

			 wlp.y = -200;
			 
			 wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			 window.setAttributes(wlp);
			 
			 final ImageView snapImage = (ImageView)dialog.findViewById(R.id.imageView1);
			 snapImage.setTag(vChannel);
			 
			 Button cross = (Button)dialog.findViewById(R.id.cross_btn);
			 cross.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					dialog.dismiss();
				}
			});
			 
			 TextView cameraName = (TextView)dialog.findViewById(R.id.camera_name_txt_view);
			 cameraName.setText(vChannel.getName());
			 
			 imageHandler = new Handler()
			 {
				 public void handleMessage(Message message)
				 {
					 VReturn vReturn = (VReturn)message.obj;
					 byte[] imageByte = (byte[]) vReturn.getResult();
					 if(imageByte!=null)
					 {
						 Bitmap bmp = getBitmap(imageByte);
						 snapImage.setImageBitmap(bmp);
					 }
					 
				 }
			 };
			 
			 
			 new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					if(vSmartMobileInterface == null ){
						vSmartMobileInterface =  new VSmartMobileInterfaceImpl(vmsIpString); 
					}
					
					 vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);			
					 VReturn vReturn = vSmartMobileInterface.mGetImageSnap(ClientLoginSession.SESSION_ID, vChannel.getId(),320, 240);
					
//					 vReturn.getResult();
					
					 imageHandler.sendMessage(Message.obtain(imageHandler, 0, vReturn));
				}
			}).start();
			 
			 snapImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					//Toast.makeText(MjpegLiveView.this,"Please wait...", Toast.LENGTH_LONG).show();
					mv = new MatrixMjpegView(MjpegLiveView.this);//					
					contentFrame.removeAllViews();
					contentFrame.addView(mv);
					mv.setDisplayMode(MatrixMjpegView.SIZE_FULLSCREEN);
					mv.setLiveChannel(liveChannel);
					mv.setServerIp(vmsIpString);
					liveMpegReciver = new VaMjpegLiveReceiver( vmsIpString , (VChannel)v.getTag() ,MjpegLiveView.this , IMAGE_WIDTH , IMAGE_HEIGHT);
					mv.setSource(liveMpegReciver);
					
					invalidateOptionsMenu();
					
					getActionBar().setTitle(liveChannel.getName());
					
				}
			});
			
		 }
		
		 private Bitmap getBitmap(byte[] b)
		 {
			return BitmapFactory.decodeByteArray(b , 0, b .length);
		 }


}
