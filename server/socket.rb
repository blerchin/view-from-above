require 'socket'
include Socket::Constants

loop do
  socket = Socket.new(AF_INET, SOCK_STREAM, 0)
  sockaddr = Socket.pack_sockaddr_in( 2200,'0.0.0.0' )
  socket.bind(sockaddr)
  
  socket.listen(5)
  socket.close
end