package com.android.videonatic.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

import com.android.videonatic.R;
import com.android.videonatic.constant.Constant;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.values.VReturn;

public class ImageFromUrl {

	int sampleSize;
	Short _channelId;
	Context mContext;
	private VSmartMobileInterfaceImpl vSmartMobileInterface;
	private static VReturn vReturn;

	public ImageFromUrl(Context context, Short chennelId, int sampleSize) {
		this.mContext = context;
		this._channelId = chennelId;
		this.sampleSize = sampleSize;
	}

	public Bitmap getImageFromWebOperations() {

		Bitmap mBitmap = null;
		BitmapFactory.Options opts = new Options();
		opts.inSampleSize = this.sampleSize;
		opts.inPurgeable = true;
		try {
			// InputStream is = (InputStream) new URL(url).getContent();
			// mBitmap = BitmapFactory.decodeStream(new FlushedInputStream(is),
			// new Rect(-1, -1, -1, -1), opts);
			if (sampleSize == -1) {
//				InputStream is = (InputStream) new URL(_channelId).getContent();
//				mBitmap = BitmapFactory.decodeStream(
//						new FlushedInputStream(is), new Rect(-1, -1, -1, -1),
//						opts);
				vSmartMobileInterface = new VSmartMobileInterfaceImpl(ClientLoginSession.SERVER_IP);			
				vReturn = vSmartMobileInterface.mGetImageSnap(ClientLoginSession.SESSION_ID, _channelId,160, 120);
				
				 vReturn.getResult();
				 
				 byte[] imageByte = (byte[]) vReturn.getResult();
				 if(imageByte != null)
					 mBitmap = BitmapFactory.decodeByteArray(imageByte , 0, imageByte .length);
				 else
				 {
					//Bitmap scaledBitmap = Bitmap.createScaledBitmap(mContext.getResources(), 160, 120, true);
					mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_image); 
				 }
			} 
			else {
				//mBitmap = BitmapFactory.decodeByteArray(_channelId,160,120);
//				mBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.video_thumb);
//				Log.e("problem", "hard luck for fetching image");
			}

		} catch (Exception e) {
			Log.e(Constant.TAG_NAME, "Error in getting " + _channelId + " -- " + e);
			opts.inSampleSize = -1;
			mBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.video_thumb);
		}
		return mBitmap;
	}

	/*private static Bitmap decodeSampledBitmapFromResource(Short _channelId2,
			int reqWidth, int reqHeight) throws MalformedURLException,
			IOException {

		// First decode with inJustDecodeBounds=true to check dimensions
//		final BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeStream((InputStream) new URL(_channelId2).getContent(),
//				new Rect(-1, -1, -1, -1), options);
//
//		// Calculate inSampleSize
//		options.inSampleSize = calculateInSampleSize(options, reqWidth,
//				reqHeight);
//
//		// Decode bitmap with inSampleSize set
//		options.inJustDecodeBounds = false;
		
		byte[] imageByte = (byte[]) vReturn.getResult();
		 
		 return BitmapFactory.decodeByteArray(imageByte , 0, imageByte .length);

	}

	private static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);

	}

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}*/
}
