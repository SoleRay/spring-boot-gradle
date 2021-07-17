package com.demo.service.tx;

import com.demo.entity.demo.Demo;
import com.demo.service.base.BaseService;

public interface TxService extends BaseService<Demo> {

    void add(Demo demo);

    void addWithNewTrans(Demo demo);

    void addWithNESTED(Demo demo);

    void change(Demo demo);
}
