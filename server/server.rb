require 'rubygems'
require 'sinatra'
require 'json'

STOR_PATH = Dir.pwd+'/public/data/'
image_datafile_name = 'image_loc.json'

image_loc = Array.new

class Time 
	def iso8601ms 
		self.strftime('%Y-%m-%dT%H:%S.') + (self.to_f%1*1000).to_i.to_s + self.strftime('%z')
	end
end

def array_from_disk_json( source_filename )
	hash = Hash.new
	File.open( source_filename, "r") { |f|
		hash = JSON::parse(f.read)
	}
	return hash

end

def array_json_to_disk( array, dest_dir, dest_filename )
	Dir.chdir(dest_dir)
	File.open( dest_filename, "w" ) { |f|
		f.puts array.to_json
	}
	
end


if File.exists?( STOR_PATH + image_datafile_name ) then
	image_loc = array_from_disk_json( STOR_PATH + image_datafile_name )
 end

get '/latest_img' do
	if( image_loc.length > 0 ) then
		return "<img src='data/#{image_loc.last["filename"]}' />"
	else 
		return "no images uploaded yet. Check back later"
	end
end

get '/img_loc' do
	image_loc.to_json

end


post '/upload' do
	puts "recieved a batch of images"
	
	params.each do |i|

	# test if param label is imageDD or similar.
	# We are unlikely to find this pattern in bogus or unwanted data so this is safe-ish
		if /image\d*/.match(i[0]) then 
			img_data = i[1][:tempfile].read
			my_filename = i[1][:filename]
		#Unix time is in ms, while ruby accepts ms as decimal
			my_timestamp = Time.at( (/.*_(\d*)\.jpg/.match(my_filename)[1].to_i ) /1000 )
			
			my_capture_time = my_timestamp.strftime("%Y-%m-%d_%H-%M")

			unless File.directory?( STOR_PATH+my_capture_time ) then
				Dir.chdir(STOR_PATH)
				Dir.mkdir(my_capture_time)
			end
			Dir.chdir( STOR_PATH + my_capture_time )
			
			img_file = File.new(my_filename, "w")
			
			#puts "saved #{i[1][:filename]}"
			img_file.write( img_data)
			img_file.close
			#add file path to hash & save to disk json
			image_loc <<  	{"date" => my_timestamp.iso8601ms,
							 "filename" => my_capture_time+"/"+my_filename}
			#latest_time = my_timestamp.iso8601
			array_json_to_disk( image_loc, STOR_PATH, image_datafile_name )
		end
		
	end
	"public data dir is #{STOR_PATH}"
end
