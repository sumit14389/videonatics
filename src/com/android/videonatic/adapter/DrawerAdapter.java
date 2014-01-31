package com.android.videonatic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.videonatic.R;


public class DrawerAdapter extends ArrayAdapter<String>{
	
	int resourceId;
	Context mContext;
	String[] gridAdapterDataList;
	
	public DrawerAdapter(Context context, int viewResourceId,
			String[] objects) {
		super(context, viewResourceId, objects);

		mContext = context;
		resourceId = viewResourceId;
		gridAdapterDataList = objects;
		
		
		
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		final ViewHolder viewHolder;
		
		if (convertView == null) {

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resourceId, null);
			
			viewHolder = new ViewHolder();
			
			viewHolder.imageIcon = (ImageView)convertView.findViewById(R.id.imgIcon);
			viewHolder.txtTitle = (TextView)convertView.findViewById(R.id.txtTitle);
			
			convertView.setTag(viewHolder);
			
		} else {
			
			viewHolder = (ViewHolder)convertView.getTag();
			
		}	
		
		switch(position)
		{
		case 0:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_home);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 1:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_video_group);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 2:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_site_map);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 3:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_video_location);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 4:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_upload);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 5:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_logout);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		default:
			break;
		}
		
	
		return convertView;
	}
	
	
	
	private class ViewHolder
	{
		TextView txtTitle;
		ImageView imageIcon;
	}

}
