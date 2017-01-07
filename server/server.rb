require 'rubygems'
require 'sinatra'
require 'json'
require 'sqlite3'
require 'base64'

#Dir.chdir("../whitmansky.mhzmaster.com/")
HOME_DIR = Dir.pwd
STOR_PATH = Dir.pwd+'/public/data/'
Dir.chdir('db')

IMAGE_DATA = HOME_DIR+'/db/'+Dir.glob('*.db').last
puts IMAGE_DATA
Dir.chdir(HOME_DIR)

db = SQLite3::Database.new( IMAGE_DATA)

db.execute "CREATE TABLE IF NOT EXISTS Images(Id INTEGER PRIMARY KEY, Time TEXT, JPGPath TEXT, B64Path TEXT);" 
db.close



class Time 
	#add iso8601 method w/ ms because no built-in support in Ruby 1.8.7 =(
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
		sql = "SELECT Time,B64Path FROM Images WHERE Time > '#{start}' ORDER BY Time LIMIT #{limit.to_i};"
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
  
  get '/documentation' do
  	erb :documentation
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
  
end
