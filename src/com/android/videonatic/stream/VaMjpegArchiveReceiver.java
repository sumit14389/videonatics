package com.android.videonatic.stream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.GregorianCalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.android.videonatic.R;
import com.android.videonatic.util.ClientLoginSession;
import com.videonetics.component.VChannel;

import com.videonetics.model.output.VFrame;
import com.videonetics.util.VUtilities;
import com.videonetics.values.VMediaProperty;
import com.videonetics.values.VPorts;




public class VaMjpegArchiveReceiver extends Thread{


	private DataInputStream videoStream;
	private boolean connected = false;
	private Context livecontext = null;
	private Bitmap bitmapNoconnect = null;
	private int requestBitrate;
	private Socket socket = null;
	private DataOutputStream out = null;
	private long initTimeStamp = 0;
	private String serverAddress = "127.0.0.1";
	
	private VChannel channelLive = null;
	int prefferedWidth = 320;
	int preferedHeight = 240;
	private long streamerSessionId;
	private int moveChoice = VMediaProperty.FORWARD_PLAY;
	private int ipChoice = VMediaProperty.IP_PLAY;
	private long lastFrameTimestamp = 0, lastFireTime = 0;
	private GregorianCalendar cal = new GregorianCalendar();
	
	Bitmap lastbitmap = null;


	public VaMjpegArchiveReceiver(String serverAddress, VChannel vChannel  ,long initTimeStamp,Context context, int prefferedWidth,  int preferedHeight){

		livecontext = context;
		this.serverAddress = serverAddress;
		this.channelLive = vChannel;
		this.initTimeStamp = initTimeStamp;
		this.prefferedWidth = prefferedWidth;
		this.preferedHeight = preferedHeight;
		//this.requestBitrate = 15;
		
		//this.requestBitrate = ViewDetails.getBitrate(prefferedWidth, preferedHeight);
		
		if(ClientLoginSession.IS_LAN_CONNECTED){
			
			this.requestBitrate = 16 * ViewDetails.getBitrate(this.prefferedWidth, this.preferedHeight);
			
		}else{
			this.requestBitrate = ViewDetails.getBitrate(prefferedWidth, preferedHeight);
			
		}


	}

	private void connect() {
		connected = false;
		System.out.println("Trying to connect remote media server IP " + serverAddress + "PORT " + VPorts.REMOTE_MEDIA_SERVER + " CH " + channelLive.getId());
		System.out.println("requestBitrate " + requestBitrate + "prefferedWidth " + prefferedWidth + " preferedHeight " + preferedHeight);
		try {
			String serverIp = InetAddress.getByName(serverAddress).getHostAddress();
			socket = new Socket(serverIp, VPorts.REMOTE_CLIP_STREAMING_PORT);
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(5000);
			out = new DataOutputStream(socket.getOutputStream());
			videoStream = new DataInputStream(socket.getInputStream());
			out.writeShort(channelLive.getId());
			out.writeLong(initTimeStamp);
			out.writeInt(this.prefferedWidth);
			out.writeInt(this.preferedHeight);
			out.writeInt(this.requestBitrate);
			out.writeInt(VMediaProperty.MJPG);
			out.flush();
			streamerSessionId = videoStream.readLong();
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
					
					int commandId = videoStream.readInt();
					int streamType = videoStream.readInt();
					int mediaType = videoStream.readInt();
					int frameType = videoStream.readInt();
					int bitrate = videoStream.readInt();
					int fps = videoStream.readInt();
					long timestamp = videoStream.readLong();
					byte[] imageFrame = new byte[vasOutputSize];
					videoStream.readFully(imageFrame);
					try { 
//						System.out.println("1 M " + mediaType + " F " + frameType + " LEN " + vasOutputSize);
							VFrame vFrame = new VFrame();
							vFrame.setBitrate(bitrate);
							vFrame.setFps(fps);
							vFrame.setFrame(imageFrame);
							vFrame.setMediaType(mediaType);
							vFrame.setFrameType(frameType);
							vFrame.setStreamType((byte) streamType);
							vFrame.setTimestamp(timestamp);
							jpegImage = vFrame.getFrame();
							
							
							if (moveChoice == VMediaProperty.FORWARD_PLAY && ipChoice == VMediaProperty.IP_PLAY) { 
								long grabbDiff = vFrame.getTimestamp() - lastFrameTimestamp;
								long timeTaken = System.currentTimeMillis() - lastFireTime;
								if (grabbDiff > timeTaken) { 
									int sleepvalue = (int) (grabbDiff - timeTaken);
									sleepvalue = sleepvalue > 200 ? 200 : sleepvalue;
									try {
										Thread.sleep(sleepvalue);
									} catch (Exception e) {
										System.err.println(e.toString());
									}
								}
							} else {
								try {
									Thread.sleep(25);
								} catch (Exception e) {
									System.err.println(e.toString());
								}
							}
							lastFireTime = System.currentTimeMillis();
							lastFrameTimestamp = vFrame.getTimestamp();
							
							if (jpegImage != null) {
								try {
									
									bitmap =  BitmapFactory.decodeByteArray(jpegImage, 0, jpegImage.length);
									
									int wid = bitmap.getWidth();
									int  hgt = bitmap.getHeight();
									Canvas canvas = null;
									
									newImage = Bitmap.createBitmap
											(wid, hgt, Bitmap.Config.ARGB_8888);
									canvas = new Canvas(newImage);
									
									
									
									Paint paint = new Paint(); 
									
									

									paint.setColor(Color.RED); 
									paint.setTextSize(35);
									paint.setStrokeWidth(35); 
									paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
									canvas.drawBitmap(bitmap, 0f, 0f, null);
									cal.setTimeInMillis(vFrame.getTimestamp());
									canvas.drawText(VUtilities.dateFormat(cal, VUtilities.DATETIME_WITH_MILLIS), wid /2 - 100, hgt - 15, paint); 
								
									
									lastbitmap = newImage;

								} catch (Exception e) {
									Log.e("Error6", e.toString());
								}
								
								

							}else{
								bitmap = lastbitmap;
								
							}
						
					}catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d("Error2", e.toString());
					}
				}else{
					System.err.println("**************vasOutputSize **********="+vasOutputSize);
					if(lastbitmap == null){
						if(bitmapNoconnect == null){
							bitmapNoconnect = BitmapFactory.decodeResource(livecontext.getResources(),
									R.drawable.connecting_server);
							}
						lastbitmap = bitmapNoconnect;
					}
					bitmap = lastbitmap;
					
					
				}
			} catch (IOException e) {
				// Network disconnected
				Log.d("Error3", e.toString());
				if(bitmapNoconnect == null){
					bitmapNoconnect = BitmapFactory.decodeResource(livecontext.getResources(),
							R.drawable.connecting_server);
					}
				bitmap = bitmapNoconnect;
				if(closeConnection()){
					connected =  false;
				}
			}
			
		}


		
		
		
		return bitmap;
	}

	public boolean closeConnection() {
		try { 
			videoStream.close();
			out.close();
			socket.close();
			return true;
		} catch (Exception e) {  
			Log.e("Error",  e.toString());
//			System.err.println("Unable to disconnect  online evenr server" +
//					" or connection alreday closed" + e.toString());
			return false;
		}
		
	}
}
