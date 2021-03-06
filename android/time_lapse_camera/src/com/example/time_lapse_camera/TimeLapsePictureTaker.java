package com.example.time_lapse_camera;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

public class TimeLapsePictureTaker extends Service {
	private static String TAG = "TimeLapsePictureTaker";
	
	NotificationManager mNM;
	
	
	private CameraPreview cp;
	private WindowManager wm;
	private WifiManager wiManager;
	private WifiLock wiLock;
	private Camera mCamera;
	
	
	private Context ctx = this;

	private String INCOMING_START_ACTION = "com.mhzmaster.tlpt.START";
	private String INCOMING_STOP_ACTION = "com.mhzmaster.tlpt.STOP";
	
	private BroadcastReceiver mIntentReceiver;
	//private Handler mRestartHandler; 

	public class LocalBinder extends Binder {
        TimeLapsePictureTaker getService() {
            return TimeLapsePictureTaker.this;
        }
        	
        	
    }
	
	@Override
	public void onCreate() {
		//int startResult = onStartCommand( new Intent(INTERNAL_RESUME_ACTION), 0, 0 );
		//Log.d(TAG,"startResult: "+startResult);
		 mIntentReceiver = new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) {
			    // Handle receiver
			    String mAction = intent.getAction();
			    Log.v(TAG,"received broadcast "+mAction);
			    if(mAction.contains(INCOMING_STOP_ACTION) ) {
			    	stopSelf();
			    
			    }
			  }
			};
			
			

			
		ctx.registerReceiver(mIntentReceiver, new IntentFilter(INCOMING_STOP_ACTION) );
		wiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wiLock = wiManager.createWifiLock(WifiManager.WIFI_MODE_FULL,"LockTag");
		wiLock.acquire();
		
		//startForeground(R.string.picture_taker_started,note);
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("TimeLapsePictureTaker", "Received start id " + startId + ": " + intent);
		String intentAction = null;
		try{
			intentAction = intent.getAction();
		} catch(NullPointerException e) {
			
		}
		Log.i(TAG, "intent is:" + intentAction );
		//when restarted as STICKY, no intent sent... so don't check
		//if( intentAction.contains(INCOMING_START_ACTION) ) {
			Log.i(TAG,"intent matches");
			initializeCameraPreview();
		// We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //} 
		return START_STICKY;		
		
		//else return START_NOT_STICKY;
	}
	@Override
	public void onDestroy(){
		// remove overlaid view
		
		
		wm.removeView(cp);
		Log.d(TAG,"view Removed");
		wiLock.release();
		Log.d(TAG,"WiLock Released");
		
		ctx.unregisterReceiver(mIntentReceiver);
		
		// Tell the user we stopped.
        Toast.makeText(this, R.string.picture_taker_stopped, Toast.LENGTH_SHORT).show();
	}
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	// This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    
  public void initializeCameraPreview(){
	  Log.d(TAG,"checking camera hardware");
	  if (checkCameraHardware( this ) ){
		
		
	    try{
	    	
		    mCamera = getCameraInstance();
		    cp = new CameraPreview(ctx,mCamera);
		    Log.d(TAG,"CameraPreview Started");
	    } catch (Exception e) {
	    	e.getStackTrace();
	    }
	    
	    // Gnarly hack thanks to http://stackoverflow.com/questions/2386025/android-camera-without-preview
	    // Allows camera to be run as a persistent service even when screen is off
	    // or user is doing other things (!!!)
	    
	    wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
	    WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
	                WindowManager.LayoutParams.WRAP_CONTENT,
	                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
	                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
	                PixelFormat.TRANSLUCENT);
	   params.width = 0;
	   params.height = 0;
	   Log.d(TAG,"WindowManager created");
	   try{ 
	   wm.addView(cp, params);
	    Log.d(TAG,"View Added");
	    cp.init();
	   } catch (Exception e) {
		   Log.d(TAG,"issue while initializing CameraPreview");
	   }
	    SurfaceHolder mHolder = cp.getHolder();
	    Log.d(TAG,"got SurfaceHolder");        
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    mHolder.setFormat(PixelFormat.TRANSPARENT); 
	    
	    
	    try {
	    	/*
	    	mCamera.stopPreview();
	    	
	    	List<Camera.Size> previewSize = mCamera.getParameters().getSupportedPreviewSizes();
	    	Camera.Size maxPreviewSize = previewSize.get(previewSize.size() - 1);
	    	Log.d(TAG,"preview size set to "+maxPreviewSize.width+" x "+maxPreviewSize.height);		
	    	
	    	mCamera.getParameters().setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
	        */
	        //mCamera.setPreviewDisplay(mHolder);
	        //mCamera.startPreview();
	        
	
	    } catch (Exception e) {
	        Log.d(TAG, "Error setting camera preview: " + e.getMessage());
	    }
	    
	  }
	}
  

}
