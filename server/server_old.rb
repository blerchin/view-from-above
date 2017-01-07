require 'rubygems'
require 'sinatra'
require 'json'
require 'sqlite3'
require 'base64'

#Dir.chdir("../whitmansky.mhzmaster.com/")
HOME_DIR = Dir.pwd
STOR_PATH = Dir.pwd+'/public/data/'
IMAGE_DATA = HOME_DIR+'/image_loc.db'

db = SQLite3::Database.new( IMAGE_DATA)

db.execute "CREATE TABLE IF NOT EXISTS Images(Id INTEGER PRIMARY KEY, Time TEXT, JPGPath TEXT, B64Path TEXT);" 
db.close



class Time 
	def iso8601ms 
		self.strftime('%Y-%m-%dT%H:%M:%S.') + (self.to_f%1*10000).to_i.to_s + self.strftime('%z')
	end
	def getMillis
	  self.to_f
	end
end

 def image_data_to_sqlite( images )
    sql = String.new
    images.each do |i|
      sql << "INSERT INTO Images (Time, JPGPath, B64Path) VALUES ('#{i[:time]}', '#{i[:jpg_path]}', '#{i[:b64_path]}'); "
    end
    
    begin
    db = SQLite3::Database.new(IMAGE_DATA)
    db.execute_batch( sql ) 
    rescue SQLite3::Exception => e
      puts sql
      puts e
    ensure
        db.close if db
    end
  end

class ImageDataRetriever
	def initialize(db_file = IMAGE_DATA)
		begin
			@db = SQLite3::Database.new(db_file)
		rescue SQLite3::Exception => e
			puts e
		end
	end
	
	def close
		@db.close
	end

	def available(start, limit)
		if(limit.nil?) then
			limit = 10000
		end
		if (start.nil?) then
			sql = "SELECT Time FROM Images LIMIT #{limit} ORDER BY Time;"
		else
			sql = "SELECT Time FROM Images WHERE Time > '#{start}' LIMIT #{limit} ORDER BY Time'"
		end
		begin
			times = @db.execute(sql)
		rescue SQLite3::Exception => e
			puts e
		end
	
	end

	def skeleton(resolution = 600 )
		begin
			frequency = @db.execute("SELECT Id FROM Images;").length / resolution
		rescue SQLite3::Exception => e
			puts e
		end
		
		begin
			sql = "SELECT Time,JPGPath FROM Images WHERE Id % #{frequency} = 0 ORDER BY Time;"
			@db.execute(sql)
		rescue SQLite3::Exception => e
			puts e
		end
		
	end
	
	def from(start,limit = 100)
		limit_time = Time.parse(start) + limit
		sql = "SELECT Time,B64Path FROM Images WHERE Time BETWEEN '#{start}' AND '#{limit_time.iso8601}' ORDER BY Time;"
		#puts sql
		begin
			@db.execute(sql)
		rescue SQLite3::Exception => e
			puts e
		end
	end
	
	def at(time)
		sql = "SELECT JPGPath FROM Images WHERE Time == '#{time}';"
		begin
			@db.get_first_value(sql)
		rescue SQLite3::Exception => e
			puts e
		end
	end
	
	##unused??
	def after(start)
		begin
			sql = "SELECT Time FROM Images WHERE Time > '#{start} ORDER BY Time'"
			puts sql
			@db.execute(sql)
		rescue SQLite3::Exception => e
			puts e
		end
	end

	
	
end


class TimeLapse < Sinatra::Base 
	set :public_folder, Proc.new { File.join(root, "public") }
  dr = ImageDataRetriever.new 

  
  get '/' do
    erb :live
  end
  
  get '/information' do
    erb :info
  end
  get '/root' do
	HOME_DIR
  
  end
  
  get '/latest_img' do
  	if( image_loc.length > 0 ) then
  		return "<img src='data/#{image_loc.last["filename"]}' />"
  	else 
  		return "no images uploaded yet. Check back later"
  	end
  end
  
  get '/available' do
  		available = []
	  	dr.available(params[:after], params[:limit]).map{|i| i[0] }.to_json  	
  end
  
  get '/skeleton' do
	dr.skeleton.map{ |i| { :time => i[0],
							:path => i[1] }}.to_json
  
  end
  get '/from?*' do
    
    limit = params[:limit].nil? ? 100 : params[:limit].to_f
  	filenames = dr.from(params[:start],limit).map{ |i| { :time => i[0],
  														:b64_path => i[1] }}
  	images = Array.new
  	filenames.each{ |f|
  		d = ""
  		File.open(STOR_PATH+f[:b64_path]){|g| d << g.read }
  		images << { :time => f[:time], :data => d } 
  	}
  	#puts 'b64 encoded'
  	#{:filenames=>filenames,:images=>images}.to_json
  	images.to_json
  end
  
  get '/at/*' do
  	time = /(.*?).jpg/.match(params[:splat].to_s)[1]
  	begin
  		puts dr.at(time)
	  	jpg_data = STOR_PATH + dr.at( time)
	  	puts jpg_data.to_s
	rescue SQLite3::Exception => e
		puts e
		jpg_data = nil
	end
  	unless jpg_data.nil? then
  		 send_file jpg_data, :type => :jpg
  	end
  end
  
  
  post '/upload' do
  	puts "recieved a batch of images"
  	image_loc = Array.new
  	params.each do |i|
  
  	# test if param label is imageDD or similar.
  	# We are unlikely to find this pattern in bogus or unwanted data so this is safe-ish
  		if /image\d*/.match(i[0]) then 
  			img_data = i[1][:tempfile].read
  			jpg_filename = i[1][:filename]
  		#Java's Unix time is in ms, while ruby accepts ms as decimal
  			my_timestamp = Time.at( (/.*_(\d*)\.jpg/.match(jpg_filename)[1].to_f ) /1000 )
  			b64_filename = "B64_"+(my_timestamp.to_f * 1000).to_s+".txt"
  			my_capture_time = my_timestamp.strftime("%Y-%m-%d_%H-%M")
  			
  			unless File.directory?( STOR_PATH+my_capture_time ) then
  				Dir.chdir(STOR_PATH)
  				Dir.mkdir(my_capture_time)
  			end
  			Dir.chdir( STOR_PATH + my_capture_time )
  			
  			img_file = File.new(jpg_filename, "w")
  			img_file.write( img_data)
  			img_file.close
  			b64_file = File.new( b64_filename, "w" )
  			b64_file << Base64.encode64(img_data)
  			b64_file.close
  			
  			Dir.chdir( HOME_DIR)
  			  			
  			#add file path to hash & save to disk json
  			image_loc <<  	{:time => my_timestamp.iso8601ms,
  							 :jpg_path => my_capture_time+"/"+jpg_filename,
  							 :b64_path => my_capture_time+"/"+b64_filename}
  			

  			#array_json_to_disk( image_loc, STOR_PATH, image_datafile_name )
  		end
  		
  	end
  	image_data_to_sqlite(image_loc.sort{|a,b| Date.parse(a[:time]) <=> Date.parse(b[:time]) } )
  end
end