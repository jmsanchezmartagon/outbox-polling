# JAMBO Outbox-Polling

A Source Kafka Connector that implements Polling Events Pattern which is responsible to pull messages into the bus and
help to implement Transactional Ouxtbox Pattern which lets us disaggregate events production of events sending.

This repository contains the source code of connector, docker files to deploy and to test and design
documents.

![jambo.jpeg](doc%2Fjambo.jpeg)

# JAMBO Architecture

Ensure transactional messaging is an important characteristic in Microservice Architecture. This solution uses a
different mechanism to publish messages which uses a database table as message queue and a connector to send it. As we
can see in the diagram, the producer (microservice 1) creates an event and is persisted in the outbox table ensuring
transactional operation on the microservice, if the operation crashed, generated events would never be sent.

![Architecture.png](doc%2FArchitecture.png)

## How implement a Kafka Connect ?

There are two type of connectors: Sink and Source. To implement this Pattern you need a Source connector. You have to
write three classes:

- Connector class: Extends of SourceConnector, its functions is to prepare configuration and create the task.
  ![SourceConnector.png](doc%2FSourceConnector.png)

- Config class: Extends of AbstractConfig, it is helpful class and its functions is to define properties configuration
  and help to get properties as List, Int, ...
  ![ConfigDef.png](doc%2FConfigDef.png)

- Task class: Extends of TaskConnector, its functions is to recover information of the source and prepare records to
  push into the bus.
  ![SourceTask.png](doc%2FSourceTask.png)
  WIP
