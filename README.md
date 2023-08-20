# JAMBO Outbox-Polling

A Source Kafka Connector to help to implement Transactional Ouxtbox Pattern that lets us disaggregate application
transactions of sending messages and Polling Events is responsible to pull messages into the bus.

![jambo.jpeg](doc%2Fjambo.jpeg)

## How implement a Kafka Connect ?

There are two type of connectors: Sink and Source. To implement this Pattern you need a Source connector. You have to
write three class:

- Connector class: Extends of SourceConnector its functions is to prepare configuration and create the task.
  ![SourceConnector.png](doc%2FSourceConnector.png)

- Config class: Extends of AbstractConfig, it is helpful class and its functions is to define properties configuration
  and help to get properties as List, Int, ...
  ![ConfigDef.png](doc%2FConfigDef.png)

- Task class: Extends of TaskConnector, its functions is to recover information of the source and prepare records to
  push into the bus.
  ![SourceTask.png](doc%2FSourceTask.png)
  WIP
