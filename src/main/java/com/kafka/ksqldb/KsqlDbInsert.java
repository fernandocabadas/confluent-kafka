package com.kafka.ksqldb;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import io.confluent.ksql.api.client.KsqlObject;

import java.util.concurrent.ExecutionException;

public class KsqlDbInsert {
    public static String KSQLDB_SERVER_HOST = "localhost";
    public static int KSQLDB_SERVER_HOST_PORT = 8088;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ClientOptions options = ClientOptions.create()
                .setHost(KSQLDB_SERVER_HOST)
                .setPort(KSQLDB_SERVER_HOST_PORT);
        Client client = Client.create(options);

        KsqlObject row = new KsqlObject()
                .put("ID", 2)
                .put("NOMBRE", "Cloe")
                .put("EDAD", 7)
                .put("TAMANO", "pequeño");

        client.insertInto("PETSSTREAMCLIENT", row).get();

        client.close();
    }

}
