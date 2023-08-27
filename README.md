# JAMBO Outbox-Polling (WIP)

A Source Kafka Connector that implements Polling Events Pattern which is responsible to pull messages into the bus and
help to implement Transactional Ouxtbox Pattern which lets us disaggregate events production of events sending.

![jambo.jpeg](doc%2Fjambo.jpeg)

This repository contains the source code of connector, docker files to deploy and to test and design
documents.

# JAMBO Architecture

Ensure transactional messaging is an important characteristic in Microservice Architecture. This solution uses a
different mechanism to publish messages which uses a database table as message queue and a connector to send it.

As we can see in the diagram, the producer (microservice 1) creates an event and is persisted in the outbox table
ensuring a transactional operation, if the operation crashed, generated events would never be sent. Jambo Connect is
doing polling on the table, when the transaction is closed, Jambo will get new rows and its will be pushed new events
into Kafka and the consumer (microservice 2) will receive the message.

Atomicity is guaranteed, you will not need to do rollback of event when the producer operation will be cancelled but
moreover if bus is down, producer operation will not be broken and the transaction will be completed. When the bus will
be up, the connector will send events and your system will be aligned.

![Architecture.png](doc%2FArchitecture.png)

# Building Jambo Connector

The following software is required to build and run it locally:

* Git
* JDK 17 or later
* Docker
* Apache Maven

## Firsts Step

Download source code:

```
   $ git clone https://github.com/jmsanchezmartagon/outbox-polling.git
```

Build:

```
   $ cd
   $ mvn clean package
```

Run:

```
   $ docker-compose up
```

### Commons Errors

* Connect unable to connect because oracle container is unhealthy when the connector is on. The first time oracle
  container create the database and this process is so slow and the connector is on before oracle is healthy, next time
  you will not have problems. The docker-compose file has dependency_on and healthy but is not working with Docker
  20.10.21.

## How works?

### Initialization

Firstly, the connector checks configuration parameters and after that prepares task configuration, so configuration
properties is divided by task number with one restriction: One outbox table only can be polled by one task but one task
can be polled one or more tables. The number of task must be accord with the nodes number of Kafka Connect cluster.
Kafka connect uses the same concept of topic partition to store all information of the task process events, Jambo uses
table name as key for partition offset.

### Fist Run

When connector start at the first time offset will not exist, get the first 5000 (will be configurable for next
version), the Id of the last processed row will be set as offset. Each row has two header, one of them is partition key
and the other is event id.

### Next Runs

The most important difference between first run and next runs it that connector after first time, recovers offset
configuration and get rows with Id > offset by this table.

### Limits

* Nowadays, the connector only works with an only jdbc url.

### Configuration properties

#### Connect Properties

| Properties            | Descriptions                                         |
|-----------------------|------------------------------------------------------|
| name                  | Connector Name                                       |
| connector.class       | Connector Class                                      |
| tasks.max             | Task number, must be accord with Kafka Connect nodes |
| offsets.storage.topic | Topic where offsets are stored                       |

#### Jambo Properties

| Properties        | Descriptions                           |
|-------------------|----------------------------------------|
| poll.interval.ms  | Time in milliseconds between one polls |
| datasource.url    | URL connection                         |
| outbox.table.list | Outbox tables comma separated list     |

# More Info

## About Outbox and Polling Pattern

You can get more information in www.microservices.io or in the book of Chris Richardson - Microservices Pattern (
Manning) which is one of the best book about Microservices that you can read.

## Outbox with Transaction Log Tailing

Another approach more sophisticated solution is based in read database log transaction (in Oracle: redo and archive
logs) to publish events. There are an amazing project called Debezium based on this is idea. You can get more
information https://debezium.io/.

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
  
