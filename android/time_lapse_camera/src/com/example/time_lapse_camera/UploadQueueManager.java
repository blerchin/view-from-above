package com.example.time_lapse_camera;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import android.util.Log;



public class UploadQueueManager {
	
	private static String TAG = "UploadQueueManager";
	private static int MAX_SIMULTANEOUS_UPLOADS = 5;
	private static int UPLOAD_STATUS_UNSUCCESSFUL = 0;
	private static int UPLOAD_STATUS_SUCCESSFUL = 1;
	private static int UPLOAD_STATUS_IN_PROGRESS = 2;
	
	
	LinkedList < AsyncTask<URI,Void,Long> > ongoingUploads;
	
	UploadQueueManager() {
		ongoingUploads = new LinkedList< AsyncTask<URI,Void,Long> >();
		
	}
	
	public void add( URI[] batch){
		if( prune() ) {
			Log.e(TAG,"sending to HTTP");
			sendBatchToHTTP( batch, ongoingUploads );
			
		}  else {
			Log.e(TAG,"too many simultaneous uploads; dropped incoming frames");
			
		}
		
	}
	
	private boolean prune() {
			Iterator<AsyncTask<URI,Void,Long>> uploadIter = ongoingUploads.iterator();
			while(uploadIter.hasNext() ) {
				AsyncTask<URI,Void,Long> uploader = uploadIter.next();
				int status = getStatusOfUploader(uploader);
				if (   status == UPLOAD_STATUS_SUCCESSFUL
					|| status == UPLOAD_STATUS_UNSUCCESSFUL) {
					
					uploadIter.remove();
				}
				
		}
		Log.d(TAG, "Uploads Running: "+ ongoingUploads.size() );
		return ongoingUploads.size() < MAX_SIMULTANEOUS_UPLOADS;

	}
	
	
	private void sendBatchToHTTP(URI[] batch, LinkedList<AsyncTask<URI,Void,Long>> list ){
		Log.i(TAG,"starting an upload batch");
		AsyncTask<URI,Void,Long> batchUpload = new BatchToHTTP().execute( batch  );
		list.add(batchUpload);
	}
	
	private int getStatusOfUploader( AsyncTask<URI,Void,Long> uploader ) {
		try{
			if (uploader != null  
				&& uploader.getStatus() == AsyncTask.Status.FINISHED 
				&& uploader.get() == Long.valueOf(1) ) {
				//Log.d(TAG,"upload successful; removing from queue");
				return UPLOAD_STATUS_SUCCESSFUL;
				
			} else if(uploader != null
					 && uploader.getStatus() == AsyncTask.Status.FINISHED
					 && uploader.get() == Long.valueOf(0) ) { 
				//Log.d(TAG,"upload unsuccessful; removing from queue");
				return UPLOAD_STATUS_UNSUCCESSFUL;
			} else {
				//Log.d(TAG, "batch still in progress; leaving in queue");
				return UPLOAD_STATUS_IN_PROGRESS;
			}
		} catch (ExecutionException e) {
			Log.d(TAG,"execution exception while getting batch operation status"+e.getMessage());
		} catch (InterruptedException e) {
			Log.d(TAG,"interruption excpetion while getting batch operation status"+e.getMessage());
		}
		return UPLOAD_STATUS_UNSUCCESSFUL;
	}
	
}
