view-from-above
===============

This repo contains software written for an installation at Whitman College, which now lives at [whitmansky.com](http://whitmansky.com).

* *android* An android app which spawns a picture-taker/uploader service. While running, the app takes several pictures a second and uploads them over HTTP to the server. App runs even if phone is locked and screen is off and is quite resilient in the face of Android's aggressive battery-saving measures.

* *server* Ruby/Sinatra/js-based web app ready to run on Ruby 1.8.7/Passenger. Receives, processes, and stores HTTP image uploads (functionality currently disabled - look in 0.1_launch branch). Serves pages and image data to included js viewer.  
