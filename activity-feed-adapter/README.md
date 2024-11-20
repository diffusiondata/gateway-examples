# Activity feed Gateway adapter example

This project demonstrates the use of the Diffusion Gateway Framework.  The 
Gateway Framework provides an easy and consistent way to develop applications
that need to connect to a 'source' or 'sink' system and get data in and out
of Diffusion.

## How to build the project

    mvn clean install


## How to run the Activity feed Gateway adapter

    java -Dgateway.config.file=activity-feed-adapter/src/main/resources/configuration.json -Dgateway.config.use-local-services=true -jar .\activity-feed-adapter\target\activity-feed-adapter-1.0.0-jar-with-dependencies.jar


MORE INFORMATION TO FOLLOW......

NOTES:

        //TODO: JH - get parameters - for now, won't use a schema - but I do need to mention it.


```mermaid
flowchart LR

%% Nodes
AFS("Pretend \n Activity feed \n server"):::orange
AFG("Activity feed \n Gateway adapter"):::green
DIF("Diffusion server"):::blue


%% Edges

AFS -. 1a) Send activity event .-> AFG 
AFG -- 2a) Invoke get latest activities ---> AFS
AFS -- 2b) Return latest activities --> AFG

AFG -- 1b) Update specific \n sport activity --> DIF
AFG -- 2c) Update the \n activities snapshot ---> DIF

%% Styling
classDef green fill:#B2DFDB,stroke:#00897B,stroke-width:2px;
classDef orange fill:#FFE0B2,stroke:#FB8C00,stroke-width:2px;
classDef blue fill:#BBDEFB,stroke:#1976D2,stroke-width:2px;
```