package com.example.time_lapse_camera;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.AsyncTask;
import android.util.Log;

public class BatchToHTTP extends AsyncTask< URI, Void, Long> {
	private URI HTTP_HOST;
	private String TAG = "BatchToHTTP";
	
	@Override
	protected Long doInBackground(URI... pictureData) {
		try{
			HTTP_HOST = new URI("http://192.168.1.100:3000/upload");
		} catch(URISyntaxException e){
			Log.d(TAG, e.getMessage());
		}
		
		Log.d(TAG, "doing stuff in the background!");
		
		
		HttpClient httpClient = new DefaultHttpClient();
		Log.i(TAG, "client initialized");
		HttpContext localContext = new BasicHttpContext();
		Log.i(TAG, "context initialized");
		HttpPost httpPost = new HttpPost(HTTP_HOST);
		
		Log.i(TAG, "about to start HTTP");
		try{
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			for( int i = 0; i < pictureData.length; i++ ){
				
				//date object is key
				URI fileURI = pictureData[i];
				Log.i(TAG,"image file set");
				File imageFile = new File(fileURI);
				entity.addPart("image"+i,
						new FileBody(imageFile ));
				Log.i(TAG,"all entities created");
				
			}
		httpPost.setEntity(entity);
		Log.i(TAG, "about to execute request");
		HttpResponse response = httpClient.execute(httpPost, localContext);
		Log.d(TAG, response.getStatusLine().toString());
			
						
		} catch (Exception e) {
			e.printStackTrace();
			return Long.valueOf(0);
		} finally{
			
		}
		return Long.valueOf(1);
	}

}
