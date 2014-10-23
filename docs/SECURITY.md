SECURITY CONCEPT IMPLEMENTATION OVERVIEW
========================================
# Motivation
Ability to support both secure and non-secure connection types on single kafka broker instance simultaneously and  
provide simple approach for implementing new connection types without affecting existing clients.
  
# Notion of channel
Channel is a high-level abstraction of connection type which could be simply represented as follows:  
  - port number (simply integer which represents the port to connect to).  
  - channel type (defines supported connection type and aware of the way how to initialize it).  

# Supported channel types
So far we support SSL and plaintext channels, but it easy to implement new one.

# New config entries
secure.ssl.enable=(true|false) - defines whether SSL is enabled or disabled  
Once it's enabled the configuration of SSL from  file (server|client).ssl.properties is loaded to channel factory:  
//Keystore file  
keystore.type=jks  
keystore=config/client.keystore  
keystorePwd=test1234  
keyPwd=test1234  
  
//Truststore file
truststore=config/client.keystore  
truststorePwd=test1234
  
Server config has one extra field "port", which is 9093 by default.

# Should we alter the broker model in order to contain one more port for SSL?
Since we don't want to affect existing clients the broker model should be as is.

# Which port should be advertised in Zookeeper?
You are still free to decide which port to advertise in server config, but by default it is as follows:  
 1) plaintext port (if it is enabled)  
 2) SSL port (if it is enabled)  

# How should clients know about available channels then?
The information about channels is populated in Zookeeper at path "/brokers/channels/{brokerId}/{channelName}" on broker startup.  
Thus, we are still backwards compatible.

# Ok, how should producers know about channels?
The new request type was added MetadataForChannelRequest, which is the same MetadataRequest but has one extra field "channelType".  
Thus, clients are able to tell broker about preferred connection type and clients that have no clue about security still can do it in old-fashion way.