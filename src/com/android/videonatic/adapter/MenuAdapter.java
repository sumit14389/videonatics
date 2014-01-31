package com.android.videonatic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.videonatic.R;

public class MenuAdapter extends ArrayAdapter<String>{
	
	int resourceId;
	Context mContext;
	String[] gridAdapterDataList;
	
	public MenuAdapter(Context context, int viewResourceId,
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
			viewHolder.imageIcon.setImageResource(R.drawable.ic_archive);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 1:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_event_list);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 2:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_log);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 3:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_setting);
			viewHolder.txtTitle.setText(gridAdapterDataList[position]);
			break;
		case 4:
			viewHolder.imageIcon.setImageResource(R.drawable.ic_ptz_menu);
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
