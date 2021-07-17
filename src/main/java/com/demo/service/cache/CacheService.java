package com.demo.service.cache;

import com.demo.entity.demo.Demo;

public interface CacheService {

    Demo findNameById(Long id);

    void deleteDemoById(Long id);

    Demo updateDemo(Demo demo);
}
