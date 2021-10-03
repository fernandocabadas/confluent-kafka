# Apache Kafka On The Rocks

Este repositorio contiene todo lo necesario para aprender como funciona Apache Kafka desde el inicio

Para poder relalizar todas las pruebas es necesario levantar el cluster de Kafka. Lo haremos con Docker Compose.
Encontraremos un fichero llamado docker-compose.yml en la raiz del proyecto.

    docker-compose up -d

El contenido que vais a encontrar es el siguiente

# Kafka Producer

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic testTopic --partitions 2 --replication-factor 1
Abrimos un consumidor para ver la salida de los mensajes introducidos por el productor que vamos a ejecutar a continuación

    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic testTopic --property print.key=true --from-beginning

Ejecutamos el productor

    ./gradlew runProducer

# Kafka Consumer

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic test_topic1 --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic test_topic2 --partitions 2 --replication-factor 1

Ejecutamos el consumidor

    ./gradlew runConsumer

Abrimos un productor para cada topic y publicamos datos para ver como funciona el consumer

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic test_topic1
    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic test_topic2

# Kafka Schema Registry - Producer

Creamos el topic para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic client-schema-registry-topic --partitions 2 --replication-factor 1

Para que el tópico valide el schema es necesario añadir la siguiente propiedad

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --alter --entity-type topics --entity-name client-schema-registry-topic --add-config confluent.value.schema.validation=true

Añadimos el schema en el schema registry

    curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{ "schema": "{ \"type\": \"record\", \"name\": \"Pet\", \"fields\":[ { \"name\": \"nombre\", \"type\": \"string\" },{ \"name\": \"edad\", \"type\": \"int\" },{ \"name\": \"tamano\", \"type\": \"string\" }]}" }' http://localhost:8081/subjects/client-schema-registry-topic-value/versions

Abrimos un consumer para ver los mensaje entrantes

    docker exec -it schema-registry kafka-avro-console-consumer --topic client-schema-registry-topic --bootstrap-server broker:29092

Introducimos un mensaje que cumpla con el schema

    {"id":1,"nombre":"Marley","edad":12,"tamano":"grande"}

# Kafka Schema Registry - Consumer

Abrimos un productor para el topic y publicamos datos para ver como funciona el schema para ello tenemos que acceder al contenedor del schema-registry

    docker exec -it schema-registry kafka-avro-console-producer --bootstrap-server broker:29092 --topic schema-registry-topic --property schema.registry.url=http://localhost:8081 --property value.schema='{"type":"record","name":"Pet","fields":[{"name":"id","type":"int"},{"name":"nombre","type":"string"},{"name":"edad","type":"int"},{"name":"tamano","type":"string"}]}'

Abrimos un consumer para ver los mensaje entrantes

    docker exec -it schema-registry kafka-avro-console-consumer --topic schema-registry-topic --bootstrap-server broker:29092

Insertamos el registro con el schema correspondiente
    
    {"nombre":"Marley","edad":12,"tamano":"grande"}

# Kafka Streams - Simple Stream

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic streams-input-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic streams-output-topic --partitions 2 --replication-factor 1

Ejecutamos el stream simple

    ./gradlew runStreamsSimple

Abrimos un productor para el topic streams-input-topic que es el topic de entrada del stream

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic streams-input-topic

Verificamos la salida abriendo un consumidor sobre el topic streams-output-topic
    
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic streams-output-topic --from-beginning

# Kafka Streams - Branch transformation

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic branch-transformations-input-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic branch-a-transformations-output-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic branch-b-transformations-output-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic branch-other-transformations-output-topic --partitions 2 --replication-factor 1

Ejecutamos el StreamsBranchTransformation

    ./gradlew runStreamsBranchTransformation

Abrimos un productor para el topic branch-transformations-input-topic que es el topic de entrada del stream

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic branch-transformations-input-topic --property parse.key=true --property key.separator=:

Verificamos la salida abriendo un consumidor sobre cada topic de salida

    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic branch-a-transformations-output-topic --from-beginning --property parse.key=true
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic branch-b-transformations-output-topic --from-beginning --property parse.key=true
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic branch-other-transformations-output-topic --from-beginning --property parse.key=true

Insertamos registros en el producer
    
    a:branch-a
    b:branch-b
    c:branch-others

# Kafka Streams - Filter transformation

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic filter-transformations-input-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic filter-transformations-output-topic --partitions 2 --replication-factor 1

Ejecutamos el StreamsFilterTransformation

    ./gradlew runStreamsFilterTransformation

Abrimos un productor para el topic filter-transformations-input-topic que es el topic de entrada del stream

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic filter-transformations-input-topic --property parse.key=true --property key.separator=:

Verificamos la salida abriendo un consumidor sobre cada topic de salida

    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic filter-transformations-output-topic --from-beginning --property parse.key=true

Insertamos registros en el producer
    
    1:a*****
    1:b*****
    1:a*****

# Kafka Streams - Map transformation

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic map-transformations-input-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic map-transformations-output-topic --partitions 2 --replication-factor 1

Ejecutamos el StreamsMapTransformation

    ./gradlew runStreamsMapTransformation

