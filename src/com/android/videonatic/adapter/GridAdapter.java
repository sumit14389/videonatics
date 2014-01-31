package com.android.videonatic.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.android.videonatic.R;
import com.android.videonatic.activity.VideoByGroupListActivity;
import com.android.videonatic.constant.Constant;
import com.android.videonatic.customview.HorizontalListView;
import com.android.videonatic.stream.MjpegLiveViewFromGroupOrLocation;
import com.android.videonatic.util.AsyncImageLoader;
import com.android.videonatic.util.ClientLoginSession;
import com.android.videonatic.util.ImageCallback;
import com.videonetics.component.VChannel;
import com.videonetics.controller.VSmartMobileInterfaceImpl;

public class GridAdapter extends ArrayAdapter {

	int resourceId;
	Context mContext;
	Vector<String> gridAdapterDataList;
	ArrayList mVChannelList;
	private HashMap<Short, SoftReference<Bitmap>> imageCache;
	private VSmartMobileInterfaceImpl vSmartMobileInterface;
	private HorizontalListView listView;
	private Handler imageHandler;

	public GridAdapter(Context context, int viewResourceId,
			ArrayList channelList, HorizontalListView horizontalListView) {
		// TODO Auto-generated constructor stub
		super(context, viewResourceId, channelList);
		mContext = context;
		resourceId = viewResourceId;
		mVChannelList = channelList;
		this.listView = horizontalListView;
		imageCache = new HashMap<Short, SoftReference<Bitmap>>();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;

		if (convertView == null) {

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resourceId, null);

			viewHolder = new ViewHolder();
			viewHolder.asyncImageLoader = new AsyncImageLoader(mContext, -1,
					imageCache);
			viewHolder.thumbnailImage = (ImageView) convertView
					.findViewById(R.id.thumbnail_img_view);

			convertView.setTag(viewHolder);

		} else {

			viewHolder = (ViewHolder) convertView.getTag();

		}
		/** For Fetching Image **/
		Short channelId = 0;
		if (mVChannelList.get(position) instanceof VChannel) {
			VChannel vChannel = (VChannel) mVChannelList.get(position);
			channelId = vChannel.getId();
		}
			/*imageHandler = new Handler() {
				public void handleMessage(Message message) {
					VReturn vReturn = (VReturn) message.obj;
					byte[] imageByte = (byte[]) vReturn.getResult();
					if (imageByte != null) {
						Bitmap bmp = getBitmap(imageByte);
						viewHolder.thumbnailImage.setImageBitmap(bmp);
						int hight = viewHolder.thumbnailImage.getLayoutParams().height;
						int width = viewHolder.thumbnailImage.getLayoutParams().width;
						Log.d("Thumb img h * w in grid", "" + hight + ""
								+ width);
					}

				}
			};

			new Thread(new Runnable() {

				@Override
				public void run() {

					vSmartMobileInterface = new VSmartMobileInterfaceImpl(
							ClientLoginSession.SERVER_IP);
					VReturn vReturn = vSmartMobileInterface.mGetImageSnap(
							ClientLoginSession.SESSION_ID, channelId, 160, 120);

					vReturn.getResult();

					imageHandler.sendMessage(Message.obtain(imageHandler, 0,
							vReturn));
				}
			}).start();
		}*/

		viewHolder.thumbnailImage.setTag(channelId);

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
//		viewHolder.thumbnailImage.setOnClickListener(onThumbnailClickListener);
//		viewHolder.thumbnailImage.setTag(channelId);
		return convertView;
	}
	/*OnClickListener onThumbnailClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			VChannel vChannel  = (VChannel) Constant.vChannelsListView.get(position);
			Intent intent = new Intent(mContext,MjpegLiveViewFromGroupOrLocation.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("channel", vChannel);
			bundle.putString("serverAddress", ClientLoginSession.SERVER_IP);
//			bundle.putString("fromWhichActivity", fromActivity);
			intent.putExtras(bundle);
			//customProgress.dismiss();
			mContext.startActivity(intent);
		}
	};*/
	private class ViewHolder {
		ImageView thumbnailImage;
		AsyncImageLoader asyncImageLoader;
	}

//	private Bitmap getBitmap(byte[] b) {
//		return BitmapFactory.decodeByteArray(b, 0, b.length);
//	}
}
