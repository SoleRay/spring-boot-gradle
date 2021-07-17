package com.demo.service.bus.impl;

import com.demo.entity.demo.Demo;
import com.demo.service.bus.BusService;
import com.demo.service.tx.TxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusServiceImpl implements BusService {

    @Autowired
    private TxService txService;

    @Transactional
    public void doBusinsess(){
        Demo d = new Demo();
        d.setDemoKey("color");
        d.setDemoName("blue");
        txService.add(d);

        d.setId(2L);
        d.setDemoName("purple");
        txService.change(d);

        int x = 1/0;
    }


    @Transactional
    public void doBusinsess2(){
        Demo d = new Demo();
        d.setDemoKey("color");
        d.setDemoName("blue");
        txService.addWithNewTrans(d);

        d.setId(2L);
        d.setDemoName("purple");
        txService.change(d);

        int x = 1/0;
    }

    @Transactional
    public void doBusinsess3(){
        Demo d = new Demo();
        d.setDemoKey("color");
        d.setDemoName("blue");
        txService.addWithNESTED(d);

        d.setId(2L);
        d.setDemoName("purple");
        txService.change(d);

        int x = 1/0;
    }
}
