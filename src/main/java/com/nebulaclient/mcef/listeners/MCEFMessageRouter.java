package com.nebulaclient.mcef.listeners;

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
        QueryMessage message = QueryMessageManager.getMessage(request);
        QueryMessageResult result = message.onMessage(new QueryMessageData(request));

        callback.success(result.response);
        callback.failure(0,result.response);


        if(result.state == QueryMessageResult.QueryResultState.FAILTURE){
            return false;
        }else {
            return true;
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
