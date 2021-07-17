package com.demo.dto.request.map;

import com.demo.dto.request.entity.Entity;

import java.util.Map;

public class EntityMapInEntity {

    private Map<String, Entity> map;

    public Map<String, Entity> getMap() {
        return map;
    }

    public void setMap(Map<String, Entity> map) {
        this.map = map;
    }
}
