# a java netty http server which takes json objects and converts them into protobuf , and insert into a kafka queue


Sample Request :

curl -H 'Content-Type: application/json' -X POST 'http://localhost:8080/' -d '{"name":"dcb", "id": "4", "salary":"12400"}'

run zookeeper : ./bin/zookeeper-server-start.sh config/zookeeper.properties
run kafka server : ./bin/kafka-server-start.sh config/server.properties
check queue : ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic employee-stuff --from-beginning
(topic name is "employee-stuff")
