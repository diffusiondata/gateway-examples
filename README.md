This file contains the documentation for the Diffusion Gateway Framework. To view the readme for a specific example, please go to the specific example's readme file.

# Gateway Framework

## TL;DR Writing a Gateway application
Implementing a Gateway application using the framework involves writing Java classes that implement required interfaces in the framework API. The main application class should implement the `GatewayApplication` interface and use defined methods to provide supported service types (and optionally, endpoint types). For each service type, a service handler class must be implemented and instances of this class passed back to the framework when requested. For full details of how to implement a Gateway application see the [javadoc](https://download.pushtechnology.com/docs/gateway-framework/latest/).

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
            <version>0.11.0</version>
        </dependency>

#### Using gradle
1. Add the Push Technology public repository to your build.gradle file

        repositories {
          maven {
            url "https://download.pushtechnology.com/maven/"
          }
        }

2. Declare the following dependency in your build.gradle file

        compile 'com.pushtechnology.gateway:gateway-framework:0.11.0'

#### Gateway framework artifacts
Get the bundled Gateway Framework jar and schema definition for configuration [here](https://download.pushtechnology.com/gateway-framework/0.11.0/gateway-framework-0.11.0-bundle.zip). This also contains an in-depth user guide about Gateway framework.

More details about the gateway framework can be found [here](https://download.pushtechnology.com/gateway-framework/0.11.0/user-guide/FrameworkUserGuide.html)