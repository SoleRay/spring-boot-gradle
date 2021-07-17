package com.demo.controller.cache;

import com.demo.entity.demo.Demo;
import com.demo.service.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;

    @PostMapping(value="/findNameById")
    public Demo findNameById(Long id){
        Demo demo = cacheService.findNameById(id);
        return demo;
    }

    @PostMapping(value="/deleteDemoById")
    public void deleteDemoById(Long id){
        cacheService.deleteDemoById(id);
    }

    @PostMapping(value="/updateDemo")
    public Demo updateDemo(Demo demo){
        Demo updateDemo = cacheService.updateDemo(demo);
        return updateDemo;
    }
}
