package com.nebulaclient.mcef.messaging.channel;

import ca.weblite.objc.Message;
import com.nebulaclient.mcef.messaging.message.MessageReceiver;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MessageChannel  {

    private UUID id;
    private URI uri;
    private ArrayList<MessageReceiver> receivers;

    public MessageChannel(URI uri) {
        this.id = UUID.randomUUID();
        this.uri = uri;
    }

    public void listen(String eventId, MessageReceiver receiver) {
        this.receivers.add(receiver);
    }



}

