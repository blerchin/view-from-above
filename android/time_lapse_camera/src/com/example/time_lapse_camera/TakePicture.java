package com.example.time_lapse_camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class TakePicture extends Activity{
	
	private static final String TAG = "TakePicture";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
	private Context context = this;
	
	private TimeLapsePictureTaker mBoundPictureTaker;
	private Boolean mPTBound;
	
	private static Camera mCamera;
    private CameraPreview mPreview;
    PowerManager pm;
    WakeLock wl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        doBindPTService();
        
        /// Pretty much everything here has been moved to the TimeLapsePictureTaker service.       
 /* 
        //pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        //WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        mCamera = getCameraInstance();
        CameraPreview cp = new CameraPreview(context,mCamera);
        
        //Gnarly hack thanks to http://stackoverflow.com/questions/2386025/android-camera-without-preview
        //Allows camera to be run as a persistent service even when screen is off (!!!)
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
        
        // deprecated setting, but required on Android versions prior to 3.0
        
        
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
        */

        //setContentView(R.layout.activity_take_picture);

        // Create an instance of Camera
        
        //cp.setZOrderOnTop(true);
/*
        try{
        	mCamera.setPreviewDisplay(cp.getHolder());
        	mCamera.startPreview();
        } catch(Exception e) {
        	Log.d(TAG,"something went wrong while starting Preview");
        }
  */      
        
        // Create our Preview view and set it as the content of our activity.
        //mPreview = new CameraPreview(this, mCamera);
        //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        //preview.addView(mPreview);
        
        /*
        Camera.Parameters camParams = mCamera.getParameters();
        
        Log.d(TAG,"previewByteSize about to get set");
        int previewPixels = camParams.getPreviewSize().width * camParams.getPreviewSize().height;
        int previewByteSize = previewPixels * android.graphics.ImageFormat.getBitsPerPixel( 
        						camParams.getPreviewFormat() ) / 8;
        Log.d(TAG,"previewByteSize = "+previewByteSize);
        byte[] previewCallbackBuffer = new byte[previewByteSize+1];
        try{
        	mCamera.addCallbackBuffer(previewCallbackBuffer);
        } catch(Exception e) {
        	Log.d(TAG,"Callback buffer is fucked");
        }
      */
        	
        	
        
        
        // Add a listener to the Capture button
        /*
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    mCamera.takePicture(null, null, mPicture);
                }
            }
        );
         */
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_take_picture, menu);
        return true;
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	doUnbindPTService();
    	
    }
    
    
   
    private ServiceConnection mPTConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundPictureTaker = ((TimeLapsePictureTaker.LocalBinder)service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(context, R.string.picture_taker_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundPictureTaker = null;
            Toast.makeText(context, R.string.picture_taker_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };
    
    void doBindPTService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
    	Log.d(TAG,"doBindPTService called");
        bindService(new Intent(context, 
                TimeLapsePictureTaker.class), mPTConnection, Context.BIND_AUTO_CREATE);
        mPTBound = true;
        Log.d(TAG,"PT Service Bound");
    }
    
    void doUnbindPTService() {
        if (mPTBound) {
            // Detach our existing connection.
            unbindService(mPTConnection);
            mPTBound = false;
        }
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


    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "TimeLapseCamera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	saveJpegToDisk( data, camera);
            
        }
    };

    public void onPause(){
    	super.onPause();
    	//wl.release();	
    
    }
    
    void saveJpegToDisk( byte[] jpegData, Camera camera) {
    	File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions: " );
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(jpegData);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }


}