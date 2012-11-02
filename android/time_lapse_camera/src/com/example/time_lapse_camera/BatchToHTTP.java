package com.example.time_lapse_camera;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.AsyncTask;
import android.util.Log;

public class BatchToHTTP extends AsyncTask< URI, Void, Long> {
	private URI HTTP_HOST;
	private String TAG = "BatchToHTTP";
	
	
	
	@Override
	protected Long doInBackground(URI... pictureData) {
		
		
		//if(  == WifiManager.WIFI_STATE_ENABLED)
			try{
				HTTP_HOST = new URI("http://whitmansky.mhzmaster.com/upload");
				//HTTP_HOST = new URI("http://192.168.1.100:3000/upload");
			} catch(URISyntaxException e){
				Log.d(TAG, e.getMessage());
			}
			final HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 120000);
			httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 
			        120000);
			httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 120000);
			httpParams.setLongParameter(ConnManagerPNames.TIMEOUT, 120000);
			HttpClient httpClient = new DefaultHttpClient();
			Log.i(TAG, "HTTP client initialized");
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(HTTP_HOST);
			
			//Log.i(TAG, "about to start HTTP");
			try{
				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				
				for( int i = 0; i < pictureData.length; i++ ){
					
					//date object is key
					URI fileURI = pictureData[i];
					//Log.d(TAG,"image file set");
					File imageFile = new File(fileURI);
					entity.addPart("image"+i,
							new FileBody(imageFile ));
					
				}
			httpPost.setEntity(entity);
			Log.i(TAG, "about to execute HTTP request");
			HttpResponse response = httpClient.execute(httpPost, localContext);
			Log.d(TAG, response.getStatusLine().toString());
				
							
			} catch (Exception e) {
				Log.w(TAG, e.getMessage());
				return Long.valueOf(0);
			} finally{
				
			}
			//deleteFiles(pictureData);
			//Log.d(TAG,"deleted files");
			return Long.valueOf(1);
	}

	private void deleteFiles(URI[] pictures){
		for( URI p : pictures) {
			File pictureFile = new File( p);
			pictureFile.delete();
		}
	}
	
	
}
