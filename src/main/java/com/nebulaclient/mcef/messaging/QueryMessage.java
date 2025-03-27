package com.nebulaclient.mcef.messaging;

public abstract class QueryMessage {

    public String queryId;

    public QueryMessage(String queryId) {
        this.queryId = queryId;
    }


    public abstract QueryMessageResult onMessage(QueryMessageData data);


}
