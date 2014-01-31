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
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.videonetics.component.VChannel;



public class ArchiveMjpegView extends SurfaceView implements SurfaceHolder.Callback {
	public final static int POSITION_UPPER_LEFT  = 9;

	public final static int POSITION_UPPER_RIGHT = 3;

	public final static int POSITION_LOWER_LEFT  = 12;

	public final static int POSITION_LOWER_RIGHT = 6;



	public final static int SIZE_STANDARD   = 1;

	public final static int SIZE_BEST_FIT   = 4;

	public final static int SIZE_FULLSCREEN = 8;


	//	private static PtzController ptzController;

	private ArchiveMjpegViewThread thread;

	private VaMjpegArchiveReceiver mIn = null;    

	private boolean showFps = true;

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

	public Context liveConext = null;
	//	private  HashMap< Short, Vas> channelVasTab = null;

	private  String touchMode = "";
	private float oldDist = 0;

	//	private RemoteLiveOutputClient liveOutputClient = null;
	private boolean pauseFlag = false;


	public class ArchiveMjpegViewThread extends Thread {

		private SurfaceHolder mSurfaceHolder;

		private int frameCounter = 0;

		private long start;

		private String fps = "";

		private Bitmap ovl;
		



		public ArchiveMjpegViewThread(SurfaceHolder surfaceHolder, Context context) {

			mSurfaceHolder = surfaceHolder;

		}



		private Rect destRect(int bmw, int bmh) {

			int tempx;

			int tempy;

			if (displayMode == ArchiveMjpegView.SIZE_STANDARD) {

				tempx = (dispWidth / 2) - (bmw / 2);

				tempy = (dispHeight / 2) - (bmh / 2);

				topLeft = new Point(tempx, tempy);

				bottomRight = new Point( bmw + tempx, bmh + tempy);

				return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);

			}

