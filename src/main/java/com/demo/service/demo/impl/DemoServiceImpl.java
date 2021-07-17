package com.demo.service.demo.impl;

import com.demo.dao.demo.DemoDao;
import com.demo.entity.demo.Demo;
import com.demo.service.base.impl.BaseServiceImpl;
import com.demo.service.demo.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

@Service
public class DemoServiceImpl extends BaseServiceImpl<Demo> implements DemoService{

    @Autowired
    private DemoDao demoDao;

    @Override
    public void sayHello(Demo demo) {
        System.out.println("hello,"+demo.getDemoName());
    }
}

