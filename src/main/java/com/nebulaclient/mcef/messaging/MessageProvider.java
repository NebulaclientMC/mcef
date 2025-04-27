package com.nebulaclient.mcef.messaging;

import com.nebulaclient.mcef.messaging.channel.MessageChannel;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URI;
import java.util.HashMap;

public class MessageProvider {

    public HashMap<MessageChannel, Socket> channels = new HashMap<>();

    public MessageChannel openChannel(URI uri){
        MessageChannel messageChannel = new MessageChannel(uri);

        Socket socket = IO.socket(messageChannel.uri);
        channels.put(messageChannel, socket);
        return messageChannel;
    }


}