			if (displayMode == ArchiveMjpegView.SIZE_BEST_FIT) {

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

			if (displayMode == ArchiveMjpegView.SIZE_FULLSCREEN){

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
			c.drawRect(0, 0, b.width() + 10, b.height() + 10, p);
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
								}else{
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

											fps = " Replay ";

											frameCounter = 0;

											start = System.currentTimeMillis();

											ovl = makeFpsOverlay(overlayPaint);

										}

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

		thread = new ArchiveMjpegViewThread(holder, context);

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
					
//					Rect archiveMode = new Rect(topLeft.x + ((bottomRight.x-topLeft.x)/2 + 80),topLeft.y,topLeft.x + ((bottomRight.x-topLeft.x)/2) + 130,topLeft.y + 50);



					float upX;
					float upY;
					String ipAddress = null;
					try { 
						//					URL url = new URL(channelLive.getAnalyticUrl());
						//					ipAddress = url.getHost();
					} catch (Exception e) {
						Log.d("Log",e.toString());
					}
					String username =channelLive.getUsername();
					String password =channelLive.getPassword();
					// Get the action that was done on this touch event
					switch (arg1.getAction() & MotionEvent.ACTION_MASK) {

					case MotionEvent.ACTION_DOWN: 
						// store the X value when the user's finger was pressed down
						downXValue = arg1.getX();
						downX = arg1.getX();
						downY = arg1.getY();


						
//						
//						if (archiveMode.contains((int)arg1.getRawX(), (int)arg1.getRawY())) { 
//							System.err.println("archive mode clicked.");
//							
//							final Calendar dateTime=Calendar.getInstance();
//							
//							OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
//								@Override
//								public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
//									
//									
//									dateTime.set(Calendar.YEAR,year);
//									dateTime.set(Calendar.MONTH, monthOfYear);
//									dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//									
//									
//									
//									OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
//										@Override
//										public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//											dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
//											dateTime.set(Calendar.MINUTE,minute);
//											
//											Bundle bundle = new Bundle();    
//											bundle.putSerializable("channel", channelLive); 
//											bundle.putSerializable("serverAddress", vmsIpString);
//											Intent intent = new Intent(liveConext, MjpegArchiveAcitivity.class); 
//											intent.putExtras(bundle);
//											liveConext.startActivity(intent);
//											
//										}
//									};
//									new TimePickerDialog(liveConext, t, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true).show();
//								}
//							};
//							new DatePickerDialog(liveConext, d, dateTime.get(Calendar.YEAR),dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH)).show();
//							
//							
//						}

					
						touchMode = "DRAG";
						break;






					case MotionEvent.ACTION_UP:

//						if (touchMode.equalsIgnoreCase("DRAG") && VaMjpegLiveReceiver.ptzControlOn) {
//
//							upX = arg1.getX();
//							upY = arg1.getY();
//
//							float deltaX = downX - upX;
//							float deltaY = downY - upY;
//
//							// swipe horizontal?
//							if(Math.abs(deltaX) > 100){
//								// left or right
//								if(deltaX < 0) {
//									ptzActionPerform(PtzAction.CONTINIOUS_LEFT_PAN);
//									try {
//										Thread.sleep(3000);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										Log.d("Error", e.toString());
//									}
//									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
//								}
//								if(deltaX > 0) {
//									ptzActionPerform(PtzAction.CONTINIOUS_RIGHT_PAN);
//									try {
//										Thread.sleep(3000);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										Log.d("Error", e.toString());
//									}
//									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
//								}
//							}
//							else {
//								Log.d("logTag", "Swipe was only " + Math.abs(deltaX) + " long, need at least " + 100);
//								//  return false; // We don't consume the event
//							}
//
//							// swipe vertical?
//							if(Math.abs(deltaY) > 100){
//								// top or down
//								if(deltaY < 0) { 
//									ptzActionPerform(PtzAction.CONTINIOUS_UP_TILT);
//									try {
//										Thread.sleep(3000);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										Log.d("Error", e.toString());
//									}
//									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
//								}
//								if(deltaY > 0) { 
//									ptzActionPerform(PtzAction.CONTINIOUS_DOWN_TILT);
//									try {
//										Thread.sleep(3000);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										Log.d("Error", e.toString());
//									}
//									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
//								}
//							}
//							else {
//								Log.d("logTag", "Swipe was only " + Math.abs(deltaX) + " long, need at least " + 100);
//								// return false; // We don't consume the event
//							}
//						}

						float currentX = arg1.getX();
						break;

					case MotionEvent.ACTION_POINTER_DOWN:
						//Toast.makeText(liveConext,"MotionEvent.ACTION_POINTER_DOWN", Toast.LENGTH_LONG).show();
						oldDist = spacing(arg1);
//						Log.d("TAG", "oldDist=" + oldDist);
//						if (oldDist > 10f) {
//							touchMode = "ZOOM";
//							Log.d("TAG", "touchMode=ZOOM" );
//						}
						break;

					case MotionEvent.ACTION_MOVE:
						//Toast.makeText(liveConext,"MotionEvent.ACTION_MOVE", Toast.LENGTH_LONG).show();

//						if (touchMode.equalsIgnoreCase("ZOOM") && VaMjpegLiveReceiver.ptzControlOn) {
//							float newDist = spacing(arg1);
//							Log.d("TAG", "newDist=" + newDist);
//							if (newDist > 10f) {
//								if(newDist > oldDist){
//
//									ptzActionPerform(PtzAction.CONTINIOUS_ZOOM_IN);
//									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
////									Toast.makeText(liveConext,"Zoom In", Toast.LENGTH_LONG).show();
//
//
//								}else{
//
////									AxisPtzDriver.getInstance().rzoomMove(ipAddress,username,password,-1000);
//									ptzActionPerform(PtzAction.CONTINIOUS_ZOOM_OUT);
//									ptzActionPerform(PtzAction.STOP_PTZ_MOVEMENT);
//
//								}
//							}
//						}else if(touchMode.equalsIgnoreCase("DRAG")){
//
//						}
						break;


					}
				}
				

				return false;
			}


		});



	

		overlayPaint = new Paint();

		overlayPaint.setTextAlign(Paint.Align.LEFT);

		overlayPaint.setTextSize(10);

		overlayPaint.setTypeface(Typeface.DEFAULT);

		overlayTextColor = Color.RED;

		overlayBackgroundColor = Color.BLACK;

		ovlPos = ArchiveMjpegView.POSITION_LOWER_RIGHT;

		displayMode = ArchiveMjpegView.SIZE_STANDARD;

		dispWidth = getWidth();

		dispHeight = getHeight();

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
			//			RemoteLiveOutputClient remoteLiveOutputClient = new RemoteLiveOutputClient(vmsIpString,channelLive.getId(),320,240);

		}

	}

	public  void resumePlayback() {
		Log.d("resumePlayback", "resumePlayback called");
		synchronized (thread) {
			this.pauseFlag = false;
			//			thread.notify();
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
				retry = false;
				thread.join();
				

			} catch (Exception e) {}

		}

	}



	public ArchiveMjpegView(Context context, AttributeSet attrs) {

		super(context, attrs); init(context);

	}



	public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {

		thread.setSurfaceSize(w, h);

	}



	public void surfaceDestroyed(SurfaceHolder holder) {

		surfaceDone = false;

		stopPlayback();

	}



	public ArchiveMjpegView(Context context) { super(context); init(context); }    

	public void surfaceCreated(SurfaceHolder holder) {surfaceDone = true; }

	public void showFps(boolean b) { showFps = b; }

	public void setSource(VaMjpegArchiveReceiver source) {
		
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

	public void kk(VaMjpegArchiveReceiver liveMpegReciver) {
		// TODO Auto-generated method stub
		mIn = liveMpegReciver;
	}





}

