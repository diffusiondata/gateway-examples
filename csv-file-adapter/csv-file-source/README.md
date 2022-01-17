# CSV File Source Adapter

## Introduction
This adapter can be used to read a CSV file and publish its contents to a Diffusion topic. It supports two types of service. 
<ol>
<li> POLLING_JSON_SOURCE  
   
   A service of this type is called by the framework in the interval set in the configuration file or at a default interval of 30 secs, if not specified in the configuration. This service type requires the following configuration to be declared in each defined service in the configuration file:  
  
    "application": 
    {
      "fileName": <fileName>,
      "diffusionTopicName": <diffusionTopicToPublishDataTo>
    }
</li>
<li>
STREAMING_JSON_SOURCE  

A service of this type is used to listen to any changes in configured CSV file. When a change is detected, the application will publish the contents of the file to the server. This service type requires the following configuration to be declared for each defined service in the configuration file:  
  
    "application": 
    {
      "fileName": <fileName>,
      "diffusionTopicName": <diffusionTopicToPublishDataTo>
    }
</li>
</ol>
