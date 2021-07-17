package com.demo.service.tx.impl;

import com.demo.dao.demo.DemoDao;
import com.demo.entity.demo.Demo;
import com.demo.service.base.impl.BaseServiceImpl;
import com.demo.service.demo.DemoService;
import com.demo.service.tx.TxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxServiceImpl extends BaseServiceImpl<Demo> implements TxService {

    @Autowired
    private DemoDao demoDao;


    /**
     * 1.加入外层大事务的方式
     *   1.1 抛出异常：本事务回滚，外层大事务全部回滚
     *   1.2 捕获异常后设置回滚：本事务回滚，外层大事务全部回滚
     *   1.3 捕获异常后不设置回滚：
     *     1.3.1 外层事务没有回滚：本事务正常提交，外层大事务全部提交
     *     1.3.2 外层事务出现回滚：本事务回滚，外层大事务全部回滚
     *
     *  总结：加入外层大事务的方式，意味着与外层大事务同为一体，共进退，保持高度一致性。
     */
    @Transactional
    @Override
    public void add(Demo demo){
          /** 1.1 抛出异常 */
//        demoDao.insert(demo);
//        int x = 1/0;

        /** 1.2 捕获异常后设置回滚 */
//        try{
//            demoDao.insert(demo);
//            int x = 1/0;
//        }catch (Exception e){
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            e.printStackTrace();
//        }

          /** 1.3 捕获异常后不设置回滚 */
        try{
            demoDao.insert(demo);
            int x = 1/0;
        }catch (Exception e){
            e.printStackTrace();
        }



    }

    /**
     * 2.新开事务的方式
     *   2.1 抛出异常：本事务回滚，外层大事务全部回滚
     *   2.2 捕获异常后设置回滚：本事务回滚，外层大事务不受本事务影响
     *   2.3 捕获异常后不设置回滚：本事务正常提交，外层大事务不影响本事务
     *
     *   总结：新开事务的情况下，只要本事务不抛出异常，本事务和外层事务互不影响
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void addWithNewTrans(Demo demo){
        /** 2.1 抛出异常 */
//        demoDao.insert(demo);
//        int x = 1/0;

        /** 2.2 捕获异常后设置回滚 */
//        try{
//            demoDao.insert(demo);
//            int x = 1/0;
//        }catch (Exception e){
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            e.printStackTrace();
//        }

        /** 2.3 捕获异常后不设置回滚 */
        try{
            demoDao.insert(demo);
            int x = 1/0;
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 3.开启内嵌事务的方式
     *   3.1 抛出异常：本事务回滚，外层大事务全部回滚
     *   3.2 捕获异常后设置回滚：本事务回滚，外层大事务不受影响
     *   3.3 捕获异常后不设置回滚：
     *       3.3.1 外层事务没有回滚：本事务正常提交，外层大事务全部提交
     *       3.3.2 外层事务出现回滚：本事务回滚，外层大事务全部回滚
     *
     *   总结：不抛异常的情况下，内嵌事务不影响外层事务，但外层事务影响内嵌事务
     *
     *   内嵌事务和新事物有什么不同？
     *   开启新事物时，会挂起外层的大事务。
     *   但开始内嵌事务时，不会挂起外层的大事务
     */
    @Transactional(propagation = Propagation.NESTED)
    @Override
    public void addWithNESTED(Demo demo){
        /** 3.1 抛出异常 */
//        demoDao.insert(demo);
//        int x = 1/0;

        /** 3.2 捕获异常后设置回滚 */
//        try{
//            demoDao.insert(demo);
//            int x = 1/0;
//        }catch (Exception e){
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            e.printStackTrace();
//        }

//        demoDao.insert(demo);

        /** 3.3 捕获异常后不设置回滚 */
        try{
            demoDao.insert(demo);
            int x = 1/0;
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Transactional
    @Override
    public void change(Demo demo){
        demoDao.updateByPrimaryKey(demo);
    }
}
