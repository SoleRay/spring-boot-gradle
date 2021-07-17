package com.demo.dto.request.geo;

import com.demo.dto.base.redis.RedisKeyParam;
import lombok.Data;

@Data
public class GeoAddParam extends RedisKeyParam {

    private double lat;

    private double lng;

    private String member;


}
