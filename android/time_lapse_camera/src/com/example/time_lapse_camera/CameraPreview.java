package com.example.time_lapse_camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.text.format.DateFormat;
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
    
    private int picturesBatched;
    private URI[] picturesToBatch;
    private int BATCH_SIZE = 20;
      
    private UploadQueueManager uploadQueue;
    
    public int picturesTaken;
    public boolean crashFlag = false; 

    public CameraPreview(Context context, Camera camera) {
        super(context);
        
        
        
        mCamera = camera;
       
        
    }
    public void init(){
    	//Log.d(TAG, "init() called");
    	 // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
    	mHolder = getHolder();
    	mHolder.addCallback(this);
    	picturesTaken = 0;
    	picturesToBatch = new URI[BATCH_SIZE];
    	uploadQueue = new UploadQueueManager();
    	
    	picturesBatched = 0;
    	
    
    }
    
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type, Date date) throws IOException {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "TimeLapseCamera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                throw (IOException) new IOException().initCause(new Throwable( "cannot access storage device."));
            }
        }
        File currentHourDir = new File( mediaStorageDir, DateFormat.format("yyyy-MM-dd_kk", date).toString() ); 
        if( ! currentHourDir.exists() ) {
        	if (! currentHourDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                throw (IOException) new IOException().initCause(new Throwable( "cannot access storage device."));
            }
        }
        File currentMinDir = new File( mediaStorageDir, DateFormat.format("yyyy-MM-dd_kk-mm", date).toString() );
        if( ! currentMinDir.exists() ) {
        	if (! currentMinDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                throw (IOException) new IOException().initCause(new Throwable( "cannot access storage device."));
            }
        }
        
        // Create a media file name
        
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(currentMinDir.getPath() + File.separator +
            "IMG_"+ getTimeStamp(date) + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(currentMinDir.getPath() + File.separator +
            "VID_"+ getTimeStamp(date) + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    
    private static String getTimeStamp(Date date){
    	//return new SimpleDateFormat("yyyyMMdd_HHmmss").format( date );
    	return String.valueOf( new Date().getTime() );
    }
    /** Create a file Uri for saving an image or video */
    private static URI getFileURI(File outputFile ){
          return outputFile.toURI();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //Log.d(TAG,"surfaceCreated() called");
    	// The Surface has been created, now tell the camera where to draw the preview.
       
       
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
    	mCamera.release();   
        
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
    	//Log.d(TAG,"surfaceChanged called");
        //if (mHolder.getSurface() == null){
	        try{
	        	mCamera.setPreviewDisplay(mHolder);
	        	mCamera.startPreview();
	        } catch (IOException e){
	        	Log.d(TAG, "camera preview was not attached to mHolder");
	        }
       	
     	//}
        
	        setPreviewCallback();
        
        
        // stop preview before making changes
        try {
            //mCamera.stopPreview();
        } catch (Exception e){
        	
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        
    }
    
   public void setPreviewCallback(){
	   try {
           
           //We have to set the callback where the Preview is started 
           
           mCamera.setPreviewCallback( 
           		new Camera.PreviewCallback() {
           			
   					@Override
   					public void onPreviewFrame(byte[] data, Camera camera)  {
   						
   						
   						int previewFormat = camera.getParameters().getPreviewFormat();
   		    	    	if (previewFormat == android.graphics.ImageFormat.NV21) {
   		    	    		Camera.Size previewSize = camera.getParameters().getPreviewSize();
   		    	    		Rect previewRect = new Rect(0, 0, previewSize.width, previewSize.height);
   		    	    		YuvImage yuvImage = new YuvImage(data, previewFormat, previewSize.width, previewSize.height, null);
   		    	    		//Log.d(TAG,"yuvImage saved");
   		    	    		File saveFile = null;
   		    	    		Date mDate = new Date();
   		    	    		try{
   		    	    				 
	    		    	    		saveFile = getOutputMediaFile( MEDIA_TYPE_IMAGE, mDate );
	    		    	    		
	    		    	    		
   		    	    		} catch(IOException e) {
   		    	    			Log.d(TAG,"Couldn't create media file");
   		    	    			
   		    	    		} finally {
	    		    	    		OutputStream outToFile = null;
	    		    	    		try {
	    		    	    			outToFile = new BufferedOutputStream( new FileOutputStream( saveFile), 8192 );
	    		    	    			yuvImage.compressToJpeg(previewRect, 50, outToFile);
	    		    	    			yuvImage = null;
	    		    	    		
	    		    	    		} catch(FileNotFoundException e) {
	    		    	    			Log.d(TAG,"File wasn't created properly: "+e.getMessage());
	    		    	    		}finally {
	    		    	    			if(outToFile != null) {
	    		    	    				try{
	    		    	    					outToFile.close();
	    		    	    					outToFile = null;
	    		    	    					//Log.d(TAG,"Took a picture!");
	    		    	    					
	    		    	    					if( picturesBatched < BATCH_SIZE) {
	    		    	    						picturesToBatch[picturesBatched] = getFileURI(saveFile); 
	    		    	    						picturesBatched++;
	    		    	    					} else {
	    		    	    						uploadQueue.add(picturesToBatch);
	    		    	    						picturesBatched = 0;
	    		    	    					}
	    		    	    				
	    		    	    				} catch (IOException ie) {
	    		    	    					Log.d(TAG,"File did not close");
	    		    	    				} 
	    		    	    				
	    		    	    		
	    				    	    	} else {
	    				    	    		Log.d(TAG,"Preview Image did not save.");
	        		    	    			}
	    				    	    	}
	    		    	    		}
			    	    	
   		    	    		} else { 
   		    	    			Log.d(TAG, "Preview Image is in wrong format");
   		    	    		}
   		    	    	}
   				});            	 

       } catch (Exception e){
           Log.d(TAG, "Error starting camera preview: " + e.getMessage());
   }
  }
}