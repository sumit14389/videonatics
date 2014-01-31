package com.android.videonatic.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

public class ImageCacheTask {
	private final Context mContext;
	private final Short chennelId;
	int sampleSize;
	private final Map<Short, SoftReference<Bitmap>> artCache;

	/**
	 * 
	 * @param context
	 * @param imageCache
	 *            : Map object that contains the soft reference of bitmaps;
	 * @param imageUrl
	 *            : url from where the bitmaps are fetched.
	 * @param sampleSize
	 *            : value by which the original image is sub sampled, to save
	 *            memory.
	 * 
	 * @see http 
	 *      ://developer.android.com/reference/android/graphics/BitmapFactory
	 *      .Options.html#inSampleSize
	 */

	public ImageCacheTask(Context context,
			HashMap<Short, SoftReference<Bitmap>> imageCache, Short channelId,
			int sampleSize) {

		this.mContext = context;
		this.chennelId = channelId;
		this.artCache = imageCache;
		this.sampleSize = sampleSize;

	}

	public void modifyImageUrl(String guid, String imageSize) {

		// NetworkConnection netConn = new NetworkConnection(
		// Configuration.FILMCODE_URL + guid);
		// // System.out.println(Configuration.FILMCODE_URL+guid);
		// String filmCode = netConn.postDataToServer(netConn.getInputStream());
		// if (filmCode != null && filmCode.length() > 0) {
		// imageUrl = Configuration.THUMBNAIL_URL + filmCode + imageSize;
		// sampleSize = -1;
		// } else {
		// imageUrl = Configuration.THUMBNAIL_URL + filmCode + imageSize;
		// sampleSize = 4;
		// }
	}

	public Bitmap getBitmapFromCache() {

		Bitmap img = null;
		if (artCache.containsKey(chennelId)) {

			img = artCache.get(chennelId).get();
			if (img == null) {
				addImageToCache(chennelId);
				img = artCache.get(chennelId).get();
			}
		} else {
			addImageToCache(chennelId);
			SoftReference<Bitmap> softImg = artCache.get(chennelId);
			if (softImg != null)
				img = softImg.get();
		}
		return img;
	}

	private void addImageToCache(Short chennelId) {
		ImageFromUrl imgFromUrl = new ImageFromUrl(mContext, chennelId,
				sampleSize);
		artCache.put(chennelId,new SoftReference<Bitmap>(imgFromUrl.getImageFromWebOperations()));
	}
}
