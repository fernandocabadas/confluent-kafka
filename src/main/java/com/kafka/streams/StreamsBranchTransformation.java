package com.kafka.streams;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

public class StreamsBranchTransformation {

    public static void main(String[] args) {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-branch-transformation");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> input = builder.stream("branch-transformations-input-topic");

        KStream<String, String>[] branches = input.branch(
                (key, value) -> key.startsWith("a"),
                (key, value) -> key.startsWith("b"),
                (key, value) -> true);

        KStream<String, String> aKeysStream = branches[0];
        KStream<String, String> bKeysStream = branches[1];
        KStream<String, String> othersStream = branches[2];

        aKeysStream.to("branch-a-transformations-output-topic");
        bKeysStream.to("branch-b-transformations-output-topic");
        othersStream.to("branch-other-transformations-output-topic");

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        final CountDownLatch latch = new CountDownLatch(1);

        // Incluimos un controlador de cierre para detectar el control-c y terminar la aplicación controladamente.
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