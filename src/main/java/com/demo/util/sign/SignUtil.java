package com.demo.util.sign;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * Created by Administrator on 2017-8-12.
 */
public class SignUtil {

    public static boolean checkSign(Map<String, String[]> paramMap){

        String signParamValue = null;
        if(paramMap.get("sign")!=null){
            signParamValue = paramMap.get("sign")[0];
        }
        if(signParamValue==null){
            return false;
        }
        Set<String> keys = paramMap.keySet();
        List<String> arrayKeys = new ArrayList<String>(keys);
        Collections.sort(arrayKeys);

        StringBuilder paramBuilder = new StringBuilder();
        for(int i = 0; i<arrayKeys.size();i++){

            String tmpKey = arrayKeys.get(i);
            String tmpValue = null;
            if("sign".equals(tmpKey)){
                continue;
            }
            if(paramMap.get(tmpKey)!=null){
                tmpValue = paramMap.get(tmpKey)[0];
            }
            if(tmpKey==null){
                continue;
            }

            paramBuilder.append(tmpKey).append(tmpValue);
        }

        String md5HexStr = DigestUtils.md5Hex(paramBuilder.toString()).toUpperCase();

        return md5HexStr.equals(signParamValue);
    }
}
