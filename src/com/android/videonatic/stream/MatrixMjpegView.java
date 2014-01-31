package com.android.videonatic.stream;





import java.nio.channels.Channel;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.android.videonatic.util.ClientLoginSession;
import com.videonetics.component.VChannel;
import com.videonetics.controller.VSmartMobileInterface;
import com.videonetics.controller.VSmartMobileInterfaceImpl;
import com.videonetics.model.action.PtzAction;
import com.videonetics.values.VReturn;
import com.videonetics.values.VReturnMessage;



public class MatrixMjpegView extends SurfaceView implements SurfaceHolder.Callback {
	public final static int POSITION_UPPER_LEFT  = 9;

	public final static int POSITION_UPPER_RIGHT = 3;

	public final static int POSITION_LOWER_LEFT  = 12;

	public final static int POSITION_LOWER_RIGHT = 6;



	public final static int SIZE_STANDARD   = 1;

	public final static int SIZE_BEST_FIT   = 4;

	public final static int SIZE_FULLSCREEN = 8;


	//	private static PtzController ptzController;

	private MjpegViewThread thread;

	private VaMjpegLiveReceiver mIn = null;    

	private boolean showFps = false;

	private boolean mRun = false;

	private boolean surfaceDone = false;    

	private Paint overlayPaint;

	private int overlayTextColor;

	private int overlayBackgroundColor;

	private int ovlPos;

	private int dispWidth;

	private int dispHeight;

	private int displayMode;

	private Point topLeft;
	private Point bottomRight;
	private VChannel channelLive = null;
	private String vmsIpString = null;

	private ArrayList<Channel> channelList = null;
	private VSmartMobileInterface vSmartMobileInterface = null;

	public Context liveConext = null;
	//	private  HashMap< Short, Vas> channelVasTab = null;

	private  String touchMode = "";
	private float oldDist = 0;

	//	private RemoteLiveOutputClient liveOutputClient = null;
	private boolean pauseFlag = false;

	private Handler locationHandler;


	public class MjpegViewThread extends Thread {

		private SurfaceHolder mSurfaceHolder;

		private int frameCounter = 0;

		private long start;

		private String fps = "";

		private Bitmap ovl;
		



		public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {

			mSurfaceHolder = surfaceHolder;

		}



		private Rect destRect(int bmw, int bmh) {

			int tempx;

			int tempy;

			if (displayMode == MatrixMjpegView.SIZE_STANDARD) {

				tempx = (dispWidth / 2) - (bmw / 2);

				tempy = (dispHeight / 2) - (bmh / 2);

				topLeft = new Point(tempx, tempy);

				bottomRight = new Point( bmw + tempx, bmh + tempy);

				return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);

			}

			if (displayMode == MatrixMjpegView.SIZE_BEST_FIT) {

				float bmasp = (float) bmw / (float) bmh;

				bmw = dispWidth;

				bmh = (int) (dispWidth / bmasp);

				if (bmh > dispHeight) {

					bmh = dispHeight;

					bmw = (int) (dispHeight * bmasp);

				}

				tempx = (dispWidth / 2) - (bmw / 2);

				tempy = (dispHeight / 2) - (bmh / 2);

				topLeft = new Point(tempx, tempy);

				bottomRight = new Point( bmw + tempx, bmh + tempy);

				return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);

			}

			if (displayMode == MatrixMjpegView.SIZE_FULLSCREEN){

				topLeft = new Point(0, 0);

				bottomRight = new Point( dispWidth,dispHeight);

				return new Rect(0, 0, dispWidth, dispHeight);
			}

