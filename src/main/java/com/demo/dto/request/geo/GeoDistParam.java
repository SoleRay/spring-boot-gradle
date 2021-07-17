package com.demo.dto.request.geo;

import com.demo.dto.base.redis.RedisKeyParam;
import lombok.Data;

@Data
public class GeoDistParam extends RedisKeyParam {

    private String member1;

    private String member2;
}
