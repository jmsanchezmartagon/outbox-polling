FROM alpine:latest
RUN apk update && apk add openjdk17 bash 
RUN cd /opt && wget https://downloads.apache.org/kafka/3.4.0/kafka_2.13-3.4.0.tgz
RUN cd /opt && tar xzf kafka_2.13-3.4.0.tgz 
WORKDIR /opt/kafka_2.13-3.4.0
RUN sed -i "s/zookeeper.connect=localhost:2181/zookeeper.connect=zookeeper:2181/g" ./config/server.properties
