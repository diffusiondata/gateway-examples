# CSV File Sink Adapter

## Introduction
This adapter can be used to get updates from Diffusion JSON topics and write the update to a CSV file. It supports only one type of service. 

### CSV_FILE_SINK  
This sink service supports getting Diffusion JSON topic updates and publishing them to a CSV file specified in the configuration. This service will correctly function only for Diffusion topics of JSON topic type. If any other type of topic selector is used in its configuration, or the topic selector matches any non JSON topic type, when an update is received for this topic, Payload converter exception will be thrown. This service type requires the following configuration to be declared in each defined service in the configuration file:
  
    "application": {
      "filePath": "./pathToCreateTheCsvFile/"
    }

Below is an example of an overall configuration of a service of type `CSV_FILE_SINK`:
    
    {
      "serviceName": "csvSinkService",
      "serviceType": "CSV_FILE_SINK",
      "description": "Subscribes to JSON diffusion topic and writes its content to CSV file",
      "config": {
        "framework": {
          "diffusionTopicSelector": "?data//"
        },
        "application": {
          "filePath": "./somepath/csvData/"
        }
      }
    }   

With this configuration, this service will subscribe to all topics that match the selectors passed in `diffusionTopicSelectors` field. For each topic path update, a CSV file will be created with this topic's content. The name and path of the file depends on the topic path and value of `filepath` in the config. In above example, if topic path is `data/users` the complete file path will be:  
 `./somepath/csvData/data/users.csv`.  
 If the file exists already, then the content of the file will be replaced with the latest content of the update.