			return null;

		}



		public void setSurfaceSize(int width, int height) {

			synchronized(mSurfaceHolder) {

				dispWidth = width;

				dispHeight = height;

			}

		}



		private Bitmap makeFpsOverlay(Paint p) {

			Rect b = new Rect();
			p.getTextBounds(fps, 0, fps.length(), b);

			// false indentation to fix forum layout            
			Bitmap bm = Bitmap.createBitmap(b.width(), b.height(), Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bm);
			p.setColor(overlayBackgroundColor);
			c.drawRect(0, 0, b.width(), b.height(), p);
			p.setColor(overlayTextColor);
			c.drawText(fps, -b.left, b.bottom-b.top-p.descent(), p);

			return bm;           

		}



		public void run() {

			start = System.currentTimeMillis();

			PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

			Bitmap bm = null;

			int width;

			int height;

			Rect destRect;

			Canvas c = null;

			Paint p = new Paint();

			while (mRun) {

				if(surfaceDone) {

					try {

						c = mSurfaceHolder.lockCanvas();

						synchronized (mSurfaceHolder) {
							
							if(mIn != null){
								bm = mIn.readMjpegFrame();

								if (bm == null) { 

									continue;
								}

								destRect = destRect(bm.getWidth(), bm.getHeight());

								c.drawColor(Color.BLUE);

								c.drawBitmap(bm, null, destRect, p);

								if(showFps) {

									p.setXfermode(mode);

									if(ovl != null) {

										// false indentation to fix forum layout                                                 

										height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom-ovl.getHeight();

										width  = ((ovlPos & 1) == 1) ? destRect.left : destRect.right -ovl.getWidth();

										c.drawBitmap(ovl, width, height, null);

									}

									p.setXfermode(null);

									frameCounter++;

									if((System.currentTimeMillis() - start) >= 1000) {

										fps = String.valueOf(frameCounter)+"fps";

										frameCounter = 0;

										start = System.currentTimeMillis();

										ovl = makeFpsOverlay(overlayPaint);

									}

								}
							}
							
						

						}

					} finally { if (c != null) mSurfaceHolder.unlockCanvasAndPost(c); }

				}

			}

		}

	}






	private void init(Context context) {
		liveConext = context;
		SurfaceHolder holder = getHolder();

		holder.addCallback(this);

		thread = new MjpegViewThread(holder, context);

		setFocusable(true);
		setClickable(true);


		// TODO Auto-generated method stub
		setOnTouchListener(new OnTouchListener() {
			float downXValue;
			float downX ;
			float downY;
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub

				if(bottomRight != null && topLeft != null){


					float upX;
					float upY;
					// Get the action that was done on this touch event
					switch (arg1.getAction() & MotionEvent.ACTION_MASK) {

					case MotionEvent.ACTION_DOWN: 
						// store the X value when the user's finger was pressed down
						downXValue = arg1.getX();
						downX = arg1.getX();
						downY = arg1.getY();


						touchMode = "DRAG";
						break;






					case MotionEvent.ACTION_UP:

						if (touchMode.equalsIgnoreCase("DRAG") && VaMjpegLiveReceiver.ptzControlOn) {

							upX = arg1.getX();
							upY = arg1.getY();

							float deltaX = downX - upX;
							float deltaY = downY - upY;

							// swipe horizontal?
							if(Math.abs(deltaX) > 100){
								// left or right
								if(deltaX < 0) {
									VaMjpegLiveReceiver.ptzControlLeft = true;
									ptzActionPerform(PtzAction.CONTINIOUS_LEFT_PAN);
									try {
										Thread.sleep((long) Math.abs(deltaX));
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										Log.d("Error", e.toString());
									}
									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
									VaMjpegLiveReceiver.ptzControlLeft = false;
								}
								if(deltaX > 0) {
									VaMjpegLiveReceiver.ptzControlRight = true;
									ptzActionPerform(PtzAction.CONTINIOUS_RIGHT_PAN);
									try {
										Thread.sleep((long) Math.abs(deltaX));
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										Log.d("Error", e.toString());
									}
									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
									VaMjpegLiveReceiver.ptzControlRight = false;
								}
							}
							else {
								Log.d("logTag", "Swipe was only " + Math.abs(deltaX) + " long, need at least " + 100);
								//  return false; // We don't consume the event
							}

							// swipe vertical?
							if(Math.abs(deltaY) > 100){
								// top or down
								if(deltaY < 0) { 
									VaMjpegLiveReceiver.ptzControlUp = true;
									ptzActionPerform(PtzAction.CONTINIOUS_UP_TILT);
									try {
										Thread.sleep((long) Math.abs(deltaY));
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										Log.d("Error", e.toString());
									}
									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
									VaMjpegLiveReceiver.ptzControlUp = false;
								}
								if(deltaY > 0) { 
									VaMjpegLiveReceiver.ptzControlDown = true;
									ptzActionPerform(PtzAction.CONTINIOUS_DOWN_TILT);
									try {
										Thread.sleep((long) Math.abs(deltaY));
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										Log.d("Error", e.toString());
									}
									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
									VaMjpegLiveReceiver.ptzControlDown = false;
								}
							}
							else {
								Log.d("logTag", "Swipe was only " + Math.abs(deltaX) + " long, need at least " + 100);
								// return false; // We don't consume the event
							}
						}

						float currentX = arg1.getX();
						break;

					case MotionEvent.ACTION_POINTER_DOWN:
						//Toast.makeText(liveConext,"MotionEvent.ACTION_POINTER_DOWN", Toast.LENGTH_LONG).show();
						oldDist = spacing(arg1);
						Log.d("TAG", "oldDist=" + oldDist);
						if (oldDist > 10f) {
							touchMode = "ZOOM";
							Log.d("TAG", "touchMode=ZOOM" );
						}
						break;

					case MotionEvent.ACTION_MOVE:
						//Toast.makeText(liveConext,"MotionEvent.ACTION_MOVE", Toast.LENGTH_LONG).show();

						if (touchMode.equalsIgnoreCase("ZOOM") && VaMjpegLiveReceiver.ptzControlOn) {
							
							try {
								float newDist = spacing(arg1);
								Log.d("TAG", "newDist=" + newDist);
								if (newDist > 10f) {
									if(newDist > oldDist){

										ptzActionPerform(PtzAction.CONTINIOUS_ZOOM_IN);
										try {
											Thread.sleep((long) Math.abs(newDist - oldDist));
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											Log.d("Error", e.toString());
										}
										ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
//										Toast.makeText(liveConext,"Zoom In", Toast.LENGTH_LONG).show();


									}else{

//										AxisPtzDriver.getInstance().rzoomMove(ipAddress,username,password,-1000);
										ptzActionPerform(PtzAction.CONTINIOUS_ZOOM_OUT);
										try {
											Thread.sleep((long) Math.abs(oldDist - newDist));
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											Log.d("Error", e.toString());
										}
										ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);

									}
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
							
						}else if(touchMode.equalsIgnoreCase("DRAG")){

						}
						break;


					}
				}
				

				return false;
			}


		});



		


		overlayPaint = new Paint();

		overlayPaint.setTextAlign(Paint.Align.LEFT);

		overlayPaint.setTextSize(12);

		overlayPaint.setTypeface(Typeface.DEFAULT);

		overlayTextColor = Color.WHITE;

		overlayBackgroundColor = Color.BLACK;

		ovlPos = MatrixMjpegView.POSITION_LOWER_RIGHT;

		displayMode = MatrixMjpegView.SIZE_STANDARD;

		dispWidth = getWidth();

		dispHeight = getHeight();

	}

	public  void ptzActionPerform(final short ptzAction) {
		
		
		locationHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				VReturn vReturn = (VReturn)msg.obj;
				if (vReturn.getReturnValue() != VReturn.SUCCESS) { 
					Toast.makeText(liveConext, VReturnMessage.returnMessage.get(vReturn.getReturnValue()), Toast.LENGTH_SHORT).show();
				} 
			}
		};
		
		
		
		new Thread(new Runnable() {			
			@Override
			public void run() {
				
				if(vSmartMobileInterface == null ){
					vSmartMobileInterface =  new VSmartMobileInterfaceImpl(vmsIpString); 
				}
				
				VReturn vReturn = vSmartMobileInterface.doPtzControl(ClientLoginSession.SESSION_ID, ptzAction, channelLive);
				locationHandler.sendMessage(Message.obtain(locationHandler, 0, vReturn));
			}
		}).start();
		
	}


	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	public void startPlayback() {

		if(mIn != null) {

			mRun = true;
			Log.d("SS", "Start");
			thread.start();   

		}

	}

	public  void resumePlayback() {
		synchronized (thread) {
			this.pauseFlag = false;
		}
		if(mIn != null) {

			Log.d("resumePlayback", "Start");
		}else{
			Log.d("resumePlayback", "mIn null");
		}


	}


	public void pausePlayback() {

		if(!this.pauseFlag) { 
			this.pauseFlag = true;
		}


	}

	public void stopPlayback() {

		
		mRun = false;
		mIn = null;
		boolean retry = true;

		while(retry) {

			try {
				thread.join();
				retry = false;

			} catch (Exception e) {
				Log.d("Error stopPlayback", e.toString());
			}

		}
		

	}



	public MatrixMjpegView(Context context, AttributeSet attrs) {

		super(context, attrs); init(context);

	}



	public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
		thread.setSurfaceSize(w, h);

	}



	public void surfaceDestroyed(SurfaceHolder holder) {

		surfaceDone = false;

		stopPlayback();

	}



	public MatrixMjpegView(Context context) { super(context); init(context); }    

	public void surfaceCreated(SurfaceHolder holder) {surfaceDone = true; }

	public void showFps(boolean b) { showFps = b; }

