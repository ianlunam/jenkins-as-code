// Add read/write permissions for all on the docker socket
//  to allow jenkins to use Docker

"sudo chmod a+rw /var/run/docker.sock".execute()
