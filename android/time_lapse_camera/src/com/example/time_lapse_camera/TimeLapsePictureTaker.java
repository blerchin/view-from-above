package com.example.time_lapse_camera;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Binder;
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
	private Camera mCamera;
	
	
	
	private Context ctx = this;
	
	//Unique TimeLapsePictureTakerID to start and cancel
	private int NOTIFICATION = R.string.picture_taker_started;
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */

	public class LocalBinder extends Binder {
        TimeLapsePictureTaker getService() {
            return TimeLapsePictureTaker.this;
        }
        	
        	
    }
	
	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    Notification note = new Notification(R.drawable.ic_launcher,getText(R.string.picture_taker_started), System.currentTimeMillis()); 
	    //1st arg can be anything, but the ID for the message we are sending is just as well.
	    
	
		
		initializeCameraPreview();
		
		//startForeground(R.string.picture_taker_started,note);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("TimeLapsePictureTaker", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
	}
	@Override
	public void onDestroy(){
		// remove overlaid view
		
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		
		mCamera.release();
		wm.removeView(cp);
		
		//stopForeground(true);
		
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
    
    /**
     * Show a notification while this service is running.
     */

  public void initializeCameraPreview(){
	    
	    try{
		    mCamera = getCameraInstance();
		    cp = new CameraPreview(ctx,mCamera);
	    } catch (Exception e) {
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
	            
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    mHolder.setFormat(PixelFormat.TRANSPARENT); 
	    
	    
	    try {
	    	mCamera.stopPreview();
	    	List<Camera.Size> previewSize = mCamera.getParameters().getSupportedPreviewSizes();
	    	Camera.Size maxPreviewSize = previewSize.get(previewSize.size() - 1);
	    	Log.d(TAG,"preview size set to "+maxPreviewSize.width+" x "+maxPreviewSize.height);		
	    	
	    	mCamera.getParameters().setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
	        mCamera.setPreviewDisplay(mHolder);
	        mCamera.startPreview();
	
	    } catch (Exception e) {
	        Log.d(TAG, "Error setting camera preview: " + e.getMessage());
	    }
	    
	    
	}
  

}
