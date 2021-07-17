package com.demo.controller.geo;

import com.demo.dto.request.geo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/geo")
public class GeoController {

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/add")
    public void add(@RequestBody GeoAddParam param) {
        Point point = new Point(param.getLng(),param.getLat());
        redisTemplate.opsForGeo().add(param.getKey(), new RedisGeoCommands.GeoLocation(param.getMember(),point));
    }

    @RequestMapping(value = "/dist")
    public Distance dist(@RequestBody GeoDistParam param) {
        Distance distance = redisTemplate.opsForGeo().distance(param.getKey(), param.getMember1(), param.getMember2());
        return distance;
    }

    @RequestMapping(value = "/pos")
    public List<String> pos(@RequestBody GeoPosParam param) {
        List<String> position = redisTemplate.opsForGeo().position(param.getKey(),param.getMembers().toArray());
        return position;
    }

    @RequestMapping(value = "/radius")
    public GeoResults radius(@RequestBody GeoRadiusParam param) {
        Circle circle = new Circle(param.getLng(),param.getLat(),param.getRadius());
        GeoResults geoResults = redisTemplate.opsForGeo().radius(param.getKey(), circle);
        return geoResults;
    }

    @RequestMapping(value = "/radiusbymember")
    public GeoResults radiusbymember(@RequestBody GeoRadiusByMemberParam param) {
        GeoResults geoResults = redisTemplate.opsForGeo().radius(param.getKey(), param.getMember(),param.getRadius());
        return geoResults;
    }
}
