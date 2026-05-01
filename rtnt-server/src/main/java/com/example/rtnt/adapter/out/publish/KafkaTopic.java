package com.example.rtnt.adapter.out.publish;

public enum KafkaTopic {
    WORLD_STATE("world-state"),
    TRADE_EVENT("trade-event");

    private final String value;

    KafkaTopic(String value) {
        this.value = value;
    }

    public String topicName() {
        return this.value;
    }
}
