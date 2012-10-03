require 'rubygems'
require 'sinatra'

image_list = Array.new
STOR_PATH = Dir.pwd+'/public/data/'

get '/latest_img' do
	if image_list.length > 0 then
		return "<img src='data/#{image_list.last[:filename]}' />"
	else
		return "no images yet. Check back soon"
	end
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
			
			image_list << {:filename => my_capture_time+"/"+my_filename, :time => my_capture_time }
			
			#puts "saved #{i[1][:filename]}"
			img_file.write( img_data)
			img_file.close
		end
		
	end
	"public data dir is #{STOR_PATH}"
end
