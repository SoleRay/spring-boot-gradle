package com.demo.controller.redis;

import com.demo.util.redis.RedisLock;
import com.demo.util.uuid.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/set")
    public void set(String key,String value) throws Exception {
        redisTemplate.opsForValue().set(key,value);
    }

    @RequestMapping(value = "/get")
    public Object get(String key) throws Exception {
        Object value = redisTemplate.opsForValue().get(key);
        return value;
    }

    @RequestMapping(value = "/batchSet")
    public void batchSet(int loop) throws Exception {
        for(int i=0;i<loop;i++){
            redisTemplate.opsForValue().set(i+"",i+"");
        }
    }

    @RequestMapping(value = "/batchHSet")
    public void batchHSet(int loop) throws Exception {
        for(int i=0;i<loop;i++){
            redisTemplate.opsForHash().putIfAbsent("box","sub-"+i,i+"");
        }
    }

    @PostMapping(value="/testLock")
    public void testLock(boolean fair){

        CountDownLatch countDownLatch = new CountDownLatch(5);
        RedisLock redisLock = new RedisLock(redisTemplate,fair);
        Runnable r = ()->{
            String value = UUIDUtil.genUUID();
            redisLock.lock(value);
            try{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }finally {
                redisLock.unlock();
                countDownLatch.countDown();
            }
        };
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(r,"Thread-" + i);
            t.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
