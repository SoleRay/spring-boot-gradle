package com.demo.service.cache.impl;

import com.demo.dao.demo.DemoDao;
import com.demo.entity.demo.Demo;
import com.demo.service.cache.CacheService;
import com.demo.service.demo.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private DemoService demoService;

    @Cacheable(cacheManager = "cacheManager", value = "demo-id", key = "#id")
    @Override
    public Demo findNameById(Long id) {
        Demo demo = demoService.selectByPrimaryKey(id);
        return demo;
    }

    @CacheEvict(cacheManager = "cacheManager", value = "demo-id", key = "#id")
    @Override
    public void deleteDemoById(Long id) {
        demoService.deleteByPrimaryKey(id);
    }

//    @CachePut(cacheManager = "cacheManager", value = "demo-id", key = "#demo.id")
    @Override
    @Transactional
    public Demo updateDemo(Demo demo) {
        demoService.updateByPrimaryKeySelective(demo);
        return this.findNameById(demo.getId());
    }
}
