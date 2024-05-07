FROM alpine:latest
RUN apk update && apk add openjdk17 bash 
RUN cd /opt && wget https://archive.apache.org/dist/kafka/3.4.1/kafka_2.12-3.4.1.tgz
RUN cd /opt && tar xzf kafka_2.12-3.4.1.tgz
RUN mkdir -p /opt/connect
WORKDIR /opt/kafka_2.12-3.4.1
RUN sed -i "s/zookeeper.connect=localhost:2181/zookeeper.connect=zookeeper:2181/g" ./config/server.properties
