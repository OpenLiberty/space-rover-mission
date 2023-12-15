// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package io.openliberty.spacerover.models;

import java.util.Objects;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

public class LeaderboardEvent {

    private static final Jsonb jsonb = JsonbBuilder.create();

    public String hostname;
    public String action; // add,remove
    public String payload;
        
    public LeaderboardEvent(String hostname, String action, String payload) {
        this.hostname = hostname;
        this.action = action;
        this.payload = payload;
    }

    public LeaderboardEvent() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaderboardEvent)) return false;
        LeaderboardEvent sl = (LeaderboardEvent) o;
        return Objects.equals(hostname, sl.hostname)
                && Objects.equals(action, sl.action)
                && Objects.equals(payload, sl.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, String.join(",", action, payload));
    }
    
    @Override
    public String toString() {
        return "LeaderboardEvent: " + jsonb.toJson(this);
    }
    
    //tag::jsonbSerializer[]
    public static class LeaderboardEventSerializer implements Serializer<Object> {
        @Override
        public byte[] serialize(String topic, Object data) {
          return jsonb.toJson(data).getBytes();
        }
    }
    //end::jsonbSerializer[]
      
    //tag::jsonbDeSerializer[]
    public static class LeaderboardEventDeserializer implements Deserializer<LeaderboardEvent> {
        @Override
        public LeaderboardEvent deserialize(String topic, byte[] data) {
            if (data == null)
                return null;
            return jsonb.fromJson(new String(data), LeaderboardEvent.class);
        }
    }
    //end::jsonbDeSerializer[]
}