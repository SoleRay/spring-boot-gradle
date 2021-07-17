package com.demo.bean.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class ByteRedisTemplate<K, V> extends RedisTemplate<String, Object> {

    public ByteRedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();

        setKeySerializer(stringSerializer);

        setHashKeySerializer(stringSerializer);
        setEnableDefaultSerializer(false);
    }

    /**
     * Constructs a new <code>StringRedisTemplate</code> instance ready to be used.
     *
     * @param connectionFactory connection factory for creating new connections
     */
    public ByteRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }
}
