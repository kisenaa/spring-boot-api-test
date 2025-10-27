package org.elysia.api.service.impl;

import org.elysia.api.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String key, Object value){
        this.redisTemplate.opsForValue().set(key, value);
    }

    public Object get(String key) {
        return this.redisTemplate.opsForValue().get(key);
    }

    public void getListValue(String key, int index){
        this.redisTemplate.opsForList().index(key, index);
    }

    public void remove(String key){
        this.redisTemplate.delete(key);
    }

}
