package com.demo.util.http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.List;


/**
 * 这是官方Example的写法
 */
public class HttpClientUtil {


    public static String doGet(String url) throws Exception{

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        try{
            //创建get请求方法
            HttpGet httpGet = new HttpGet(url);

            //执行请求
            response = httpClient.execute(httpGet);

            //获取响应实体
            HttpEntity entity = response.getEntity();

            //获取实体字符串
            String result = EntityUtils.toString(entity, "UTF-8");

            //关闭实体流
            EntityUtils.consume(entity);

            //返回结果
            return result;
        }finally {
            if(response!=null){
                response.close();
            }
        }
    }

    /**
     *
     * 请使用HttpParamBuilder构造nvps
     *
     */
    public static String doFormPost(String url,List<NameValuePair> nvps) throws Exception{

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        try {
            //创建post请求方法
            HttpPost httpPost = new HttpPost(url);

            //加入post请求参数
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            //执行请求
            response = httpClient.execute(httpPost);

            //获取响应实体
            HttpEntity entity = response.getEntity();

            //获取实体字符串
            String result = EntityUtils.toString(entity, "UTF-8");

            //关闭实体流
            EntityUtils.consume(entity);

            //返回结果
            return result;
        } finally {
            if(response!=null){
                response.close();
            }
        }
    }


    public static String doJSONPost(String url,String jsonParamStr) throws Exception{

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        try {
            //创建post请求方法
            HttpPost httpPost = new HttpPost(url);

            //加入post请求参数
            httpPost.setEntity(new StringEntity(jsonParamStr, ContentType.APPLICATION_JSON));

            //执行请求
            response = httpClient.execute(httpPost);

            //获取响应实体
            HttpEntity entity = response.getEntity();

            //获取实体字符串
            String result = EntityUtils.toString(entity, "UTF-8");

            //关闭实体流
            EntityUtils.consume(entity);

            //返回结果
            return result;
        } finally {
            if(response!=null){
                response.close();
            }
        }
    }
}
