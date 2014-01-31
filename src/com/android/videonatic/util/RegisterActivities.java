package com.android.videonatic.util;

import java.util.Vector;

import android.app.Activity;

public final class RegisterActivities {
public static Vector<Activity> activityStore = new Vector<Activity>();
	
	private RegisterActivities() {
		// TODO Auto-generated constructor stub
	}

	public static void registerActivity(Activity context){
		activityStore.addElement(context);		
	}
	
	public static Vector<Activity> getAllActivities(){
		
		return activityStore;
	}
	
	public static void removeActivityAt(int index)
	{
		activityStore.elementAt(index).finish();
	}
	
	
	public static void removeAllActivities(){
				
    	for(int i = 0 ; i < activityStore.size() ; i++ )
    	{    		
    		(activityStore.elementAt(i)).finish();							        		
    	}
    	
		System.runFinalizersOnExit(true);
		activityStore.removeAllElements();
	}
}
