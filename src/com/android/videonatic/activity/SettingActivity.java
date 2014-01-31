package com.android.videonatic.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.videonatic.R;
import com.android.videonatic.screen.DisplayScreen;
import com.android.videonatic.util.RegisterActivities;
import com.android.videonatic.util.Util;

public class SettingActivity extends DisplayScreen{

	
	private Button localIpBtn;
	private Button wanIpBtn;
	private LinearLayout ipAddressLayout;
	private Button doneBtn;
	private final int LOCAL = 0;
	private final int WAN = 1;
	
	private int METHOD = -1;
	private Util util;
	private EditText ipAddressLocalEdtTxt;
	private EditText ipAddressWanEdtTxt;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);	
		RegisterActivities.registerActivity(this);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		
		util = new Util(this);
		
		initView();
	}
	
	private void initView() 
	{
		localIpBtn = (Button)findViewById(R.id.local_ip_btn);	
		localIpBtn.setOnClickListener(this);
		
		wanIpBtn = (Button)findViewById(R.id.wan_ip_btn);
		wanIpBtn.setOnClickListener(this);
		
		doneBtn = (Button)findViewById(R.id.done_btn);
		doneBtn.setOnClickListener(this);
		
		ipAddressLayout = (LinearLayout)findViewById(R.id.ip_address_layout);
		//ipAddressLayout.setVisibility(View.GONE);
		
		ipAddressLocalEdtTxt = (EditText)findViewById(R.id.ip_address_local_edit_txt);
		ipAddressLocalEdtTxt.setVisibility(View.GONE);
		ipAddressWanEdtTxt = (EditText)findViewById(R.id.ip_address_wan_edit_txt);
		ipAddressWanEdtTxt.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		case R.id.local_ip_btn:
			if(!ipAddressLocalEdtTxt.isShown())
				ipAddressLocalEdtTxt.setVisibility(View.VISIBLE);
			
			if(ipAddressWanEdtTxt.isShown())
				ipAddressWanEdtTxt.setVisibility(View.GONE);
						
			METHOD = LOCAL;
			if(getIP(METHOD)!=null && getIP(METHOD).length()!=0)
				ipAddressLocalEdtTxt.setText(getIP(METHOD));
			else
				ipAddressLocalEdtTxt.setHint("Enter Local IP here");
			break;
		case R.id.wan_ip_btn:
			if(!ipAddressWanEdtTxt.isShown())
				ipAddressWanEdtTxt.setVisibility(View.VISIBLE);
			
			if(ipAddressLocalEdtTxt.isShown())
				ipAddressLocalEdtTxt.setVisibility(View.GONE);
			
			METHOD = WAN;
			if(getIP(METHOD)!=null && getIP(METHOD).length()!=0)
				ipAddressWanEdtTxt.setText(getIP(METHOD));
			else
				ipAddressWanEdtTxt.setHint("Enter WAN IP here");
			break;
		case R.id.done_btn:
			if(METHOD == LOCAL)
			{
				if(ipAddressLocalEdtTxt.length()!=0)
					setIp(METHOD);
				else
					util.showDialog("Enter Ip address");
				
				
			}	
			
			else if(METHOD == WAN)
			{
				if(ipAddressWanEdtTxt.length()!=0)
					setIp(METHOD);
				else
					util.showDialog("Enter Ip address");
			}
			break;
		}
	}

	private void setIp(int method) 
	{
		SharedPreferences settings=getSharedPreferences("mypers",0);
		SharedPreferences.Editor editor=settings.edit();
		switch(method)
		{
		case LOCAL:
			editor.putString("local_ip", ipAddressLocalEdtTxt.getText().toString());	
			ipAddressLocalEdtTxt.setVisibility(View.GONE);
			break;
		case WAN:
			editor.putString("wan_ip", ipAddressWanEdtTxt.getText().toString());	
			ipAddressWanEdtTxt.setVisibility(View.GONE);
			break;
		}
		
		editor.commit();			
		
		Toast.makeText(this, "IP address successfuly added", Toast.LENGTH_LONG).show();
		//ipAddressLayout.setVisibility(View.GONE);
	}
	
	private String getIP(int method)
	{
		String address = null;
		SharedPreferences settings=getSharedPreferences("mypers",0);
		
		switch(method)
		{
		case LOCAL:
			address = settings.getString("local_ip", "");
			break;
		case WAN:
			address = settings.getString("wan_ip", "");
			break;
		default:
			break;
		}
		
		return address;
	}

}
