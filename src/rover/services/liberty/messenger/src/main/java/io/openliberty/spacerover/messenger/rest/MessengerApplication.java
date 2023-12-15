// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package io.openliberty.spacerover.messenger.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import javax.naming.InitialContext;
import jakarta.enterprise.concurrent.ManagedExecutorService;

import io.openliberty.spacerover.models.LeaderboardEvent;
import io.openliberty.spacerover.models.LeaderboardEvent.LeaderboardEventDeserializer;

import java.util.Properties;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;

import java.io.UnsupportedEncodingException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.MongoDatabase;
import io.openliberty.spacerover.messenger.models.MessengerConstants;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.StringBuilder;

@ApplicationPath("/")
public class MessengerApplication extends Application {
    
    private static ManagedExecutorService executor;

    // @Inject
	// @ConfigProperty(name = "io.openliberty.messenger.topicName", defaultValue = "leaderboard.event")
	private static String topicName = "leaderboard.event";

    // @Inject
	// @ConfigProperty(name = "io.openliberty.messenger.bootstrapServers", defaultValue = "my-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092")
	private static String bootstrapServers = "my-cluster-kafka-bootstrap:9092";

    private static MessengerApplication messengerApplication;

    public static MessengerApplication getInstance() {
        if (messengerApplication == null) {
            messengerApplication = new MessengerApplication();
        }
        return messengerApplication;
    }

    public MessengerApplication() {
        try {
            System.out.println("Setting Executor with topic " + topicName + " and bootstrapServers " + bootstrapServers);
            if (executor == null) {
                executor = (ManagedExecutorService) new InitialContext().lookup("java:comp/DefaultManagedExecutorService");
                System.out.println("Creating MessengerResource");
                System.out.println("Starting consumer in a new thread...");
                startConsumer();
                System.out.println("-------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to set executor");
        }
    }

    public static Future<Integer> startConsumer() {
        return executor.submit(new Callable<Integer>() { 
            public Integer call() throws Exception { 
                try{
                    runConsumer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            } 
        });  
    }

    public static void runConsumer() throws Exception {
		final Consumer<String, LeaderboardEvent> consumer = createConsumer();
        final int giveUp = 100;
		int noRecordsCount = 0;
        while (true) {
            final ConsumerRecords<String, LeaderboardEvent> consumerRecords = consumer.poll(Duration.ofMillis(1000));

            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }

            consumerRecords.forEach(record -> {
                System.out.printf("Consumer Record:(%s, %s, %d, %d)\n",
                        record.key(), record.value().toString(),
                        record.partition(), record.offset());
				LeaderboardEvent leaderboardEvent = record.value();

				if (leaderboardEvent.action.equals("add")) {
					System.out.println("Updating MongoDB");
                    try {
                        String url = "http://leaderboard:9080/mongo/leaderboard/";
                        URL obj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/json");
                        con.setDoOutput(true);

                        try(OutputStream os = con.getOutputStream()) {
                            byte[] input = leaderboardEvent.payload.getBytes("utf-8");
                            os.write(input, 0, input.length);			
                        }

                        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            System.out.println(response.toString());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
				}
            });
            consumer.commitAsync();
        }
        consumer.close();
	}

    private static Consumer<String, LeaderboardEvent> createConsumer() {
		final Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "messenger-consumer-group-id");
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "messenger-consumer-group-instance");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LeaderboardEventDeserializer.class.getName());
		final Consumer<String, LeaderboardEvent> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(topicName));
		return consumer;
	}
}