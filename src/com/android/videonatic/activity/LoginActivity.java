package com.android.videonatic.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.android.videonatic.R;
import com.android.videonatic.constant.Constant;
import com.android.videonatic.customview.CustomProgress;
import com.android.videonatic.util.ClientLoginSession;
import com.android.videonatic.util.RegisterActivities;
import com.android.videonatic.util.Util;
import com.videonetics.component.VChannel;
import com.videonetics.controller.VSmartMobileInterface;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.model.generic.LoginSession;
import com.videonetics.model.input.VUserMessage;
import com.videonetics.values.VReturn;
import com.videonetics.values.VReturnMessage;

public class LoginActivity extends Activity implements OnClickListener{
	
	
	private Button loginBtn;
	private Button setting;
	private EditText userName;
	private EditText password;
	private Util util;
	private Handler loginHandler;
	private final int LOCAL = 0;
	private final int WAN = 1;
	private Button wanBtn;
	private Button lanBtn;
	private Button okBtn;
	public VSmartMobileInterface vSmartMobileInterface;
	private Handler locationHandler; 
	public static boolean exitHeartBeat = false;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		RegisterActivities.registerActivity(this);
		util = new Util(this);
		
		initView();
	}

	private void initView() 
	{
		loginBtn = (Button)findViewById(R.id.login_btn);	
		loginBtn.setOnClickListener(this);
		
		setting = (Button)findViewById(R.id.setting_btn);
		setting.setOnClickListener(this);
		
		userName = (EditText)findViewById(R.id.user_name_edt_txt);
		password = (EditText)findViewById(R.id.password_edt_txt);
	}

	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		case R.id.login_btn:
			checkValidation();
			//callDashboardActivity();			
			break;
		case R.id.setting_btn:
			startSettingActivity();			
			break;
		}
		
	}

	private void checkValidation() 
	{
		if(userName.length() != 0)
		{
			if(password.length() !=0)
			{
				if(util.isConnectionPossible())
				{
					SharedPreferences settings=getSharedPreferences("mypers",0);
					final String localIp = settings.getString("local_ip", "");
					callLogin(userName.getText().toString(), password.getText().toString(),localIp,LOCAL);
				}
				else
				{
					util.showDialog("No connection available!");
				}
			}
			else
			{
				util.showDialog("Enter password!");
				password.requestFocus();
			}
		}
		else
		{
			util.showDialog("Enter user name!");
			userName.requestFocus();
		}
			
	}

	private void callLogin(final String userName, final String password, final String iPAddress, final int method) 
	{
		
		final CustomProgress progress = new CustomProgress(this, "");
		progress.show();
		
		
		loginHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				VReturn vReturn = (VReturn)msg.obj;
				progress.dismiss();
				 if((vReturn.getReturnValue() == VReturn.SUCCESS || vReturn.getReturnValue() == VReturn.USER_ALREADY_LOGED_IN)){
					 
						if(vReturn.getReturnValue() == VReturn.USER_ALREADY_LOGED_IN){
							if(ClientLoginSession.SESSION_ID == 0){
								// Another person

								util.showDialog("Another user already login!");
								System.err.println("Another person");
								callLogout(ClientLoginSession.SESSION_ID);

							}else{
								// Running in back ground
								System.err.println("Running in back ground");

								// Call Log out
								callLogout(ClientLoginSession.SESSION_ID);
//								vReturn =VMobileIntefaceImpl.getInstance().doLogOut(ivmsAddress,ClientLoginSession.SESSION_ID );
//								if (vReturn.getReturnValue() != VReturn.SUCCESS && vReturn.getReturnValue() != VReturn.USER_INVALID_SESSION) { 
//									Log.i("Log out ", "System closing without logged-out successfully.\nError Code " + vReturn.getReturnValue());
//								}else{
//									ClientLoginSession.SESSION_ID = 0;
//								}
//								errMsg = "Old session running ! Try again." ;

								System.err.println("Old session running ! Try again.");
								//							handler.sendEmptyMessage(0);
							}
						}else{
							
							exitHeartBeat = false;
							
							LoginSession loginSession = (LoginSession) vReturn.getResult();		
							ClientLoginSession.SESSION_ID = loginSession.getSessionId();
							ClientLoginSession.SERVER_IP = iPAddress;
							
							if(method == WAN)
								ClientLoginSession.IS_LAN_CONNECTED = false;
							else
								ClientLoginSession.IS_LAN_CONNECTED = true;
								
							
							saveSession(loginSession.getSessionId());
							
							System.err.println("**********************************" + isMyServiceRunning() + "*******************************888888");
							if(!isMyServiceRunning()){
								HeartBeatSender heartBeatSender = new HeartBeatSender(util.mContext);
								heartBeatSender.start();
							}
							
							callGeoLocation(loginSession);	
						}
					 
					 /* LoginSession loginSession = (LoginSession) vReturn.getResult();		
					  ClientLoginSession.SESSION_ID = loginSession.getSessionId();
					  saveSession(loginSession.getSessionId());
					  callGeoLocation(loginSession);	  */
					 
					 }else if(vReturn.getReturnValue() == VReturn.SSC_CONNECTION_ERROR) {
						if(method == LOCAL)
						{
							showLocalFailureDialog();
						}
						else if(method == WAN)
						{
							showWanFailureDialog();							
						}
					 }
					 else
					 {
						 util.showDialog(VReturnMessage.returnMessage.get(vReturn.getReturnValue()));
					 }
				 }
			};
		
		
	
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
			ClientLoginSession.SERVER_IP = iPAddress;	
			vSmartMobileInterface = new VSmartMobileInterfaceImpl(iPAddress);				 
			VReturn vReturn = vSmartMobileInterface.mLogin(userName, password, util.getIMSIId());		
			
			loginHandler.sendMessage(Message.obtain(loginHandler, 0, vReturn));	
			}
		}).start();
		
	}
	
	private void saveSession(long sessionId)
	{
		SharedPreferences settings=getSharedPreferences("mypers",0);
		SharedPreferences.Editor editor=settings.edit();
		editor.putLong("session_id", sessionId);
		editor.commit();
	}

	protected void callGeoLocation(final LoginSession loginSession) 
	{
		locationHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				VReturn vReturn = (VReturn)msg.obj;
				  if(vReturn.getReturnValue() == VReturn.SUCCESS){
				   Constant.vChannels = (ArrayList<VChannel>) vReturn.getResult();
				   callDashboardActivity();
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
				
				VReturn vReturn = vSmartMobileInterface.mGetGeoChannels(loginSession.getSessionId());
				locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
			}
		}).start();
		
	}

	protected void showWanFailureDialog() 
	{
		final Dialog dialog = new Dialog(this);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.wan_failure_dialog);
		dialog.show();
		
		lanBtn = (Button)dialog.findViewById(R.id.lan_btn);
		lanBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dialog.dismiss();
				SharedPreferences settings=getSharedPreferences("mypers",0);
				final String localIp = settings.getString("lan_ip", "");
				callLogin(userName.getText().toString(), password.getText().toString(),localIp,LOCAL);
				
			}
		});
		
		okBtn = (Button)dialog.findViewById(R.id.ok_btn);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();				
			}
		});
		
	}

	protected void showLocalFailureDialog() 
	{
		final Dialog dialog = new Dialog(this);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.local_failure_dialog);
		dialog.show();
		
		wanBtn = (Button)dialog.findViewById(R.id.wan_btn);
		wanBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				SharedPreferences settings=getSharedPreferences("mypers",0);
				final String localIp = settings.getString("wan_ip", "");
				callLogin(userName.getText().toString(), password.getText().toString(),localIp,WAN);
				
			}
		});
		
		okBtn = (Button)dialog.findViewById(R.id.ok_btn);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				
			}
		});
		
	}

	private void callDashboardActivity() 
	{
		Intent intent = new Intent(this,DashboardActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);		
		
	}

	private void startSettingActivity() 
	{
		Intent intent = new Intent(this,SettingActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);		
		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	
		if(keyCode == event.KEYCODE_BACK)
		{
			RegisterActivities.removeAllActivities();
		}
		
		return super.onKeyDown(keyCode, event);
}
	
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.android.videonatic.activity.LoginActivity".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	protected void callLogout(final long loginSession) 
	{
		locationHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				VReturn vReturn = (VReturn)msg.obj;
				if(vReturn.getReturnValue() == VReturn.SUCCESS){
					util.showDialog("Old session running ! Try again.");
					LoginActivity.exitHeartBeat = true;
				}else{
					System.err.println("Problem in log out");
				}
			}
		};



		new Thread(new Runnable() {			
			@Override
			public void run() {

				VReturn vReturn = vSmartMobileInterface.mLogOut(loginSession);
				locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
			}
		}).start();

	}
	
	private class HeartBeatSender extends Thread {

		Context context = null;
		private int heartBeatCounter = 0;
		private int backGroundCounter = 0;
		private static final int BACKGROUND_TIME_LIMIT = 60*1000;
		private static final int HEART_BEAT_CHECK_INTERVALE = 5000;
		
		public HeartBeatSender(Context context) {
			super();
			this.context = context;
		}

		public void run() {
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				Log.d("Error",e.getMessage());
			}
			while (!exitHeartBeat) {
				boolean isInForground = false;

				ActivityManager activityManager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
				List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

				for(RunningAppProcessInfo appProcess : appProcesses){
					if(appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND ){

						if(appProcess.processName.equals("com.android.videonatic")){
							Log.i("**********************************appProcess Forgorund ***************************************** ", appProcess.processName);
							isInForground = true;
							backGroundCounter = 0;
							break;
						}

					}
				}

				if(isInForground){
					VReturn vReturn = vSmartMobileInterface.mHeartBeat(ClientLoginSession.SESSION_ID );
					//					clientInterface.

					System.err.println("**********************************vReturn doHeartBeat **********************************" + vReturn.getReturnValue());
					if (vReturn.getReturnValue() == VReturn.SUCCESS) { 
						heartBeatCounter = 0;
					} else if (vReturn.getReturnValue() == VReturn.USER_INVALID_SESSION) { 
						String message = "Login Session expired.";
						if (vReturn.getResult() != null) { 
							VUserMessage userMessage = (VUserMessage) vReturn.getResult();
							message = message + "\nYou have been forcefully logged out by '" + userMessage.getFromUser() +
									"'.\nMessage for you from '" + userMessage.getFromUser() + "' - " + userMessage.getMessage();
						}
						//						System.exit(0);
						Log.i("**********************************Info","Heart Beat Stopping... **********************************");
						RegisterActivities.removeAllActivities();
						exitHeartBeat =  true;
					} else { 
						if (heartBeatCounter >= 5) { 
							//							System.exit(0);
							Log.i("Info","**********************************Heart Beat Stopping... **********************************");
							RegisterActivities.removeAllActivities();
							exitHeartBeat =  true;
						} else { 
							heartBeatCounter++;
						}
					}
				}else{
					boolean isInBackground = false;
					activityManager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
					appProcesses = activityManager.getRunningAppProcesses();

					for(RunningAppProcessInfo appProcess : appProcesses){
						if(appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND ){

							if(appProcess.processName.equals("com.android.videonatic")){
								Log.i("**********************************appProcess-- Back ground ********************************** ", appProcess.processName);
								isInBackground = true;
								backGroundCounter ++;
								break;
							}

						}
					}
					if(backGroundCounter <= (BACKGROUND_TIME_LIMIT/HEART_BEAT_CHECK_INTERVALE)){
						VReturn vReturn = vSmartMobileInterface.mHeartBeat(ClientLoginSession.SESSION_ID );
						//					clientInterface.
						if (vReturn.getReturnValue() == VReturn.SUCCESS) { 
							heartBeatCounter = 0;
						} else if (vReturn.getReturnValue() == VReturn.USER_INVALID_SESSION) { 
							String message = "Login Session expired.";
							if (vReturn.getResult() != null) { 
								VUserMessage userMessage = (VUserMessage) vReturn.getResult();
								message = message + "\nYou have been forcefully logged out by '" + userMessage.getFromUser() +
										"'.\nMessage for you from '" + userMessage.getFromUser() + "' - " + userMessage.getMessage();
							}
							RegisterActivities.removeAllActivities();
						} else { 
							if (heartBeatCounter >= 5) { 
								RegisterActivities.removeAllActivities();
								exitHeartBeat =  true;
							} else { 
								heartBeatCounter++;
							}
						}
					}else{
						//						Log.i("isInForground App", "********* false ************" );
						VReturn vReturn = vSmartMobileInterface.mLogOut(ClientLoginSession.SESSION_ID );
						if (vReturn.getReturnValue() != VReturn.SUCCESS && vReturn.getReturnValue() != VReturn.USER_INVALID_SESSION) { 
							Log.i("**********************************Log out **********************************", "System closing without logged-out successfully.\nError Code " + vReturn.getReturnValue());
						}else{
							ClientLoginSession.SESSION_ID = 0;
						}
						RegisterActivities.removeAllActivities();
						exitHeartBeat =  true;
					}

				}


				try {
					sleep(HEART_BEAT_CHECK_INTERVALE);
				} catch (InterruptedException e) {
					System.err.println(e.toString());
				}
			}
		}
	}

}
