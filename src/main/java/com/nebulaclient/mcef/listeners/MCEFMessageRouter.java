package com.nebulaclient.mcef.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandler;

public class MCEFMessageRouter implements CefMessageRouterHandler {

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        return false;
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
