package com.android.videonatic.activity;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.videonatic.R;
import com.android.videonatic.adapter.DrawerAdapter;
import com.android.videonatic.adapter.GridAdapter;
import com.android.videonatic.customview.CustomProgress;
import com.android.videonatic.customview.HorizontalListView;
import com.android.videonatic.screen.DisplayScreen;
import com.android.videonatic.stream.MjpegLiveViewFromGroupOrLocation;
import com.android.videonatic.util.ClientLoginSession;
import com.android.videonatic.util.RegisterActivities;
import com.android.videonatic.util.Util;
import com.videonetics.component.VChannel;
import com.videonetics.component.VGroup;
import com.videonetics.controller.VSmartMobileInterface;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.values.VReturn;

public class VideoByGroupActivity extends DisplayScreen implements OnNavigationListener{



	private DrawerLayout drawerLayout;
	String[] dashBoardArray;
	private ListView leftDrawerList;

	private ActionBarDrawerToggle drawerToggle;

	private Handler locationHandler;
	public VSmartMobileInterface vSmartMobileInterface;
	private LinearLayout parentLinearLayout;
	//private VSmartMobileInterfaceImpl vSmartMobileInterfaceImpl;
	
	private LayoutInflater inflater;
	private int yPos = 0;
	private int xPos = 0;
	private ScrollView scrollView;
	private Hashtable<VGroup, ArrayList<VChannel>> groupHashTable;
	private CustomProgress customProgress;
	//AdapterView<?> adpter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_thumb_layout);
		RegisterActivities.registerActivity(this);

		customProgress = new CustomProgress(VideoByGroupActivity.this, R.string.loading_msg);
		initView();
		inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		/*Vector<String> vector = new Vector<String>();
		vector.setSize(30);

		GridAdapter adapter = new GridAdapter(this, R.layout.custom_thumb,
				vector);
		thumbGrid.setAdapter(adapter);*/
		
		
		locationHandler = new Handler()
		{

			@SuppressWarnings("unchecked")
			public void handleMessage(Message message)
			{
				VReturn vReturn = (VReturn)message.obj;
				if(vReturn.getReturnValue() == VReturn.SUCCESS){
				 
					groupHashTable = (Hashtable<VGroup, ArrayList<VChannel>>) vReturn.getResult();
					
					if(groupHashTable != null &&groupHashTable.size() > 0)
						inflateView(groupHashTable);
					else if(groupHashTable != null &&groupHashTable.size() == 0)
						Util.showDialog(VideoByGroupActivity.this, R.string.no_location_found);
				}
			}
		};
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
			
			vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);				 
			VReturn vReturn = vSmartMobileInterface.mGetGroupTree(ClientLoginSession.SESSION_ID);		
			
			locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));	
			}
		}).start();

	}

	protected void inflateView(Hashtable<VGroup, ArrayList<VChannel>> groupHashTable) {
		// TODO Auto-generated method stub
		Enumeration<VGroup> locations = groupHashTable.keys();
        int nodeIndex = 0;
        while (locations.hasMoreElements()) 
        {
            VGroup group = locations.nextElement();
            LinearLayout mHorizontalScrollConatiner = addCategoryBar(group);
             ArrayList<VChannel> channelList = groupHashTable.get(group);
             addItem(mHorizontalScrollConatiner,channelList);
        }
	}

	private void addItem(LinearLayout mHorizontalScrollConatiner,final ArrayList<VChannel> channelList) {
		// TODO Auto-generated method stub
		
		LinearLayout mInflatedLayout = (LinearLayout) inflater.inflate(R.layout.horizontal_list, null);
		final HorizontalListView horizontalListView = (HorizontalListView) mInflatedLayout.findViewById(R.id.horizontal_list_view);
		
		GridAdapter gridAdapter = null;
		if(channelList != null)
			gridAdapter = new GridAdapter(VideoByGroupActivity.this, R.layout.custom_thumb, channelList,horizontalListView);
		
		if(gridAdapter != null) {
			horizontalListView.setAdapter(gridAdapter);
		}
				
		horizontalListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adpter, View v, int position,
					long id) {
				// TODO Auto-generated method stub
//				int childCount = adpter.getChildCount();
//				for(int ii = 0; ii < childCount; ii++) {
//					adpter.getChildAt(ii).setClickable(false);
//				}
				//this.adpter =adpter;
				customProgress.show();
				VChannel vChannel  = (VChannel) channelList.get(position);
				//new ItemClickAsyntask().execute(vChannel);
				Intent intent = new Intent(VideoByGroupActivity.this,MjpegLiveViewFromGroupOrLocation.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("channel", vChannel);
				Log.d("position  =====>", ""+position);
				bundle.putString("serverAddress", ClientLoginSession.SERVER_IP);
				intent.putExtras(bundle);
				customProgress.dismiss();
				startActivity(intent);
				overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
//				for(int ii = 0; ii < childCount; ii++) {
//					adpter.getChildAt(ii).setClickable(true);
//				}
			}
		});
		
		horizontalListView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				horizontalListView.setClickable(false);
				if(event.getAction() == MotionEvent.ACTION_MOVE){
					if(Math.abs(xPos - (int)event.getX()) > 3){
						scrollView.requestDisallowInterceptTouchEvent(true);
					}
					if(Math.abs(yPos - (int)event.getY()) > 50){											
						scrollView.requestDisallowInterceptTouchEvent(false);
					}
					xPos = (int)event.getX();
					yPos = (int)event.getY();
					return true;
				}
				return false;
			}
		});
		mHorizontalScrollConatiner.addView(mInflatedLayout);
	}

	private LinearLayout addCategoryBar(VGroup group) {
		// TODO Auto-generated method stub
		
		LinearLayout mInflatedLayout = (LinearLayout) inflater.inflate(R.layout.horizontal_list_container, null);
		
		LinearLayout mHorizontalScrollConatiner = (LinearLayout) mInflatedLayout.findViewById(R.id.horizontal_scroll_container_layout);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,130);
		mHorizontalScrollConatiner.setLayoutParams(params);
		
		TextView title = (TextView) mInflatedLayout.findViewById(R.id.category_title_txt_view);
		title.setText(group.getName());
		parentLinearLayout.addView(mInflatedLayout);
		
		return mHorizontalScrollConatiner;
	}

	private void initView() {
		parentLinearLayout = (LinearLayout) findViewById(R.id.parent_linear_layout);
		scrollView = (ScrollView) findViewById(R.id.scrollView1);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		leftDrawerList = (ListView) findViewById(R.id.left_drawer);
		dashBoardArray = getResources().getStringArray(R.array.dashboard_array);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		leftDrawerList.setAdapter(new DrawerAdapter(this,
				R.layout.list_item_title_navigation, dashBoardArray));
		leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		leftDrawerList.setItemChecked(3, true);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#513125")));

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
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
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}
	
	   @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.action_toggle_list_or_grid, menu);
	        return super.onCreateOptionsMenu(menu);
	    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.show_list:
			callVideoByLocationListActiviy();
			break;

		default:
			break;
		}
		// Handle action buttons
		return super.onOptionsItemSelected(item);

	}

	private void callVideoByLocationListActiviy() {
		// TODO Auto-generated method stub
		Intent ii = new Intent(VideoByGroupActivity.this,VideoByLocationListActivity.class);
		startActivity(ii);
	}

	public void selectItem(int position) {
		// update selected item and title, then close the drawer
		leftDrawerList.setItemChecked(position, true);
		drawerLayout.closeDrawer(leftDrawerList);

		switch (position) {

		case 0:
			callDasboardActivity();
			break;
		case 1:
			//callVideoByGroupActivity();
			break;
		case 2:
			callSiteMapActivity();
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
				Intent intent = new Intent(VideoByGroupActivity.this,
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
		Intent intent = new Intent(VideoByGroupActivity.this,SiteMapActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}

	private void callVideoByLocation() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(VideoByGroupActivity.this,VideoByLocationActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}

	private void callDasboardActivity() {
		Intent intent = new Intent(VideoByGroupActivity.this, DashboardActivity.class);
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

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

 class ItemClickAsyntask extends AsyncTask<VChannel , Void, VChannel>
 {
	 @Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		customProgress.show();
	}
	@Override
	protected VChannel doInBackground(VChannel... params) {
		VChannel[] vChannel;
		// TODO Auto-generated method stub
		vChannel  = params;
//		Intent intent = new Intent(VideoByGroupActivity.this,MjpegLiveViewFromGroupOrLocation.class);
//		Bundle bundle = new Bundle();
//		bundle.putSerializable("channel", vChannel[0]);
//	//	Log.d("position  =====>", ""+position);
//		bundle.putString("serverAddress", ClientLoginSession.SERVER_IP);
//		intent.putExtras(bundle);
//		customProgress.dismiss();
//		startActivity(intent);
//		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		return vChannel[0];
	}
	@Override
	protected void onPostExecute(VChannel result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Intent intent = new Intent(VideoByGroupActivity.this,MjpegLiveViewFromGroupOrLocation.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("channel", result);
	//	Log.d("position  =====>", ""+position);
		bundle.putString("serverAddress", ClientLoginSession.SERVER_IP);
		intent.putExtras(bundle);
		customProgress.dismiss();
		startActivity(intent);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}
	 
 }

}
