package com.android.videonatic.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class Util {
	
	public Context mContext;
	
	public Util(Context context)
	{
		this.mContext = context;
	}
	
	public String getIMSIId()
	{
		String imsiId = null;
		
		TelephonyManager mTelephonyMgr = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		imsiId = mTelephonyMgr.getSubscriberId();
		return imsiId;
	}
	
public boolean isConnectionPossible(){
		
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    return (netInfo != null && netInfo.isConnected()) ;

	}
/**
 * 
 * @param context
 *            : context of the dialog
 * @param msg
 *            : Message to appear
 */
public static void showDialog(Context context, int msg)

{
	new AlertDialog.Builder(context).setMessage(msg).setCancelable(false)
			.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			}).show();

}
public void showDialog(String msg)

{
	new AlertDialog.Builder(mContext).setMessage(msg).setCancelable(false)
			.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			}).show();

}

}
