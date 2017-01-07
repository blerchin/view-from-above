package whitmanskyapplet;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PImage;
import s373.flob.ABlob;
import s373.flob.Flob;
import s373.flob.trackedBlob;

public class WhitmanSkyApplet extends PApplet {
	private LinkedList<File> imageDirs;
	private LinkedList<String> imageLocations;
	private Iterator<String> li;
	private final String IMAGE_DIR = "/opt/view-from-above/whitman_sky_applet/test_images"; 
	Flob flob;
	int speed;
	int flobOm= 1;
	int thresh = 30;
	
	private PImage pi;
	
	public void setup() {
		size(640,480);
		speed = 10;
		
		imageDirs = new LinkedList<File>();
		imageLocations = new LinkedList<String>();
		File d = new File(IMAGE_DIR);
		for ( File f : d.listFiles() ) {
			if( f.isDirectory() ){
				imageDirs.add(f);
			}
		}
		for( File dir : imageDirs ){
			for( File f : dir.listFiles()){
				if( f.getPath().endsWith(".jpg") ){
					imageLocations.add(f.getPath() );
				}
			}
			
		}
		li = imageLocations.iterator();
		for(int i=0; i<6000; i++) li.next();
		
		pi = loadImage( li.next() );
		flob = new Flob(pi,this);
		flob.setOm(flobOm);
		flob.setThresh(thresh);
 	    flob.settrackedBlobLifeTime(20);
 	    flob.setMinNumPixels(500);
		flob.setMaxNumPixels(1000);
		println(imageLocations.size()+" images in directory");
		
	}

	public void draw() {
		if ( frameCount % speed == 0 ){
			if(li.hasNext() ){
				String loc = li.next();
				println(loc);
				pi = loadImage( loc );
				//opencv.loadImage(loc);
				//opencv.read();
				ArrayList<trackedBlob> blobs = flob.track( flob.binarize(pi)) ;
				flob.setSrcImage(3);
				//ArrayList<trackedBlob> blobs = flob.tracksimple(pi);
				
				image(flob.getImage(),0,0);
				println(blobs.size());
				for( trackedBlob tb : blobs ){
						//trackedBlob tb = flob.getTrackedBlob(i);
					
					    //String txt = "id: "+tb.id+" time: "+tb.presencetime+" ";
					    float velmult = 100.0f;
					    fill(220,220,255,100);
					    rect(tb.cx,tb.cy,tb.dimx,tb.dimy);
					    fill(0,255,0,200);
					    rect(tb.cx,tb.cy, 5, 5); 
					    fill(0);
					    //line(tb.cx, tb.cy, tb.cx + tb.velx * velmult ,tb.cy + tb.vely * velmult ); 
					    //text(txt,tb.cx -tb.dimx*0.10f, tb.cy + 5f);   
				}
				
			}
		}
		
	}
}
