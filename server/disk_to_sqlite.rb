require 'rubygems'
require 'sqlite3'
require 'base64'

HOME = Dir.pwd
data_dir = '/public/data'
sqlite_db = '/image_loc.db'

class Time 
	def iso8601ms 
		self.strftime('%Y-%m-%dT%H:%M:%S.') + (self.to_f%1*10000).to_i.to_s + self.strftime('%z')
	end
	def getMillis
	  self.to_f
	end
end

unless File.exist?('db') then
	Dir.mkdir('db')
end

IMAGE_DATA = HOME+"/db/image_loc#{Time.new.to_i}.db"

db = SQLite3::Database.new( IMAGE_DATA)
db.execute "CREATE TABLE IF NOT EXISTS Images(Id INTEGER PRIMARY KEY, Time TEXT, JPGPath TEXT, B64Path TEXT);" 


Dir.chdir(HOME+data_dir)
Dir.glob( File.join('**','*.jpg') ).each do |f|
	fdate = /(.*)\/IMG_(\d*?).jpg/.match(f)
	udate = (/.*IMG_(\d*?).jpg/.match(f)[1].to_f) / 1000.0
	b64_fname = "#{fdate[1]}/B64_#{fdate[2]}.txt"
	
	jpg_file = File.new(f,"r")
	jpg_data = jpg_file.read
	jpg_file.close
	
	b64_file = File.new( b64_fname, "w" )
  	b64_file << Base64.encode64(jpg_data)
  	b64_file.close	
  	
  	puts udate
	sql = "INSERT INTO Images (Time, JPGPath, B64Path) VALUES ('#{Time.at(udate.to_f).iso8601ms}', '#{f}', '#{b64_fname}');"
	#puts sql
	db.execute sql
	end

db.close




#img_dirs.each{ |d|

#	d.entries.each{ |i|
#		udate = /(\d*?).jpg/.match(i)
#		unless udate.nil? then
#			puts udate[1]
#		end
#	}

#}
