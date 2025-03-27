package com.nebulaclient.mcef.messaging;

public class QueryMessageResult {

    public String response;
    public static enum QueryResultState {
        FAILTURE,
        SUCCESS,
    }
    public QueryResultState state = QueryResultState.FAILTURE;

    public static QueryMessageResult fail(String response) {
        QueryMessageResult result = new QueryMessageResult();
        result.state = QueryResultState.FAILTURE;
        result.response = response;
        return result;
    }

    public static QueryMessageResult success(String response) {
        QueryMessageResult result = new QueryMessageResult();
        result.state = QueryResultState.SUCCESS;
        result.response = response;
        return result;
    }

}
