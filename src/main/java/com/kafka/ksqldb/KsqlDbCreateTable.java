package com.kafka.ksqldb;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class KsqlDbCreateTable {
    public static String KSQLDB_SERVER_HOST = "localhost";
    public static int KSQLDB_SERVER_HOST_PORT = 8088;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ClientOptions options = ClientOptions.create()
                .setHost(KSQLDB_SERVER_HOST)
                .setPort(KSQLDB_SERVER_HOST_PORT);
        Client client = Client.create(options);

        String sql = "CREATE TABLE petsTableClient (id INT primary key, nombre VARCHAR, edad INT, tamano VARCHAR)"
                + "WITH (KAFKA_TOPIC='ksql-table-pet-client', VALUE_FORMAT='json', partitions=2, replicas=1);";

        Map<String, Object> properties = Collections.singletonMap("auto.offset.reset", "earliest");

        client.executeStatement(sql, properties).get();

        client.close();

    }

}
