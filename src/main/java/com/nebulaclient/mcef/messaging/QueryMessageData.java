package com.nebulaclient.mcef.messaging;

public class QueryMessageData {

    private final String payload;

    public QueryMessageData(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }


}
