package com.android.videonatic.activity;

import java.util.ArrayList;

import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.videonatic.R;
import com.android.videonatic.adapter.DrawerAdapter;
import com.android.videonatic.adapter.ListAdapter;
import com.android.videonatic.constant.Constant;
import com.android.videonatic.customview.CustomProgress;
import com.android.videonatic.screen.DisplayScreen;
import com.android.videonatic.stream.MjpegLiveViewFromGroupOrLocation;
import com.android.videonatic.util.ClientLoginSession;
import com.android.videonatic.util.RegisterActivities;
import com.android.videonatic.util.Util;
import com.videonetics.component.VChannel;
import com.videonetics.controller.VSmartMobileInterface;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.values.VReturn;

public class VideoByLocationListActivity extends DisplayScreen implements OnNavigationListener{
	
	private DrawerLayout drawerLayout;
	String[] dashBoardArray;
	private ListView leftDrawerList;
	
	private ActionBarDrawerToggle drawerToggle;
	private ListView videoThumbList;
	private Handler locationHandler;
	private Util util;
	public VSmartMobileInterface vSmartMobileInterface;
	private CustomProgress customProgress;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_thumb_list);
		
		RegisterActivities.registerActivity(this);
		util = new Util(this);
		customProgress = new CustomProgress(VideoByLocationListActivity.this, R.string.loading_msg);
		initView();
		
//		Vector<String> vector = new Vector<String>();
//		vector.setSize(30);
//		
//		ListAdapter adapter = new ListAdapter(this,R.layout.custom_thumb_list, vector);
//		videoThumbList.setAdapter(adapter);
		locationHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				VReturn vReturn = (VReturn)msg.obj;
				  if(vReturn.getReturnValue() == VReturn.SUCCESS){
				   Constant.vChannelsListView = (ArrayList<VChannel>) vReturn.getResult();
				   ListAdapter adapter = new ListAdapter(VideoByLocationListActivity.this,R.layout.custom_thumb_list, Constant.vChannelsListView,videoThumbList);
				   videoThumbList.setAdapter(adapter);
				   
				   videoThumbList.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> adapterView, View v,
								int position, long id) {
							// TODO Auto-generated method stub
							customProgress.show();
							VChannel vChannel  = (VChannel) Constant.vChannelsListView.get(position);
							Intent intent = new Intent(VideoByLocationListActivity.this,MjpegLiveViewFromGroupOrLocation.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("channel", vChannel);
							bundle.putString("serverAddress", ClientLoginSession.SERVER_IP);
							intent.putExtras(bundle);
							customProgress.dismiss();
							startActivity(intent);
						}
					});
				   /*System.err.println(vChannels.size());
				   
				   for (VChannel vChannel : vChannels) {
				    System.err.println("vChannel getLatitude" + vChannel.getLatitude());
				    System.err.println("vChannel getLongitude" + vChannel.getLongitude());
				   }*/
				  }else{
				   System.err.println("Problem in mGetGeoChannels");
				   util.showDialog("Unable to fetch location! please try again!");
				   //call logout
				   
				  }
			}
		};
		
		
		
		new Thread(new Runnable() {			
			@Override
			public void run() {
				
				vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);				 
				VReturn vReturn = vSmartMobileInterface.mGetAllChannels(ClientLoginSession.SESSION_ID);
				
				locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
			}
		}).start();
		
	}

	private void initView() 
	{
		
		videoThumbList = (ListView)findViewById(R.id.video_thumb_list);
		
		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		leftDrawerList = (ListView)findViewById(R.id.left_drawer);
		dashBoardArray = getResources().getStringArray(R.array.dashboard_array);		
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		leftDrawerList.setAdapter(new DrawerAdapter(this, R.layout.list_item_title_navigation, dashBoardArray));
		leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		leftDrawerList.setItemChecked(3, true);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
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
			   callSiteMapActivity();
			   break;
		   case 3:
			   //callVideoByLocation();
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

	   protected void callLogout(final long loginSession) {
			// final CustomProgress progress = new CustomProgress(this, "");
			locationHandler = new Handler() {
				public void handleMessage(Message msg) {
					VReturn vReturn = (VReturn) msg.obj;
					if (vReturn.getReturnValue() == VReturn.SUCCESS) {
						LoginActivity.exitHeartBeat = true;

					} else {
						System.err.println("Problem in log out");
					}

					// progress.dismiss();
					Intent intent = new Intent(VideoByLocationListActivity.this,
							LoginActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
				}

			};

			// progress.show();
			new Thread(new Runnable() {
				@Override
				public void run() {
					vSmartMobileInterface = new VSmartMobileInterfaceImpl(
							ClientLoginSession.SERVER_IP);
					VReturn vReturn = vSmartMobileInterface.mLogOut(loginSession);
					locationHandler.sendMessage(Message.obtain(locationHandler, 0,
							vReturn));
				}
			}).start();

		}

		private void callSiteMapActivity() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(VideoByLocationListActivity.this,SiteMapActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		}

		private void callVideoByGroupActivity() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(VideoByLocationListActivity.this,VideoByGroupActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		}

		private void callDasboardActivity() {
			Intent intent = new Intent(VideoByLocationListActivity.this, DashboardActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);

		}

		private void callUploadActivity() {

		}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
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

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

}
