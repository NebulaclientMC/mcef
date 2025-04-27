package com.nebulaclient.mcef.messaging;

import com.nebulaclient.mcef.MCEF;
import org.spongepowered.asm.util.logging.MessageRouter;

import java.util.ArrayList;
import java.util.Objects;


/***
 * @apiNote Receive messages from JavaScript. Create a new QueryMessage and register it
 * @see MessageReceiver
 */
public class QueryMessageManager {

    private static final ArrayList<MessageReceiver> messages = new ArrayList<MessageReceiver>();

    public static void listen(MessageReceiver message) {
        for (MessageReceiver m : messages) {
            if (Objects.equals(m.queryId, message.queryId)) {
                System.err.println("[JCEF/Query] You have already registered an message with this queryId. Please change it and try again!");
                return;
            }
        }
        messages.add(message);
    }


    public static void emit(SendingMessage sendingMessage) {

    }

    public static MessageReceiver getMessage(String queryId) {
        for (MessageReceiver m : messages) {
            if (Objects.equals(m.queryId, queryId)) {
                return m;
            }
        }
        return null;
    }

    public static ArrayList<MessageReceiver> getMessages() {
        return messages;
    }
}
