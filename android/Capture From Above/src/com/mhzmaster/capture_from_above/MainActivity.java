package com.mhzmaster.capture_from_above;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class MainActivity extends Activity {
	//Camera mCamera;
	//Context mainActivityContext = this;
	//BitmapDrawable reviewImage;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        
    }
    
    public void captureImage(){
    	CameraPreview mPreview = new CameraPreview();
    	
    }
    
/*
    
    
    
    
    private boolean safeCameraOpen() {
        boolean qOpened = false;
      
        try {
        	mCamera = Camera.open();
        	
            mCamera.getParameters();
            
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;    
    }
    
    public void captureImage(View view) throws IOException{
    	boolean cameraOn = safeCameraOpen();
    	if(cameraOn){
    		System.out.println("camera is on");
    		try{
	    		SurfaceHolder dummyHolder = new SurfaceView(mainActivityContext).getHolder();
	    		dummyHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    		
	    		mCamera.setPreviewDisplay(dummyHolder);
	    		mCamera.startPreview();
	    		
		    	mCamera.takePicture(new Camera.ShutterCallback() {
					
					@Override
					public void onShutter() {
						// TODO Auto-generated method stub
						System.out.println("shutter tripped");
					}
				}, null, null, new Camera.PictureCallback() {
					
					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						System.out.println("picture taken");
						// TODO Auto-generated method stub
						new BitmapFactory();
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				    	BitmapDrawable reviewDrawable = new BitmapDrawable(bitmap);
				    	reviewImage =  reviewDrawable;
				    	
					}
				} );
    		} finally {
    			mCamera.release();	
    		}
    	}else {
    			System.out.println("something went wrong");
    		}
    	
	    	
    }
    
    
    public void createReviewMode() {
    	LinearLayout mLinearLayout = new LinearLayout(this);
    	
    	ImageView i = new ImageView(this);
    	i.setImageDrawable(reviewImage);
    	i.setAdjustViewBounds(true);
    	i.setLayoutParams( new Gallery.LayoutParams( Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
    	
    	mLinearLayout.addView(i);
    	setContentView(mLinearLayout);	
    
    }
    */
   

}
