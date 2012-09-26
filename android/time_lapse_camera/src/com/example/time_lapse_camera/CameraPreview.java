package com.example.time_lapse_camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	private static final String TAG = "CameraPreview";
	
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        
        
        
        mCamera = camera;
        /*
       
       
        //I think this is made unnecessary by callback in surfaceChanged
        
        mCamera.setPreviewCallback( 
        		new Camera.PreviewCallback() {
					
					@Override
					public void onPreviewFrame(byte[] data, Camera camera)  {
						Log.d(TAG,"yo it's a preview");
						int previewFormat = camera.getParameters().getPreviewFormat();
		    	    	if (previewFormat == android.graphics.ImageFormat.NV21) {
		    	    		Camera.Size previewSize = camera.getParameters().getPreviewSize();
		    	    		Rect previewRect = new Rect(0, 0, previewSize.width, previewSize.height);
		    	    		YuvImage yuvImage = new YuvImage(data, previewFormat, previewSize.width, previewSize.height, null);
		    	    		
		    	    		File saveFile = getOutputMediaFile( MEDIA_TYPE_IMAGE );
		    	    		OutputStream outToFile = null;
		    	    		try {
		    	    			outToFile = new BufferedOutputStream( new FileOutputStream( saveFile) );
		    	    			yuvImage.compressToJpeg(previewRect, 60, outToFile);
		    	    		
		    	    		} catch(FileNotFoundException e) {
		    	    			Log.d(TAG,"File wasn't created properly: "+e.getMessage());
		    	    		}finally {
		    	    			if(outToFile != null) {
		    	    				try{
		    	    					outToFile.close();
		    	    				} catch (IOException e) {
		    	    					Log.d(TAG,"File did not close");
		    	    				}
		    	    				
		    	    		
				    	    	} else {
				    	    		Log.d(TAG, "Preview Image is in wrong format");
				    	    	}
		    	    		}
		    	    	}
					}
				});
        */
        
        
    }
    public void init(){
    	Log.d(TAG, "init() called");
    	 // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
    	mHolder = getHolder();
    	mHolder.addCallback(this);
    	
    
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

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"surfaceCreated() called");
    	// The Surface has been created, now tell the camera where to draw the preview.
        
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
    	Log.d(TAG,"surfaceChanged called");
        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
           
            //We have to set the callback where the Preview is started. because... 
            /*mCamera.setPreviewCallback( 
            		new Camera.PreviewCallback() {
            			
    					@Override
    					public void onPreviewFrame(byte[] data, Camera camera)  {
    						
    						int previewFormat = camera.getParameters().getPreviewFormat();
    		    	    	if (previewFormat == android.graphics.ImageFormat.NV21) {
    		    	    		Camera.Size previewSize = camera.getParameters().getPreviewSize();
    		    	    		Rect previewRect = new Rect(0, 0, previewSize.width, previewSize.height);
    		    	    		YuvImage yuvImage = new YuvImage(data, previewFormat, previewSize.width, previewSize.height, null);
    		    	    		
    		    	    		File saveFile = getOutputMediaFile( MEDIA_TYPE_IMAGE );
    		    	    		OutputStream outToFile = null;
    		    	    		try {
    		    	    			outToFile = new BufferedOutputStream( new FileOutputStream( saveFile) );
    		    	    			yuvImage.compressToJpeg(previewRect, 60, outToFile);
    		    	    		
    		    	    		} catch(FileNotFoundException e) {
    		    	    			Log.d(TAG,"File wasn't created properly: "+e.getMessage());
    		    	    		}finally {
    		    	    			if(outToFile != null) {
    		    	    				try{
    		    	    					outToFile.close();
    		    	    				} catch (IOException e) {
    		    	    					Log.d(TAG,"File did not close");
    		    	    				}
    		    	    				
    		    	    		
    				    	    	} else {
    				    	    		Log.d(TAG, "Preview Image is in wrong format");
    				    	    	}
    		    	    		}
    		    	    	}
    					}
    				});
*/            	 

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}