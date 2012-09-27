package com.example.time_lapse_camera;

import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

public class TimeLapsePictureTaker extends Service {
	private static String TAG = "TimeLapsePictureTaker";
	private PowerManager pm;
	private WakeLock wl;
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

        pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        
        wl.acquire();
        
        mCamera = getCameraInstance();
        cp = new CameraPreview(ctx,mCamera);
        
        //Gnarly hack thanks to http://stackoverflow.com/questions/2386025/android-camera-without-preview
        //Allows camera to be run as a persistent service even when screen is off (!!!)
        
        wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);        
       Log.d(TAG,"WindowManager created");
        wm.addView(cp, params);
        Log.d(TAG,"View Added");
        cp.init();
        SurfaceHolder mHolder = cp.getHolder();
                
        
        try {
        	List<Camera.Size> previewSize = mCamera.getParameters().getSupportedPreviewSizes();
        	Camera.Size maxPreviewSize = previewSize.get(previewSize.size() - 1);
        	Log.d(TAG,"preview size set to "+maxPreviewSize.width+" x "+maxPreviewSize.height);		
        	
        	mCamera.getParameters().setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            
            
            
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
		
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
		wm.removeView(cp);
		mCamera.stopPreview();
		wl.release();

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
    
}
