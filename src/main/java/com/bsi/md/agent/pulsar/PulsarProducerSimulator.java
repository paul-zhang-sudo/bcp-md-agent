/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.bsi.md.agent.pulsar;

import com.bsi.framework.core.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.TypedMessageBuilder;

import java.nio.charset.StandardCharsets;

@Slf4j
public class PulsarProducerSimulator {

    private final String topic;

    private final PulsarClientSimulator pulsarClientSimulator;

    private static final Integer MAX_PENDING_MSG = 1000;

    private Producer<byte[]> producer;

    public PulsarProducerSimulator(String topic, PulsarClientSimulator pulsarClientSimulator) {
        this.topic = topic;
        this.pulsarClientSimulator = pulsarClientSimulator;
        try {
            PulsarClient pulsarClient = pulsarClientSimulator.getPulsarClient();
            producer = pulsarClient.newProducer().topic(topic).maxPendingMessages(MAX_PENDING_MSG).autoUpdatePartitions(true).create();
        } catch (Exception e) {
            log.error("producer msg failed. e : {}", ExceptionUtils.getFullStackTrace(e));
        }
    }

    public String produce(String msg, String key) {
        try {
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            TypedMessageBuilder<byte[]> messageBuilder = producer.newMessage().value(bytes).key(key);
            MessageId send = messageBuilder.send();
            return String.format("send msg success. msgId: %s, topic: %s", send.toString(), topic);
        } catch (Exception e) {
            log.warn("produce msg failed. e : {}", ExceptionUtils.getFullStackTrace(e));
        }
        return String.format("send msg failed. topic: %s", topic);
    }

    public String close() {
        try {
            producer.close();
        } catch (Exception e) {
            log.error("close producer failed. e : {}", ExceptionUtils.getFullStackTrace(e));
        }

        try {
            this.pulsarClientSimulator.close();
        } catch (Exception e) {
            log.error("close pulsar client failed. e: {}", ExceptionUtils.getFullStackTrace(e));
        }
        return "success";
    }
}
