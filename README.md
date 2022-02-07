This file contains the documentation for the Diffusion Gateway Framework. To view the readme for a specific example, please go to the specific example's readme file.

# Gateway Framework

## TL;DR Writing a Gateway application
Implementing a Gateway application using the framework involves writing Java classes that implement required interfaces in the framework API. The main application class should implement the `GatewayApplication` interface and use defined methods to provide supported service types (and optionally, endpoint types). For each service type, a service handler class must be implemented and instances of this class passed back to the framework when requested. For full details of how to implement a Gateway application see the [javadoc](https://download.pushtechnology.com/docs/gateway-framework/0.2.0/)

The csv-file-adapter in this repo, can be used as a sample reference to develop a Gateway application for any other type of external data sources. 

### To get started
#### Using maven
1. Add the Push Technology public repository to your pom.xml file

        <repositories>
          <repository>
            <id>push-repository</id>
            <url>https://download.pushtechnology.com/maven/</url>
          </repository>
        </repositories>
        
2. Declare the following dependency in your pom.xml file

        <dependency>
            <groupId>com.pushtechnology.gateway</groupId>
            <artifactId>gateway-framework</artifactId>
            <version>0.2.0</version>
        </dependency>

#### Using gradle
1. Add the Push Technology public repository to your build.gradle file

        repositories {
          maven {
            url "https://download.pushtechnology.com/maven/"
          }
        }

2. Declare the following dependency in your build.gradle file

        compile 'com.pushtechnology.gateway:gateway-framework:0.2.0'

#### Gateway framework artifacts
Get the bundled Gateway Framework jar and schema definition for configuration [here](https://download.pushtechnology.com/gateway-framework/0.2.0/gateway-framework-0.2.0-bundle.zip)

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

Each status consists of a title, description and level. The level can be Green, Amber, or Red.

* Green means that the adapter/service is functioning as it is supposed to.
* Amber means that an issue is affecting some aspect of the operation of the service; for example, a particular message may not have been delivered, or the adapter may have been paused.
* Red means that a terminal incident has occurred that is preventing publishing or subscribing to Diffusion topics.

#### Service states
The state of a service can be 'active' or 'paused'. When a service is added successfully, the service will be in active state. If the service is successfully registered with framework, but fails to be started due to any reason, it will be in paused state. An active service can transition to 'paused' state if 'Pause' operation is executed from Diffusion console or connection to Diffusion server is broken or application sends a Red level status to the framework. Application developers can trigger a service to pause (for some terminal error scenarios, like authentication failure with external data source) by using 'StateHandler' to send a status Item with Red level which will cause the service to pause. An application user can resume a service by executing 'Resume' operation in Diffusion console. If a service is paused due to disconnection with the server, as soon as the connection re-establishes, the service will be resumed, provided that it was paused due to disconnection and not triggered via console or application error. 

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
Some out of the box payload convertors are provided for common formats such as Avro and CSV. For full details about payload convertors and the default and issued convertors see that package Javadoc for the `com.pushtechnology.gateway.framework.convertors` package.

#### Configuration
The framework expects applications to pass configuration as a file in JSON format. The location of this file can be passed when starting the application as a System property or Environment variable with key `config.file`. The whole configuration is a combination of framework defined configuration and application developer defined configuration for the service types and endpoint types of the application. The schema for the framework defined configuration can be found in the artifact bundle together with the framework jar. When designing the application the developers should identify the configurations required for the service types and endpoint types they are going to support and document them appropriately, so that users can create a configuration file which will be used when starting the application.

Using the configuration file, the application can be started as following:  
    `java -jar -Dconfig.file=./configuration.json application-{version}.jar`

The applications can be started without a configuration file by passing only bootstrap configuration. Bootstrap configuration includes details required to connect to the Diffusion server and to register the application with the server. These are server URL, principal, password and Gateway application ID. The Gateway framework provides an option to set these bootstrap details as system properties or environment variables, which will be used by default, if they are not set in the configuration file. The framework will override these, if these details are also set in the configuration file. The allowed system/env properties are:  
``` 
diffusion.gateway.server.url  
diffusion.gateway.principal  
diffusion.gateway.password  
gateway.client.id
```
The application can be started using only bootstrap config as follows:  
`java -jar -Ddiffusion.gateway.server.url=ws://localhost:8080 -Ddiffusion.gateway.principal=admin -Ddiffusion.gateway.password=password  -Dgateway.client.id=application-1 application-{version}.jar`

##### Configuration persistence
The configuration passed to the application during startup, or added or updated via the Diffusion Management console, is persisted in the Diffusion server. If an application is started with only bootstrap configuration, and configuration for the passed gateway application ID is available in the server, it will be used to instantiate the application.

##### Configuration schema
The schema of the configuration as expected by the Framework can be seen [here](file://frameworkConfigSchema.json).

The schemas for the supported service types and endpoint types should be defined and documented by application developers, to enable users to define a valid configuration. 

#### Configuration secrets
Any sensitive configuration value, like credentials can be set as a secret in the form of `$SECRET_VARIABLE` in configuration file, or when adding a service via Diffusion console. The actual value can be set as a System property or environment variable for the used secret variable name. This prevents the exposure of such sensitive information in configuration file and is also hidden in Diffusion console. 

#### Diffusion topic handling
For any source service, topics are created automatically by the framework, if they are not already present. Whether these topics are removed or persisted after the termination of service / application, depends on how the service is configured by the application developer. 

To enable developers to define how to handle the topics created during publication, the framework provides option to define [Persistence policy](https://download.pushtechnology.com/docs/gateway-framework/0.2.0/com/pushtechnology/gateway/framework/SourceHandler.SourceServiceProperties.PersistencePolicy.html)