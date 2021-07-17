package com.demo.util.http;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpParamBuilder {


    public static List<NameValuePair> build(Map<String,Object> params){

        List<NameValuePair> nvps = new ArrayList<>();

        for(Map.Entry<String,Object> param : params.entrySet()){
            NameValuePair nvp = new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue()));
            nvps.add(nvp);
        }
        return nvps;
    }
}
