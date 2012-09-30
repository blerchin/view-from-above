require 'socket'
include Socket::Constants

loop do
	server = TCPServer.new(2200)
	server.puts "Hello my friend"
	puts "The server said, '#{socket}'"
	server.close
end