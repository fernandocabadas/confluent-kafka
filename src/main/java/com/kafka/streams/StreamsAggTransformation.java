package com.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class StreamsAggTransformation {

    public static void main(String[] args) {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-agg-transformation");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> source = builder.stream("aggregations-input-topic");

        // Agrupar el stream de origen por la clave existente.
        KGroupedStream<String, String> groupedStream = source.groupByKey();

        // Crea una agregación que suma la longitud en caracteres del valor para todos los registros que compartan la misma clave.
        KTable<String, Integer> aggregatedTable = groupedStream.aggregate(
                () -> 0,
                (aggKey, newValue, aggValue) -> aggValue + newValue.length(),
                Materialized.with(Serdes.String(), Serdes.Integer()));
        aggregatedTable.toStream().to("aggregations-output-charactercount-topic", Produced.with(Serdes.String(), Serdes.Integer()));

        // Cuenta el número de registros para cada clave.
        KTable<String, Long> countedTable = groupedStream.count(Materialized.with(Serdes.String(), Serdes.Long()));
        countedTable.toStream().to("aggregations-output-count-topic", Produced.with(Serdes.String(), Serdes.Long()));

        // Combina los valores de todos los registros con la misma clave en una cadena separada por espacios.
        KTable<String, String> reducedTable = groupedStream.reduce((aggValue, newValue) -> aggValue + " " + newValue);
        reducedTable.toStream().to("aggregations-output-reduce-topic");

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe());
        final CountDownLatch latch = new CountDownLatch(1);

        // Attach a shutdown handler to catch control-c and terminate the application gracefully.
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }

}