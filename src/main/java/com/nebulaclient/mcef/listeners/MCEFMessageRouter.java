package com.nebulaclient.mcef.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebulaclient.mcef.messaging.QueryMessage;
import com.nebulaclient.mcef.messaging.QueryMessageData;
import com.nebulaclient.mcef.messaging.QueryMessageManager;
import com.nebulaclient.mcef.messaging.QueryMessageResult;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandler;

import javax.management.Query;

public class MCEFMessageRouter implements CefMessageRouterHandler {

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(request);

            String extractedQueryId = jsonNode.has("queryId") ? jsonNode.get("queryId").asText() : null;

            if (extractedQueryId == null) {
                callback.failure(1, "queryId is missing");
                return false;
            }

            QueryMessage message = QueryMessageManager.getMessage(extractedQueryId);
            QueryMessageResult result = message.onMessage(new QueryMessageData(request));

            if (result.state == QueryMessageResult.QueryResultState.FAILTURE) {
                callback.failure(0, result.response);
                return false;
            } else if (result.state == QueryMessageResult.QueryResultState.SUCCESS) {
                callback.success(result.response);
                return true;
            }else{
                return true;
            }
        } catch (Exception e) {
            callback.failure(2, "Invalid JSON format");
            return false;
        }
    }


    @Override
    public void onQueryCanceled(CefBrowser browser, CefFrame frame, long queryId) {

    }

    @Override
    public void setNativeRef(String identifer, long nativeRef) {

    }

    @Override
    public long getNativeRef(String identifer) {
        return 0;
    }
}
