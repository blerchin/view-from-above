require 'sinatra'

image_list = Array.new


get '/latest_img' do
	"<img src='../#{image_list.last[:filename]}' />"
end

post '/upload' do
	puts "rec'd"
	
	params.each{ |i|

		#if regex image.*
		img_data = i[1][:tempfile].read
		my_filename = "data/"+i[1][:filename]

		my_capture_time = Time.at( /.*_(\d*)\.jpg/.match(my_filename)[1].to_i )

		img_file = File.new("public/"+my_filename, "w")
		
		image_list << {:filename => my_filename, :time => my_capture_time }
		
		#puts "saved #{i[1][:filename]}"
		img_file.write( img_data)
		img_file.close
	}

end