Abrimos un productor para el topic map-transformations-input-topic que es el topic de entrada del stream

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic map-transformations-input-topic --property parse.key=true --property key.separator=:

Verificamos la salida abriendo un consumidor sobre cada topic de salida

    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic map-transformations-output-topic --from-beginning --property parse.key=true

Insertamos registros en el producer

    1:marley
    2:cloe
    3:rita

# Kafka Streams - FlatMap transformation

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic flatmap-transformations-input-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic flatmap-transformations-output-topic --partitions 2 --replication-factor 1

Ejecutamos el StreamsFlatmapTransformation

    ./gradlew runStreamsFlatmapTransformation

Abrimos un productor para el topic flatmap-transformations-input-topic que es el topic de entrada del stream

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic flatmap-transformations-input-topic --property parse.key=true --property key.separator=:

Verificamos la salida abriendo un consumidor sobre cada topic de salida

    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic flatmap-transformations-output-topic --from-beginning --property parse.key=true
    
Insertamos registros en el producer

    1:marley
    2:cloe
    3:rita

# Kafka Streams - Aggregations - GroupBy

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic aggregations-input-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic aggregations-output-charactercount-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic aggregations-output-count-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic aggregations-output-reduce-topic --partitions 2 --replication-factor 1

Ejecutamos el StreamsFlatmapTransformation

    ./gradlew runStreamsAggTransformation

Abrimos un productor para el topic agg-transformations-input-topic que es el topic de entrada del stream

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic aggregations-input-topic --property parse.key=true --property key.separator=:

Verificamos la salida abriendo un consumidor sobre cada topic de salida

    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic aggregations-output-charactercount-topic --property print.key=true --property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic aggregations-output-count-topic --property print.key=true --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic aggregations-output-reduce-topic --property print.key=true

Insertamos valores en el producer

    1:marley
    1:cloe
    1:rita

# Kafka Streams - Joins

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic join-input-topic-left --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic join-input-topic-right --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic inner-join-output-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic left-join-output-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --zookeeper localhost:2181 --create --topic outer-join-output-topic --partitions 2 --replication-factor 1

Ejecutamos el StreamsJoin

    ./gradlew runStreamsJoin

Abrimos dos productores para los topics join-input-topic-left y join-input-topic-right que son los topics de entrada de los streams

    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic join-input-topic-left --property parse.key=true --property key.separator=:
    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic join-input-topic-right --property parse.key=true --property key.separator=:

Verificamos la salida abriendo un consumidor sobre cada topic de salida

    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic inner-join-output-topic --from-beginning --property parse.key=true
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic left-join-output-topic --from-beginning --property parse.key=true
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic outer-join-output-topic --from-beginning --property parse.key=true

Insertamos datos en el producer left

    1:marley
    3:luis

Insertamos datos en el producer right

    1:cloe
    1:rita
    2:pepe
    3:jose

# Kafka Streams - Windowing

Creamos los topics para hacer las pruebas

    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic windowing-input-topic --partitions 2 --replication-factor 1
    docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --create --topic windowing-output-topic --partitions 2 --replication-factor 1

Ejecutamos el StreamsWindowing

    ./gradlew runStreamsWindowing

Abrimos un productor para el topic windowing-input-topic que es el topic de entrada del stream
    
    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic windowing-input-topic --property parse.key=true --property key.separator=:

Verificamos la salida abriendo un consumidor sobre cada topic de salida
    
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic windowing-output-topic --from-beginning --property parse.key=true

Insertamos datos en el producer

    1:marley
    1:cloe
    2:rita
    2:niebla

Esperamos 25 seg

    1:pepe

# Kafka Ksql Client - Crear Stream

Ejecutamos el runKsqlDbCreateStream

    ./gradlew runKsqlDbCreateStream

Abrimos una nueva consola de ksql para ver el stream

    docker exec -it ksqldb-cli ksql
    
    list streams;

# Kafka Ksql Client - Borrar Stream

Ejecutamos el runKsqlDbDropStream

    ./gradlew runKsqlDbDropStream

Abrimos una nueva consola de ksql para ver el stream

    docker exec -it ksqldb-cli ksql

    list streams;

# Kafka Ksql Client - Crear tabla

Ejecutamos el runKsqlDbCreateTable

    ./gradlew runKsqlDbCreateTable

Abrimos una nueva consola de ksql para ver la tabla

    docker exec -it ksqldb-cli ksql

    show tables;

# Kafka Ksql Client - Borrar tabla

Ejecutamos el runKsqlDbDropTable

    ./gradlew runKsqlDbDropTable

Abrimos una nueva consola de ksql para ver la tabla

    docker exec -it ksqldb-cli ksql

    show tables;

# Kafka Ksql Client - Insertar datos

Ejecutamos el runKsqlDbInsert

    ./gradlew runKsqlDbInsert

Abrimos una nueva consola de ksql para ver los datos

    docker exec -it ksqldb-cli ksql

    SELECT * FROM petsStreamClient EMIT CHANGES;

# Kafka Ksql Client - Seleccionar datos

Ejecutamos el runKsqlDbSelect
    
    ./gradlew runKsqlDbSelect