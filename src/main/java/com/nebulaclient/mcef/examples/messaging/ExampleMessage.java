package com.nebulaclient.mcef.examples.messaging;

import com.nebulaclient.mcef.messaging.QueryMessage;
import com.nebulaclient.mcef.messaging.QueryMessageData;
import com.nebulaclient.mcef.messaging.QueryMessageResult;

public class ExampleMessage extends QueryMessage {

    public ExampleMessage() {
        super("hello-world");
    }


    @Override
    public QueryMessageResult onMessage(QueryMessageData data) {
        if(data.getPayload() == "hello"){
            return QueryMessageResult.fail("Hello back!");
        }
        return QueryMessageResult.success("Why don't you say hello");
    }
}
