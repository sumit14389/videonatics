package com.android.videonatic.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.videonetics.controller.VSmartMobileInterfaceImpl;

public class AsyncImageLoader {

	// private int position;
	private final HashMap<Short, SoftReference<Bitmap>> imageCache;
	private final Context mContext;
	private final int inSampleSize;
	public VSmartMobileInterfaceImpl vSmartMobileInterface;
	String guid;

	public AsyncImageLoader(Context context, int samplesize,
			HashMap<Short, SoftReference<Bitmap>> imageCache2) {

		mContext = context;
		this.inSampleSize = samplesize;
		this.imageCache = imageCache2;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Bitmap loadDrawable(final int position, final Short channelId,
			final ImageCallback imageCallback) {

		// this.position = position;
		// if (imageCache.containsKey(imageUrl)) {
		// SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
		// Bitmap bitmap = softReference.get();
		// if (bitmap != null) {
		// return bitmap;
		// }
		// }

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Bitmap) message.obj, channelId);
			}
		};

		new Thread() {
			@Override
			public void run() {
				ImageCacheTask imgCacheTask = new ImageCacheTask(mContext,
						imageCache, channelId, inSampleSize);
				Bitmap mBitmap = imgCacheTask.getBitmapFromCache();
				imageCache.put(channelId, new SoftReference<Bitmap>(mBitmap));

				Message message = handler.obtainMessage(position, mBitmap);
				handler.sendMessage(message);
			}

		}.start();
		return null;

	}
}

