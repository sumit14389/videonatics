package com.android.videonatic.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.videonatic.R;
import com.android.videonatic.util.AsyncImageLoader;
import com.android.videonatic.util.ImageCallback;
import com.videonetics.component.VChannel;
import com.videonetics.controller.VSmartMobileInterfaceImpl;

public class ListAdapter extends ArrayAdapter{
	
	int resourceId;
	Context mContext;
	ArrayList listAdapterDataList;
	private VSmartMobileInterfaceImpl vSmartMobileInterface;
	private Handler imageHandler;
	private HashMap<Short, SoftReference<Bitmap>> imageCache;
	ListView listView;
	
	public ListAdapter(Context context, int viewResouceId, ArrayList vChannelsListView, ListView videoThumbList)
	{
		super(context, viewResouceId, vChannelsListView);
		
		mContext = context;
		resourceId = viewResouceId;
		listAdapterDataList = vChannelsListView;
		listView = videoThumbList;
		imageCache = new HashMap<Short, SoftReference<Bitmap>>();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		final ViewHolder viewHolder;
		
		if (convertView == null) {

			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resourceId, null);
			
			viewHolder = new ViewHolder();
			viewHolder.thumbImage = (ImageView) convertView.findViewById(R.id.thumb_img_list);
			viewHolder.videoLocationName = (TextView) convertView.findViewById(R.id.video_name_txt_view);
			viewHolder.asyncImageLoader = new AsyncImageLoader(mContext, -1,
					imageCache);
			convertView.setTag(viewHolder);
			
		} else {
			
			viewHolder = (ViewHolder)convertView.getTag();
			
		}	
		/** For Fetching Image **/
		Short channelId = 0;		
		if (listAdapterDataList.get(position) instanceof VChannel) {
			VChannel vChannel = (VChannel) listAdapterDataList.get(position);
			String videoLocationName = vChannel.getName();
			channelId = vChannel.getId();
			viewHolder.videoLocationName.setText(videoLocationName);
			
			/*imageHandler = new Handler() {
				public void handleMessage(Message message) {
					VReturn vReturn = (VReturn) message.obj;
					byte[] imageByte = (byte[]) vReturn.getResult();
					if (imageByte != null) {
						Bitmap bmp = getBitmap(imageByte);
						viewHolder.thumbImage.setImageBitmap(bmp);
						int hight = viewHolder.thumbImage.getLayoutParams().height;
						int width = viewHolder.thumbImage.getLayoutParams().width;
						Log.d("Thumb img h * w in list", ""+hight+""+width);
					}

				}
			};
		new Thread(new Runnable() {

			@Override
			public void run() {

				vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);
				VReturn vReturn = vSmartMobileInterface.mGetImageSnap(ClientLoginSession.SESSION_ID, channelId, 160, 120);

				vReturn.getResult();

				imageHandler.sendMessage(Message.obtain(imageHandler, 0,vReturn));
			}
		}).start();*/
		}
		viewHolder.thumbImage.setTag(channelId);
		Bitmap cachedImage = viewHolder.asyncImageLoader.loadDrawable(position,
				channelId, new ImageCallback() {

					@SuppressWarnings("deprecation")
					public void imageLoaded(Bitmap imgBitmap, Short channelId) {

						ImageView imageViewByTag = (ImageView) listView
								.findViewWithTag(channelId);
						if (imageViewByTag != null) {
//							Drawable drawable = new BitmapDrawable(mContext.getResources(),imgBitmap);
//							imageViewByTag.setBackgroundDrawable(drawable);
							Bitmap scaledBitmap = Bitmap.createScaledBitmap(imgBitmap, 160, 120, true);
							imageViewByTag.setImageBitmap(scaledBitmap);
						}
					}
				});
		return convertView;
	}
		
	private class ViewHolder
	{
		ImageView thumbImage;
		TextView videoLocationName;
		AsyncImageLoader asyncImageLoader;
	}
	
//	private Bitmap getBitmap(byte[] b) 
//	{
//		return BitmapFactory.decodeByteArray(b, 0, b.length);
//	}
}
