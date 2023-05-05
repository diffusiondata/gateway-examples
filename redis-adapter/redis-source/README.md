# Redis Source Adapter

## Introduction
This adapter can be used to read from a Redis instance and publish its contents to a Diffusion topic. It supports two types of service.
<ol>
<li> POLLING_REDIS_SOURCE  
   
   A service of this type is called by the framework in the interval set in the configuration file or at a default interval of 30 secs, if not specified in the configuration. This service type requires the following configuration to be declared in each defined service in the configuration file:  
  
    "application": 
    {
      "redisUrl": <redisUrl>,
      "diffusionTopicName": <diffusionTopicToPublishDataTo>
    }
</li>
<li>
STREAMING_REDIS_SOURCE  

A service of this type is used to subscribe to a Redis channel. When a message is received, the application will publish the value to a Diffusion topic using the channel key as the topic path. This service type requires the following configuration to be declared for each defined service in the configuration file:  
  
    "application": 
    {
      "redisUrl": <redisUrl>,
      "diffusionTopicName": <diffusionTopicToPublishDataTo>
    }
</li>
</ol>
