package com.nebulaclient.mcef.messaging;

public abstract class MessageReceiver {

    public String queryId;

    public MessageReceiver(String queryId) {
        this.queryId = queryId;
    }

    public abstract QueryMessageResult onMessage(QueryMessageData data);

}
