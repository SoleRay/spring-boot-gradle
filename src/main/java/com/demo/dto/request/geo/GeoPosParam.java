package com.demo.dto.request.geo;

import com.demo.dto.base.redis.RedisKeyParam;
import lombok.Data;

import java.util.List;

@Data
public class GeoPosParam extends RedisKeyParam {

    private List<String> members;
}
