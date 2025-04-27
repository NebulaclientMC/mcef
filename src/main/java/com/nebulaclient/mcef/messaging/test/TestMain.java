package com.nebulaclient.mcef.messaging.test;

import com.nebulaclient.mcef.messaging.MessageProvider;
import com.nebulaclient.mcef.messaging.channel.MessageChannel;
import com.nebulaclient.mcef.messaging.message.MessageReceiver;

import java.net.URI;
import java.net.URISyntaxException;

public class TestMain {

    public static void main(String[] args) throws URISyntaxException {
        MessageProvider provider = new MessageProvider();
        MessageChannel channel = provider.openChannel(new URI("socketIO"));
        channel.listen("test", new MessageReceiver() {
            @Override
            public void onReceive() {
                System.out.println("Received message");
            }
        });
    }

}
