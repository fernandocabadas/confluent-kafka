package com.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class StreamsJoin {

    public static void main(String[] args) {

        final Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-join");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> left = builder.stream("join-input-topic-left");
        KStream<String, String> right = builder.stream("join-input-topic-right");

        //Inner join.
        KStream<String, String> innerJoined = left.join(
                right,
                (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue,
                JoinWindows.of(Duration.ofMinutes(5)));
        innerJoined.to("inner-join-output-topic");

        //Left join.
        KStream<String, String> leftJoined = left.leftJoin(
                right,
                (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue,
                JoinWindows.of(Duration.ofMinutes(5)));
        leftJoined.to("left-join-output-topic");

        //Full outer join.
        KStream<String, String> outerJoined = left.outerJoin(
                right,
                (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue,
                JoinWindows.of(Duration.ofMinutes(5)));
        outerJoined.to("outer-join-output-topic");

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);

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