//	public void setSource(VaMjpegLiveReceiver source) {
//		if(this.channelLive!= null){
//			VaMjpegLiveReceiver.ptzControlOn = false;
//			if(channelLive.getIsPTZ() == VChannel.PTZ){
//				VaMjpegLiveReceiver.isPtzCam = true;
//			}else{
//				VaMjpegLiveReceiver.isPtzCam = false;
//
//			}
//		}
//		mIn = source;
//		startPlayback();
//
//	}
	
	public void setSource(VaMjpegLiveReceiver source) {
		if(this.channelLive!= null){
			
			if(channelLive.getIsPTZ() == VChannel.PTZ){
				VaMjpegLiveReceiver.ptzControlOn = true;
			}else{
				VaMjpegLiveReceiver.ptzControlOn = false;

			}
		}
		mIn = source;
		startPlayback();

	}


	public void setOverlayPaint(Paint p) { overlayPaint = p; }

	public void setOverlayTextColor(int c) { overlayTextColor = c; }

	public void setOverlayBackgroundColor(int c) { overlayBackgroundColor = c; }

	public void setOverlayPosition(int p) { ovlPos = p; }

	public void setDisplayMode(int s) { displayMode = s; }



	public void setLiveChannel(VChannel chl) {
		// TODO Auto-generated method stub
		this.channelLive = chl;
	}

	public void setServerIp(String serverid) {
		// TODO Auto-generated method stub
		this.vmsIpString = serverid;
	}


	public void setChannelList(ArrayList<Channel> channelList) {
		// TODO Auto-generated method stub
		this.channelList = channelList;
	}

	public void kk(VaMjpegLiveReceiver liveMpegReciver) {
		// TODO Auto-generated method stub
		mIn = liveMpegReciver;
	}

	public void setMobileInterface(VSmartMobileInterface _vSmartMobileInterface)
	{
		vSmartMobileInterface = _vSmartMobileInterface;
	}



}

