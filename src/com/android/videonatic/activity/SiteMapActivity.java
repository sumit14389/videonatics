package com.android.videonatic.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.videonatic.R;
import com.android.videonatic.adapter.DrawerAdapter;
import com.android.videonatic.constant.Constant;
import com.android.videonatic.stream.MjpegLiveView;
import com.android.videonatic.util.ClientLoginSession;
import com.android.videonatic.util.RegisterActivities;
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
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.values.VReturn;

public class SiteMapActivity extends Activity implements OnInfoWindowClickListener{
	
	
	private GoogleMap siteMap;
	private DrawerLayout drawerLayout;
	String[] dashBoardArray;
	private ListView leftDrawerList;
	private ActionBarDrawerToggle drwawerToggle;
	private ActionBarDrawerToggle drawerToggle;
	private VSmartMobileInterfaceImpl vSmartMobileInterface;
	private Handler imageHandler;
	private Handler locationHandler;
	protected ImageView snapImage;
	private Util util;
	private Marker marker;
	private Bitmap bmp;


	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sitemap);
		RegisterActivities.registerActivity(this);
		util = new Util(this);
		initView();
		initializeMap();
	}

	private void initializeMap() 
	{	
		
		addMarker();
		zoomMap();
		
		
		siteMap.setOnInfoWindowClickListener(this);
		
		
		
		siteMap.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			
			@Override
			public View getInfoWindow(final Marker marker) {
				final View view = getLayoutInflater().inflate(R.layout.map_popup_windows,
						null);

				
				final TextView txtView = (TextView)view.findViewById(R.id.loc_txt_view);
				txtView.setText(marker.getTitle());
				txtView.setTag(marker.getTitle());
				
				final ImageView snapImage = (ImageView)view.findViewById(R.id.snap_image);
				snapImage.setTag(marker.getTitle());
				
				CameraPosition cameraPosition = new CameraPosition.Builder().target(
		                marker.getPosition()).zoom(12).build();
		 
				siteMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 20));
				//siteMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
				siteMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

				int index = Integer.parseInt( marker.getSnippet());
				final VChannel vChannel  = Constant.vChannels.get(index);
				//showImageDialog(vChannel);
				
				
				 
				/* imageHandler = new Handler()
				 {
					 public void handleMessage(Message message)
					 {
						 VReturn vReturn = (VReturn)message.obj;
						 byte[] imageByte = (byte[]) vReturn.getResult();
						 if(imageByte!=null)
						 {
							 bmp = getBitmap(imageByte);	
							 
							 getInfoContents(marker);
						 }
						 
					 }
				 };
				 
				 
				 new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						 vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);			
						 VReturn vReturn = vSmartMobileInterface.mGetImageSnap(ClientLoginSession.SESSION_ID, vChannel.getId(),320, 240);
						
						 vReturn.getResult();				
						
						 imageHandler.sendMessage(Message.obtain(imageHandler, 0, vReturn));
					}
				}).start();*/
				 
				
				
				return view;
		
			}
			
			@SuppressWarnings("deprecation")
			@Override
			public View getInfoContents(Marker marker) {
				
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

	private void initView() {
	
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
		
		leftDrawerList.setItemChecked(2, true);
		
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

	
	@Override
	public void onInfoWindowClick(Marker marker) {
				
		int index = Integer.parseInt( marker.getSnippet());
		VChannel vChannel  = Constant.vChannels.get(index);
	/*	Intent intent = new Intent(this,MjpegLiveView.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("channel", vChannel);
		bundle.putString("serverAddress", ClientLoginSession.SERVER_IP);
		intent.putExtras(bundle);
		startActivity(intent);*/
		
		showImageDialog(vChannel);
	}
	
	 private void showImageDialog(final VChannel vChannel) 
	 {
		
		 final Dialog dialog = new Dialog(this);		
		 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		 dialog.setContentView(R.layout.dialaog_image);
		 dialog.show();
		 
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
				
				 vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);			
				 VReturn vReturn = vSmartMobileInterface.mGetImageSnap(ClientLoginSession.SESSION_ID, vChannel.getId(),160, 120);
				
				 vReturn.getResult();
				
				 imageHandler.sendMessage(Message.obtain(imageHandler, 0, vReturn));
			}
		}).start();
		 
		 snapImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				String fromActivity = "fromSiteMap";
				VChannel vChannel  = (VChannel) v.getTag();
				Intent intent = new Intent(SiteMapActivity.this,MjpegLiveView.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("channel", vChannel);
				bundle.putString("serverAddress", ClientLoginSession.SERVER_IP);
				bundle.putString("fromWhichActivity", fromActivity);
				intent.putExtras(bundle);
				startActivity(intent);
				
			}
		});
		 
		
		
	 }
	 
	 private Bitmap getBitmap(byte[] b)
	 {
		return BitmapFactory.decodeByteArray(b , 0, b .length);
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
					Intent intent = new Intent(SiteMapActivity.this,
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

		private void callVideoByGroupActivity() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(SiteMapActivity.this,VideoByGroupActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		}

		private void callVideoByLocation() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(SiteMapActivity.this,VideoByLocationActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		}

		private void callDasboardActivity() {
			Intent intent = new Intent(SiteMapActivity.this, DashboardActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);

		}

		private void callUploadActivity() {

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
