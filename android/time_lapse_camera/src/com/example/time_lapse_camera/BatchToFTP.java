package com.example.time_lapse_camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.os.AsyncTask;
import android.util.Log;

public class BatchToFTP extends AsyncTask<URI, Void, Long> {

	private String TAG = "BatchToFTP";
	
	private String FTP_SERVER = "mhzmaster.com";
	private String FTP_PATH = "view_from_above_images/";
	private String FTP_USER = "view_from_above";
	private String FTP_PASS = "avastcandid";
	
	protected Long doInBackground(URI ... pictureUris ) {
		Log.d(TAG,"BatchToFTP Instance!");
			
			
		FTPClient fc = new FTPClient();
		try {
			fc.connect(FTP_SERVER);
			fc.login(FTP_USER, FTP_PASS);
			Log.d(TAG,"logged in");
			fc.changeWorkingDirectory(FTP_PATH);
				if(fc.getReplyString().contains("250")) {
					Log.d(TAG,"Connection opened");
					fc.setFileType(FTP.BINARY_FILE_TYPE);
					for ( URI p : pictureUris){
						Log.d(TAG, "trying a picture URI: " + p.toString());
						
						File jpeg = new File(p);
															
						FileInputStream inStream =  new FileInputStream( new File(p) );
						fc.enterLocalPassiveMode();
						
						boolean result = fc.storeFile(jpeg.getName(), inStream);
						if (result == false) {
							Log.d(TAG,"did not store file: "+jpeg.getName());
							return Long.valueOf(0);
						}
						
						inStream.close();
						Log.d(TAG,"wrote file to FTP:"+jpeg.getName());
					}
					fc.logout();
					fc.disconnect();
				}
					
			} catch (IOException e){
				Log.e(TAG,"something went wrong while uploading the file: "+ e.getMessage());
				return Long.valueOf(0);
			} finally {
				
			}


		return Long.valueOf(1);

	}
}