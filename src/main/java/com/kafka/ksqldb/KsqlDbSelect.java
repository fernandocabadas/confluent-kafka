package com.kafka.ksqldb;

import io.confluent.ksql.api.client.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class KsqlDbSelect {
    public static String KSQLDB_SERVER_HOST = "localhost";
    public static int KSQLDB_SERVER_HOST_PORT = 8088;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ClientOptions options = ClientOptions.create()
                .setHost(KSQLDB_SERVER_HOST)
                .setPort(KSQLDB_SERVER_HOST_PORT);
        Client client = Client.create(options);

        String query = "SELECT * FROM petsStreamClient EMIT CHANGES;";

        Map<String, Object> properties = Collections.singletonMap("auto.offset.reset", "earliest");

        StreamedQueryResult streamedQueryResult = client.streamQuery(query, properties).get();

        for (int i = 0; i < 10; i++) {
            Row row = streamedQueryResult.poll();
            if (row != null) {
                System.out.println("Fila: " + row.values());
            } else {
                System.out.println("Query finalizada.");
            }
        }

        client.close();
    }

}
