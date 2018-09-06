multithreaded client-server dictionary

To run the dictionary, first try to launch the server though the following cmd:
java -jar Server.jar -p (enter any port number ) -f Dictionary.xml (or you can build other files)

then try to launch to client though the following:
java -jar Client.jar -h (hostname) -p (the port number matching the server's port)
