package com.demo.util.uuid;

import java.util.UUID;

/**
 * Created by Arthur on 2016/12/28 0028.
 */
public class UUIDUtil {

    public static String genUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
