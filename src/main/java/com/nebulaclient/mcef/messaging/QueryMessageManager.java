package com.nebulaclient.mcef.messaging;

import java.util.ArrayList;
import java.util.Objects;

public class QueryMessageManager {

    private static ArrayList<QueryMessage> messages;

    public static void registerMessage(QueryMessage message) {
        for (QueryMessage m : messages) {
            if (Objects.equals(m.queryId, message.queryId)) {
                System.err.println("[JCEF/Query] You have already registered an message with this queryId. Please change it and try again!");
                return;
            }
        }
        messages.add(message);
    }


    public static QueryMessage getMessage(String queryId) {
        for (QueryMessage m : messages) {
            if (Objects.equals(m.queryId, queryId)) {
                return m;
            }
        }
        return null;
    }

    public static ArrayList<QueryMessage> getMessages() {
        return messages;
    }
}
