package com.android.videonatic.stream;

import java.io.File;
import java.util.Hashtable;

import android.graphics.Color;

import com.videonetics.component.VChannel;

public class ViewDetails {

	public static Color lanComponentStartColor = new Color();
	public static Color wanComponentStartColor = new Color();
//	public static Color componentEndColor = Color.WHITE;
	

//	public static Color containerstartColor = new Color(140, 180, 225);
//	public static Color containerendColor =  Color.white;
//	
//	public static Color TABLE_ODD_ROW_COLOR = Color.white;
//	public static Color TABLE_EVEN_ROW_COLOR = Color.lightGray;
//	public static Color TABLE_SELECTED_ROW_COLOR = new Color(223, 201, 162);

//	public static Dimension defaultDim = new Dimension(1024, 768);
	public static File lastStorageDirectory = null;
	public static boolean showVideoDetails = true;
	public static int defaultImageWidth = 320;
	public static int defaultImageHeigh = 240;
	
	public static long COPY_TIME_VALUE = -1;
	
	public static byte getStreamType(int width, int height) { 
		byte streamType = VChannel.D1;
		if (width >= 1000 || height >= 750) { 
			streamType = VChannel.HD;
		} else if (width >= 500 || height >= 375) { 
			streamType = VChannel.D1;
		} 
		return streamType;
	}
	
	
	public static int getBitrate(int width, int height) { 
		int bitrate = 32;
		if (width >= 1000 || height >= 750) { 
			bitrate = 256;
		} else if (width >= 750 || height >= 562) { 
			bitrate = 128;
		}  else if (width >= 500 || height >= 375) { 
			bitrate = 128;
		}  else if (width >= 375 || height >= 281) { 
			bitrate = 64;
		} else if (width >= 250 || height >= 187) { 
			bitrate = 32;
		} 
		return bitrate;
	}
	
	
	public static int getResizeValue(int width, int height) { 
		int resizeValue = 100;
		if (width <= 320 ) { 
			resizeValue = 50;
		} else if (width <= 800 ) { 
			resizeValue = 200;
		}  else {
			resizeValue = 250;
		} 
		return resizeValue;
	}
	
//	public static Color getHitmapColor(byte value) {
//		Color color = Color.red;
//		int alpha = 50 + (4 * (value % 25));
//		if (value < 25) { 
//			color = new Color(0, 0, 255, alpha);
//		}  else if (value < 50) { 
//			color = new Color(0, 255, 0, alpha);
//		} else if (value < 75) { 
//			color = new Color(255, 128, 0, alpha);
//		} else {
//			color = new Color(255, 0, 0, alpha);
//		}
//		return color;
//	}
	
	
	public static Hashtable<Short, VChannel> currentRunningChannels = new Hashtable<Short, VChannel>();

//	public static final ImageIcon cameraIcon = new ImageIcon("iconset/Camera/Camera_16x16.png");
//	public static final ImageIcon locationIcon = new ImageIcon("iconset/Globe/Globe_16x16.png");
//	public static final ImageIcon groupIcon = new ImageIcon("iconset/Group/Group 16x16.png");
//	public static final ImageIcon treeIcon = new ImageIcon("iconset/Tree/tree 16x16.png");
//	public static final ImageIcon ptzIcon = new ImageIcon("iconset/Ptz Camera/Camera 16x16.png");
}
