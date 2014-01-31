package com.android.videonatic.util;

import android.graphics.Bitmap;

public interface ImageCallback {
	public void imageLoaded(Bitmap imgBitmap, Short channelId);
}
