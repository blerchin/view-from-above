$(document).ready(function() {
	var dp = 100;
	var p;
	var t;
	var container = document.getElementsByClassName('viewer')[0];
	var controls = document.getElementsByClassName('viewer-controls')[0];
	var currentTime;
	var TIME_DISPLAY_WIDTH = 65;
	var w = $(container).width();
	var size = {"width": (w > 480) ? w : 480};
	size.width = size.width <= 1024 ? size.width : 1024;
	size["height"] = size.width * (3/4)
	size["left"] = $('.container').offset().left;
	$(container).width(size.width).height(size.height);
	$(controls).css({'position':'absolute','top':(size.height-20),'left':0});
	var sliderDragged = false;

	$.ajaxSetup({
		url: "/from",
		type: "GET",
		context: document.body,
		timeout: 10000 
	});
	//$('.container').prepend('<span style="color: #dd1133; font-size: 14px" class="notice">We are very sorry for the inconvenience, but a technical issue is currently preventing us from showing images taken after about noon today. The good news is those images are still being stored. We just can\'t show them to you at the moment. We\'ve got our best people working on it. In the meantime, feel free to browse the archived images from earlier today</div>');
	var times = []
	var skeleton = []
	
	var paper = Raphael(controls, size.width+20, 70);
	var slider = paper.path( "M10.5,4.5 L28.5,4.5 L28.5,23.385 L19.503,35.01 L10.5,23.428z")
			.attr("fill", "#FFF").attr("stroke","#666").attr("stroke-width","2").transform("T0,30");

	var timeDisplay = paper.set();
	timeDisplay.push( paper.rect(-65,-10,130,20).attr({'stroke-width':0, 'fill':'#fff', 'opacity':'.8'}) );
	timeDisplay.push( paper.text(0,0,"").attr("fill","#000").attr("font-size","14px") );



			
	var canvas = container.getElementsByTagName('svg')[0];
	var drawn_images = [];
	var timeline_times = [];
	var timeline = new paper.set();

	function getParameter(paramName) {
	  var searchString = window.location.search.substring(1),
		  i, val, params = searchString.split("&");
	
	  for (i=0;i<params.length;i++) {
		val = params[i].split("=");
		if (val[0] == paramName) {
		  return unescape(val[1]);
		}
	  }
	  return null;
	}
	
	t = getParameter('time');
	console.log(t);
	
	function selectLatestFrame() {
		p = times.length-1;
		t = times[p];
	
	}

	function getSkeleton(){
		$.ajax({
			url:"/skeleton"
			}).done( function(d) {
				skeleton = $.parseJSON(d);
				drawTimeline(skeleton);
				if( t == null) {
					var recent = skeleton.slice(skeleton.length/2, (skeleton.length/2)+1 )[0].time
				} else {
					var recent = t;
				}
				ImageRetriever.seek(recent);
				moveSlider(closestTimelineTime(recent).x, "abs");
			})
	}
	getSkeleton();

	slider.drag(
			 function(dx,dy,x,y,e){
			 	var oX = x - size.left;
			 	e.preventDefault();

				if( oX > 0 && oX < size.width){
					moveSlider(oX,"abs");
					currentTime = timeline_times[timeIndexFromX(oX, size.width, timeline_times)].time;
					timeDisplay.attr("text",toNiceTimeStamp( new Date(Date.parse(currentTime)) ));
					}
			},
			 function(x,y,e){
				sliderDragged = true;
	
			},
			function(e){

				sliderDragged = false;
				//p = timeIndexFromX(oX, size.width, timeline_times);
				//console.log(currentTime);

				ImageRetriever.seek( currentTime );
			});
	 var live = $('<img width="'+size.width+'" height="'+size.height+'" class="live img" />').css('border-width','0').appendTo(container);
	 var preloader = $('<img class="preload" src = "img/preload.gif" width="64" height="64" />')
											.css({"position":"absolute",
												"top":(size.height/2)-32,
												"left":(size.width/2)-32 }).appendTo(container);
	// Drawing functions only here, please!!
		window.setInterval(function() {
				
				//console.log("time is"+t);
					var i = null;
					if( i = ImageRetriever.getNext()) {
//						if(p+20 < times.length) ImageRetriever.getImage(times[p+10]);
						//drawn_images.push( paper.image("data:image/jpeg;base64,"+ImageRetriever.getImage(t).data,0,0,size.width,size.height) );
						live.attr('src',"data:image/jpeg;base64,"+i.data);
						timeDisplay.attr("text",toNiceTimeStamp( new Date(Date.parse(i.time)) ));
						preloader.hide();
					}else{
						preloader.show();
					
					}
					ImageRetriever.trim();
					//timeline.toFront();
					//slider.toFront();
	

				
				}, 300);	


incrementFrame = function(n) {
	if (p < times.length-1){
		p+=n;
		t = times[p];
		//console.log(p);
		}
}
	
drawTimeline = function(times) {	
	timeline.clear();
	timeline_times = [];

	for( i = 0; i < 100; i++ ) {
			var pos = i * parseInt(times.length/100);
			//var time = new Date(times[pos]);
			timeline_times[i] = { "time": times[pos].time, "major": i%10==0 ? true : false };
		}
	//console.log(timeline_times);
		timeline.clear();	
		timeline.push( paper.rect(0,0,size.width,20)
				.attr("fill","#fff").attr("opacity","1")
				.attr("stroke-width","0"));
	timeline_times.forEach( function(i) {
		i["x"]= timeline_times.indexOf(i)*(size.width/timeline_times.length);
		

		var time = new Date(Date.parse(i.time));
		timeline.push( rect = paper.rect(i.x, 0, i.major ? 2 : 1, i.major ? 10 : 7,0) );
		if( i.major ) {
			timeline.push( label = paper.text(i.x,15,amPM(time,false)+":"+niceMins(time)+amPM(time,true)));
		}
		rect.attr("stroke-width","0");
		rect.attr("fill","#999");
	});
	timeline.forEach(function(i) {
		i.transform("t0,40");
		});
}

moveSlider = function(x,type) {
	//console.log(x,type);
	slider.toFront();
	var slider_x = x - slider.getBBox().width;
	var timer_x = slider_x;
	var timer_w = TIME_DISPLAY_WIDTH;
	//console.log(timer_w);
	//don't let time display off screen
	if( size.width - timer_x < timer_w ){
		timer_x = size.width - timer_w;
	} else if (timer_x < timer_w ){
		timer_x = timer_w;
	}
	
	if( type == "abs"){
		slider.transform("T"+slider_x+",15" );
		timeDisplay.transform("T"+(timer_x)+",10" );
	} else {
		slider.transform("t"+slider_x+",15" );
		timeDisplay.transform("t"+(timer_x+20)+",10" );
	}
	
	//console.log(timer_x);
}	

// it is often quicker or more meaningful to find the closest tick 
// than to find the exact time at slider location
closestTimelineTime = function(time) {
	var diff = null;
	closestTime = null;
	timeline_times.forEach( function(i) {
		d = Math.abs(Date.parse(i.time) - Date.parse(time) );
		if( diff == null || d < diff ) {
			closestTime = i;
			diff = d;
		}
	});
	return closestTime;
}
xFromTime = function(time, width, arr) {
	var index = null;
	var x = null;
	for( t in arr) {
		if ( t.match(time) ) {
			index = arr.getIndexOf(t);
		}
	}
	if(index != null ) {
		x = (index / arr.length) * width
	}
	return x;
}
timeFromX = function (x, width, arr) {
	var index = parseInt((x/width) * arr.length);
	return arr[index];
}
timeIndexFromX = function (x, width, arr) {
	var index = parseInt((x/width) * arr.length);
	return index;
}
	
var requestCount =0;	
//Iterable array wrapper with prefetching and garbage collecting
var ImageRetriever = {
	imageData: [],
	i: null,
	getting: false,
	requestSize: 30,
	bufferSize: 100,
	prefetchThreshold: 15,
	
	retriever: function() {
		var that = this;
	//awesome closure is awesome.
		this.retrieve = function(start,limit,prefetch) {
		if(!that.getting ) {
			that.getting = true;
			$.ajax({
					data: {"start": start,"limit":limit},
					context: document.body
				}).done(function(d) {
					var insertPos = that.imageData.length;
					//ImageRetriever.imageData = {};
					//console.log($.parseJSON(d) );
					//that.imageData = that.imageData.concat( $.parseJSON(d) );
					var json = $.parseJSON(d);
					if( json.length > 1 ) {
						that.imageData.push.apply( that.imageData, json );
						}
					//console.log('received',that.imageData);
					that.getting = false;
					if( typeof prefetch == 'undefined' || !prefetch) {
						that.i = insertPos;
					} 
					
				}).error(function(d) {
					that.getting = false;
				});
			}
		}
	},
	seeker: function() {
		var that = this;
		this.seek = function(time) { 
			that.imageData = [];
			that.retrieve( time, that.requestSize, false);
	
		}
	},
	nextGetter: function(){
		var that = this;
		this.getNext = function(){
			var result = null;
			var next = that.imageData[that.i+1];
			
			if(typeof next != 'undefined') {
				that.i++
				result = next;
				t = next.time;
				//if we are 15 away from end of buffer, prefetch from end of current buffer
				if(typeof that.imageData[that.i+15] == 'undefined' && that.imageData.length>0) {
					that.retrieve( that.imageData.slice(-1)[0].time, that.bufferSize, true)
				}
				if(!sliderDragged) {
					moveSlider(closestTimelineTime(next.time).x, "abs");
				}
			} else if (typeof next === 'undefined' && that.imageData.length > 0) {
				//console.log(that.imageData.slice(-1)[0]);
				that.seek( that.imageData.slice(-1)[0].time);



				result = null;
			} else {
				result = null;
			}

			return result;
		}
		
	},
	trimmer: function(){
		var that = this;
		this.trim = function(){
			if(that.imageData.length > that.bufferSize){
			//that.imageData.splice(0, that.imageData.length - that.bufferSize);
			}
		}
	},
	init: function(){
		ImageRetriever.retriever();
		ImageRetriever.seeker();
		ImageRetriever.nextGetter();
		ImageRetriever.trimmer();

	}
}
ImageRetriever.init();

function toShareLink( time) {
	var url = "http://"+document.domain+"/?"+jQuery.param( {"time": time} );
	console.log(url);
	return url;
}
function toImgLink( time) {
	var url = "http://"+document.domain+"/at/"+escape(time)+".jpg";
	console.log(url);
	return url;
}
 FB.init({appId: "369272939827053", status: true, cookie: false});

      function postToFbFeed() {

        // calling the API ...
        var obj = {
          method: 'feed',
          link: toShareLink(t),
          picture: toImgLink(t),
          name: 'Whitman Sky',
          caption: toNiceTimeStamp( new Date(Date.parse(t)) ),
          description: 'Whitman Sky: Know thyself.'
        };

        function callback(response) {
          document.getElementById('msg').innerHTML = "Post ID: " + response['post_id'];
        }

        FB.ui(obj);
      }
      
      function tweet() {
		  msg = 'Check out the Whitman Sky Cam: '+toShareLink(t);      	  
		  params = jQuery.param({url: null, text:msg, hashtags:'#whitmansky'});
		  url = "https://twitter.com/share/?"+params;
		  var newwindow = window.open(url,'Tweet a Whitman Sky Image','height=250,width=500');
		  if(window.focus) {newwindow.focus()}
      	  return false;
      }
/*
var share = $('<div class="share">share this image:<br/></div>').appendTo('.viewer')
$('<a href="#" class="fb button"></a>').appendTo(share).click(function(){
										 postToFbFeed() ;
										 });
$('<a href="#" class="tw button"></a>').appendTo(share).click(function(){
										 tweet() 
										});
$('<a href="#" class="link button"></a>').appendTo(share).click(function(){
										 	alert('Share link: '+toShareLink(t)); 
										});
$('<a href="#" class="pic button"></a>').appendTo(share).click(function(){
										 	var newwindow = window.open(toImgLink(t),'View Current Image','width=640,height=480');
										 	if(window.focus) {newwindow.focus()}
										});
										*/
function include(arr,obj) {
    return (arr.indexOf(obj) != -1);
}

});
