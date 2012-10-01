require 'sinatra'

post '/upload' do
	http_obj = params[:image]
	
	puts params
	
	svr_temp = http_obj[:tempfile].read
	img_file = File.new(http_obj[:filename], "w")
	img_file.write( svr_temp)
	img_file.close
	

end
