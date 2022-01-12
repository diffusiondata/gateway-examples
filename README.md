This file contains the documentation for the Diffusion Gateway Framework. To view the readme for a specific example, please go to the specific example's readme file.

# Gateway Framework

## Introduction
The Gateway framework provides a standard approach for creating pub/sub applications to connect any external data systems to a Diffusion server. Under the hood it uses pub/sub APIs provided by the Diffusion Java client SDK to publish data to Diffusion topics and to subscribe to them. It enables application developers to focus on their business requirement to connect to external systems, fetch data from them and publish data to them, where Diffusion specific operations are handled by the framework. The framework performs most of the heavy lifting regarding managing connections to a server, creating topics, publishing to topics and subscribing to the topics. Using the framework, developers can also benefit from adding support for visualizing and managing their application from the Diffusion Management console. The behaviour of such an application can be changed dynamically via the Console, if required. For example, while the application is running, its configuration can be updated at runtime to add publishing to a new Diffusion topic.

## Concepts 

### Gateway application
Applications created using the Gateway framework are known as "Gateway applications". These applications are usually adapters used to connect an external data source to a Diffusion server to perform pub/sub operations.

### Services
A **service** is a sub process within the application that can be specific to publishing/subscribing streams of data to/from a specific data source e.g. a topic (in the case of Kafka), or a queue (in the case of MQ), or a table (in the case of databases) in external data systems. It has its own lifecycle linked to the main application, which can be paused and resumed as required. A service can be added, updated or removed from the application at runtime.

### Service Types
An application defines the **service types** it supports. The user of the application can then define services of any supported type. An application can be composed of multiple service types and a user of the application can define any number of services of a supported type.  Different service types can be of different **service modes** and each service type is of one of the modes described below.

##### Source service
A source service is a service which can be used to publish data from external sources to Diffusion topics.  
There are two modes of source service:
<ol>
<li>**Polling source service** : The framework polls the developer provided implementation to poll for data to be published to Diffusion server.
</li>
<li>**Streaming source service** : The application will explicitly call a method to publish the data, when it's available.</li>
</ol>

##### Sink service 
A sink service refers to the service mode which can be used to publish data to an external sink from Diffusion topics. When an update is available for a topic, this update is sent by the framework to the application, which can then be published to external sink systems.

#### Endpoints
An endpoint is a set of configuration that can be defined to be used within a single service or reused across multiple services. Details to connect to an external data source can be defined as an endpoint, if this is to be reused across multiple services. Multiple services can refer to the defined endpoint using its name in the configuration. The use of endpoints by an application is optional, but may be useful when multiple services have the same requirements for connecting to a back end system. If an application does support endpoints then it will define specific **endpoint types** that it supports.

#### Operations
Operations are actions which can be performed against a Gateway application or its services. These operations can be executed via the Diffusion Management console. Gateway applications support 'pause', 'resume' and 'shutdown' operations. Services support 'pause' and 'resume' operations.

#### Status items
The status of the application and its services are presented as status items in the Diffusion Management console. The framework generates different status items relating to different events, such as publication of data to a Diffusion topic, subscription to a Diffusion topic, any error registered by the application, or the state of services. These are aggregated and presented in the Diffusion Management console. These can be observed as a mechanism to monitor the status of services and the application in general.

#### Payload Convertors
Payload convertors can be used to convert data from one format to another OR to transform the structure of data published to Diffusion or to an external sink. The framework supports two types of Payload convertor:
<ol>
<li>
`InboundPayloadConvertor`: This is used to convert or transform data from an external source into the data format or structure expected in a Diffusion topic.
</li>  

<li>
`OutboundPayloadConvertor`: This is used to convert to transform data from Diffusion format into the data format or structure expected to be published to a data sink. 
</li>
</ol>
In addition, some out of the box payload convertors are provided for common formats such as Avro and CSV. For full details about payload convertors and the default and issued convertors see that package Javadoc for the `com.pushtechnology.gateway.framework.convertors` package.

#### Configuration
The framework expects applications to pass configuration as a file in JSON format. The location of this file can be passed when starting the application as a System property or Environment variable with key `config.file`. The whole configuration is a combination of framework defined configuration and application developer defined configuration for the service types and endpoint types of the application. The schema for the framework defined configuration can be found in the artifact bundle together with the framework jar. When designing the application the developers should identify the configurations required for the service types and endpoint types they are going to support and document them appropriately, so that users can create a configuration file which will be used when starting the application.

Using the configuration file, the client can be started as following:  
    `java -jar -Dconfig.file=./configuration.json application-{version}.jar`

The clients can be started without a configuration file by passing only bootstrap configuration. Bootstrap configuration includes details required to connect to the Diffusion server and to register the client with the server. These are server URL, principal, password and Gateway client ID. The Gateway framework provides an option to set these bootstrap details as system properties or environment variables, which will be used by default, if they are not set in the configuration file. The framework will override these, if these details are also set in the configuration file. The allowed system/env properties are:  
``` 
diffusion.gateway.server.url  
diffusion.gateway.principal  
diffusion.gateway.password  
gateway.client.id
```
The client can be started using only bootstrap config as follows:  
`java -jar -Ddiffusion.gateway.server.url=ws://localhost:8080 -Ddiffusion.gateway.principal=admin -Ddiffusion.gateway.password=password  -Dgateway.client.id=application-1 application-{version}.jar`

##### Configuration persistence
The configuration passed to the application during startup, or added or updated via the Diffusion Management console, is persisted in the Diffusion server. If a client is started with only bootstrap configuration, and configuration for the passed gateway client ID is available in the server, it will be used to instantiate the client.

##### Configuration schema
The schema of the configuration as expected by the Framework can be seen [here](file://frameworkConfigSchema.json).

The schemas for the supported service types and endpoint types should be defined and documented by application developers, to enable users to define a valid configuration. 

## Writing a Gateway application
Implementing a Gateway client application using the framework involves writing Java classes that implement required interfaces in the framework API. The main application class should implement the `GatewayApplication` interface and use defined methods to provide supported service types (and optionally, endpoint types). For each service type, a service handler class must be implemented and instances of this class passed back to the framework when requested. For full details of how to implement a Gateway application see the package Javadoc for the `com.pushtechnology.gateway.framework` package. 

The csv-file-adapter in this repo, can be used as a sample reference to develop a Gateway application for any other type of external data sources. 

## Gotchas
// TODO

## Server setups to use Gateway Clients for Efficiency and High throughput
// TODO