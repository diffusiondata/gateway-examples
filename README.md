This file contains documentation on the Gateway Framework. To view readme for specific example, please goto specific example's readme file.

# Gateway-Framework

## Introduction
Gateway framework provides a standard approach to create pub/sub applications to connect any external data systems with Diffusion server. Under the hood it uses pub/sub APIs provided by Diffusion java client SDK to publish data to Diffusion topics and to subscribe to them. It enables application developers to focus on their business requirement to connect to external systems, fetch data from them and publish data to them, where Diffusion specific operations are handled by the framework. The framework performs most of the heavy lifting regarding managing connections to server, creating topics, publishing to topics and subscribing from the topics. Using the framework, developers can also benefit adding support for visualizing and managing their application from Diffusion Management console. The behaviour of such application could be changed dynamically via Console, if required. For example, while the application is running, its configuration could be updated during runtime to add publishing to new Diffusion topic.

## Concepts 

#### Gateway client
Applications created using Gateway framework are termed as Gateway clients. These clients are usually adapters used to connect any external datasource with Diffusion server to perform pub/sub operations.

#### Services
A service is a sub process within the application that can be specific to publishing/subscribing streams of data to/from specific data source e.g: topic (in case of Kafka), or a queue(in case of MQs), or a table(in case of databases) in external data systems. It has its own lifecycle linked to the main application, which can be paused and resumed as required. An application can be comprised of multiple services. A service can be added, updated or removed from application during runtime. Different services can be of different **service modes**. Application developers can define different service types for such modes which they want to support in an application.

##### Source service
Source service refers to the service which can be used to publish data from external sources to Diffusion topics.  
There are two modes of source service:
<ol>
<li>Polling source service:  Framework polls the developer provided implementation to poll for data to be published to Diffusion server.
</li>
<li>Streaming source service: Application will explicitly call a method to publish the data, when its available.</li>
</ol>

##### Sink service 
Sink service refers to the service mode which can be used to publish data to an external sink from Diffusion topics. When an update is available for a topic, this update is sent by the framework to application, which can then be published to external sink systems.

#### Endpoints
Endpoint is a set of configuration that can be defined to be used in single service or reused across multiple service. Usually configurations like details to connect to external source can be defined as an endpoint, if this is tobe reused across multiple services. Multiple service can refer to the defined endpoint using its name in the configuration.

#### Operations
Operations are actions which can be performed against Gateway client or its services. These operations can be executed via the Diffusion Management console. Gateway client supports 'pause', 'resume' and 'shutdown' operations. Services support 'pause' and 'resume' operations.

#### Status items
Status of the application and services are presented as status items in Diffusion Management console. The framework generates different status items related to different events such as publication of data to a Diffusion topic, subscription of a Diffusion topic, any error registered by the application or state of services. These are aggregated and presented in the Diffusion Management console. These can be observed as a mechanism to monitor status of services and application in general.

#### Payload Convertors
Payload convertors can be used to convert data from one format to another OR to transform structure of published data to Diffusion or to external sink. The framework supports two types of Payload convertors:
<ol>
<li>
InboundPayloadConvertor: This is used to convert or transform data from external source into data format or structure expected in Diffusion topic. The framework supports default converters to convert data to Diffusion supported topic types which are int64, string, binary, double and JSON. In addition, CSVToJsonConvertor and AvroToJsonConvertor is provided which can be used to support source data in CSV and Avro formats and publish them to JSON Diffusion topics.
</li>  

<li>
OutboundPayloadConvertor: This is used to convert to transform data from Diffusion into data format or structure expected to be published to Sink source. The default convertors provided in framework can be used to 
</li>
</ol>

#### Configuration
The framework expects applications to pass configuration as a file in JSON format. This file can be passed when starting the application as System property or Environment variable with key `config.file`. The whole configuration is combination of framework defined configuration and developer defined configuration for the services types and endpoint types of the application. The schema for the framework defined configuration can be found in the artifact bundle together with the framework jar. When designing the application the developers should identify the configurations required for the serviceTypes and endpointTypes they are going to support and document them appropriately, so that users can create a configuration file which will be used when starting the application.

Using the configuration file, the client can be started as following:  
    `java -jar -Dconfig.file=./configuration.json application-{version}.jar`

The clients can be started without configuration file by passing only bootstrap configuration. Bootstrap configuration includes details required to connect to the server and to register the client with server which are server URL, principal, password and Gateway client ID. Gateway framework provides an option to set these bootstrap configs as system properties or environment variable, which will be used by default, if they are not set in the configuration file. The framework will override these, if these configs are also set in configuration file. The allowed system/env properties are:  
``` 
diffusion.gateway.server.url  
diffusion.gateway.principal  
diffusion.gateway.password  
gateway.client.id
```
The client can be started by only using bootstrap config as following:  
`java -jar -Ddiffusion.gateway.server.url=ws://localhost:8080 -Ddiffusion.gateway.principal=admin -Ddiffusion.gateway.password=password  -Dgateway.client.id=application-1 application-{version}.jar`

##### Configuration persistence
The configuration passed to the application during startup or added or updated via Diffusion Management console, is persisted in Diffusion server. If a client is started with only bootstrap configuration, and configuration for the passed gateway client ID is available in the server, it will be used to instantiate the client.

##### Configuration schema
The schema of the configuration as expected by the Framework can be seen [here](file://frameworkConfigSchema.json).

Schema for the supported service types and endpoint types should be defined and documented by developers, to enable users to define a valid configuration. 

## Get started
Implementing a Gateway client using the framework, involves implementing required interfaces in the framework.
The main application should implement `GatewayApplication` interface and defined methods to register supported serviceTypes and endpointTypes. For each serviceType, a service handler can be implemented tobe passed back to the framework. The csv-file-adapter in this repo, can be used as a sample reference to develop a Gateway client for any other type of external data sources. 
 
## Gotchas
// TODO

## Server setups to use Gateway Clients for Efficiency and High throughput
// TODO