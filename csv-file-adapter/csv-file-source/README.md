# CSV File Source Adapter

## Introduction
This adapter can be used to read a CSV file and publish its contents into Diffusion topic. It supports two types of services. 
<ol>
<li> POLLING_JSON_SOURCE  
   
   This source service is called by the framework in the interval set in the configuration file or in default interval of 30 secs, if not specified in configuration. This service type requires following configuration to be declared in each defined service in the configuration file:  
  
    "application": 
    {
      "fileName": <fileName>,
      "diffusionTopicName": <diffusionTopicToPublishDataTo>
    }
</li>
<li>
STREAMING_JSON_SOURCE  

This source service is used to listen to any changes in configured CSV file. When a change is detected, the application will publish the contents of the file to the server. This service type also requires following configuration to be declared in each defined service in the configuration file:  
  
    "application": 
    {
      "fileName": <fileName>,
      "diffusionTopicName": <diffusionTopicToPublishDataTo>
    }
</li>
</ol>
