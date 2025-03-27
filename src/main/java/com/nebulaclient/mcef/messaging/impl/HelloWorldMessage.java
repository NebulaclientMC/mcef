package com.nebulaclient.mcef.messaging.impl;

import com.nebulaclient.mcef.messaging.QueryMessage;
import com.nebulaclient.mcef.messaging.QueryMessageData;
import com.nebulaclient.mcef.messaging.QueryMessageResult;

public class HelloWorldMessage extends QueryMessage {

    public HelloWorldMessage() {
        super("hello-world");
    }


    @Override
    public QueryMessageResult onMessage(QueryMessageData data) {
        if(data.getPayload() == "hi"){
            return QueryMessageResult.fail("Failed sorry");
        }
        return QueryMessageResult.success("Thanks");
    }
}
