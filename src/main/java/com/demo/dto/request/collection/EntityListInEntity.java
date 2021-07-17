package com.demo.dto.request.collection;

import com.demo.dto.request.entity.Entity;

import java.util.List;

public class EntityListInEntity {

    private List<Entity> entityList;

    public List<Entity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<Entity> entityList) {
        this.entityList = entityList;
    }
}
