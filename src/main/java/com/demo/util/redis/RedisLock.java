package com.demo.util.redis;

import com.demo.util.uuid.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class RedisLock extends AbstractQueuedSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(RedisLock.class);

    private RedisTemplate redisTemplate;

    private final String key;

    private long expireTime = 3 * 1000;

    private Sync sync;

    public RedisLock(RedisTemplate redisTemplate) {
        this(redisTemplate,true);
    }

    public RedisLock(RedisTemplate redisTemplate,boolean fair) {
        this(redisTemplate, 0, fair);
    }

    public RedisLock(RedisTemplate redisTemplate, long expireTime, boolean fair) {
        this.redisTemplate = redisTemplate;
        this.expireTime = expireTime == 0 ? this.expireTime : expireTime;
        sync = fair ? new FairSync() : new NonFairSync();

        key = UUIDUtil.genUUID();
    }

    interface Sync {
        void lock(String value);

        void unlock();
    }

    class FairSync implements Sync {

        private Queue<Thread> queue = new LinkedBlockingQueue();

        @Override
        public void lock(String value) {
            while (!tryLock(value)) {
                queue.add(Thread.currentThread());
                LockSupport.park();
            }
        }

        @Override
        public void unlock() {
            redisTemplate.delete(key);
            Thread t = queue.poll();
            if (t != null) {
                LockSupport.unpark(t);
            }
            LOG.info(Thread.currentThread().getName() + " successful release lock!");
        }
    }

    class NonFairSync implements Sync {

        private ReentrantLock lock = new ReentrantLock();
        private Condition condition = lock.newCondition();

        @Override
        public void lock(String value) {
            while (!tryLock(value)) {
                lock.lock();
                try {
                    condition.await(expireTime,TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public void unlock() {
            lock.lock();
            try {
                redisTemplate.delete(key);
                condition.signalAll();
                LOG.info(Thread.currentThread().getName() + " successful release lock!");
            } finally {
                lock.unlock();
            }

        }
    }

    public void lock(String value) {
        if (value == null) {
            throw new RuntimeException("value can't be mull!");
        }

        sync.lock(value);
    }

    public void unlock() {
        sync.unlock();
    }

    public boolean tryLock(String value) {
        redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.MILLISECONDS);

        String valueInRedis = redisTemplate.opsForValue().get(key).toString();
        if (value.equals(valueInRedis)) {
            LOG.info(Thread.currentThread().getName() + " successful get lock!");
            return true;
        }
        return false;
    }


}
