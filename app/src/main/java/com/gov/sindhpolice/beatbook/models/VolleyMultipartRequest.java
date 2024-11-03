package com.gov.sindhpolice.beatbook.models;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final Map<String, DataPart> byteData;
    private final Map<String, String> params;
    private final Response.Listener<NetworkResponse> listener;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.byteData = new HashMap<>();
        this.params = new HashMap<>();
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    public void addFile(String key, DataPart dataPart) {
        byteData.put(key, dataPart);
    }

    protected Map<String, DataPart> getByteData() {
        return byteData;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }
}
