package com.android.videonatic.stream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.android.videonatic.R;
import com.android.videonatic.util.ClientLoginSession;
import com.videonetics.component.VChannel;
import com.videonetics.mobile.videoreciver.ViewDetails;
import com.videonetics.model.output.VFrame;
import com.videonetics.values.VMediaProperty;
import com.videonetics.values.VPorts;




public class VaMjpegLiveReceiver extends Thread{


	private DataInputStream videoStream;
	private boolean connected = false;
	private Context livecontext = null;
	private Bitmap bitmapNoconnect = null;
	private int requestBitrate;
	public static boolean ptzControlOn  = true;
	public static boolean ptzControlUp  = false;
	public static boolean ptzControlLeft  = false;
	public static boolean ptzControlRight  = false;
	public static boolean ptzControlDown  = false;
	
	public static boolean isPtzCam  = false;
	private Socket socket = null;
	private DataOutputStream out = null;
	private String serverAddress = "127.0.0.1";
	
	private VChannel channelLive = null;
	int prefferedWidth = 320;
	int preferedHeight = 240;


	public VaMjpegLiveReceiver(String serverAddress, VChannel vChannel ,Context context,int prefferedWidth,  int preferedHeight){

		livecontext = context;
		this.serverAddress = serverAddress;
		this.channelLive = vChannel;
		bitmapNoconnect = BitmapFactory.decodeResource(livecontext.getResources(),
				R.drawable.connecting_server);
		this.prefferedWidth = prefferedWidth;
		this.preferedHeight = preferedHeight;
		
		if(ClientLoginSession.IS_LAN_CONNECTED){
			
			this.requestBitrate = 16 * ViewDetails.getBitrate(this.prefferedWidth, this.preferedHeight);
			
		}else{
			this.requestBitrate = ViewDetails.getBitrate(prefferedWidth, preferedHeight);
			
		}

	}

	private void connect() {
		connected = false;
//		System.out.println("Trying to connect remote media server IP " + serverAddress + "PORT " + VPorts.REMOTE_MEDIA_SERVER + " CH " + channelLive.getId());
		try {
			String serverIp = InetAddress.getByName(serverAddress).getHostAddress();
			socket = new Socket(serverIp, VPorts.REMOTE_MEDIA_SERVER);
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(5000);
			out = new DataOutputStream(socket.getOutputStream());
			videoStream = new DataInputStream(socket.getInputStream());
			out.writeShort(channelLive.getId());
			out.writeInt(this.prefferedWidth);
			out.writeInt(this.preferedHeight);
			out.writeInt(this.requestBitrate);
			out.writeInt(VMediaProperty.MJPG);
			out.flush();
//			System.out.println("Success to connect remote media server IP " + serverAddress + "PORT " + VPorts.REMOTE_MEDIA_SERVER + " CH " + channelLive.getId());
			connected = true;
		} catch (Exception e) {
			System.err.println("Unable to connect remote media server IP " + serverAddress + "PORT " + VPorts.REMOTE_MEDIA_SERVER + " CH " + channelLive.getId());
			connected =  false;
			try {
				this.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				Log.d("Error", e1.getMessage());
			}
		}
	}



	public Bitmap readMjpegFrame() {

//		System.err.println("**************readMjpegFrame **********88");
		byte[] jpegImage = null;
		Bitmap bitmap = null;
		Bitmap newImage = null;


		if (!connected) { 
			connect();
		} 

		if (connected) {
			
			try {
				int vasOutputSize = videoStream.readInt();
				if (vasOutputSize > 0) { 
					byte[] rawVFrame = new byte[vasOutputSize];
					videoStream.readFully(rawVFrame);
					try { 
						ByteArrayInputStream bais = new ByteArrayInputStream(rawVFrame);
						ObjectInputStream ois = new ObjectInputStream(bais);
						try {
							VFrame vFrame = (VFrame) ois.readObject();
							jpegImage = vFrame.getFrame();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							Log.d("Error", e.toString());
							if(closeConnection()){
								connected =  false;
							}
							
						}
					}catch (IOException e) {
						// TODO Auto-generated catch block
						Log.d("Error", e.toString());
						if(closeConnection()){
							connected =  false;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("Error", e.toString());
				if(closeConnection()){
					connected =  false;
				}
				
			}
			
		}


		if (jpegImage != null) {
			try {
				//   Dump in afile
				
				
				bitmap =  BitmapFactory.decodeStream(new ByteArrayInputStream(jpegImage));
//				int wid = bitmap.getWidth();
//				int  hgt = bitmap.getHeight();
//				Canvas canvas = null;
//				Drawable drawable = null;
//				
//				newImage = Bitmap.createBitmap
//						(wid, hgt, Bitmap.Config.ARGB_8888);
//				canvas = new Canvas(newImage);
//				
//				canvas.drawBitmap(bitmap, 0f, 0f, null);
//
//				drawable = this.livecontext.getResources().getDrawable(R.drawable.connecting_server);
//				drawable.setBounds(wid - 80,0, wid - 40, 50);
//				
//				//drawable.draw(canvas);
//				bitmap = newImage;

//				canvas = null;
//				drawable = null;

			} catch (Exception e) {
				Log.e("Error6", e.toString());
			}
			
			

		}else{
			
			bitmap = bitmapNoconnect;
			int wid = bitmap.getWidth();
			int  hgt = bitmap.getHeight();
			Canvas canvas = null;
			Drawable drawable = null;
			
			newImage = Bitmap.createBitmap
					(wid, hgt, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(newImage);
			
			canvas.drawBitmap(bitmap, 0f, 0f, null);

			drawable = this.livecontext.getResources().getDrawable(R.drawable.connecting_server);
			drawable.setBounds(wid - 80,0, wid - 40, 50);
			
			
			
			drawable.draw(canvas);
			bitmap = newImage;
		}
		
		return bitmap;
	}

	public boolean closeConnection() {
		System.err.println("closeConnection enter");
		try { 
			videoStream.close();
			out.close();
			socket.close();
			System.err.println("closeConnection out");
			return true;
		} catch (Exception e) {  
			Log.e("Error",  e.toString());
//			System.err.println("Unable to disconnect  online evenr server" +
//					" or connection alreday closed" + e.toString());
			return false;
		}
		
	}

	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @param connected the connected to set
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	
	
}
