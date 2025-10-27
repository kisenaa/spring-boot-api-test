package org.elysia.api.service;

public interface RedisService {
    void save(String key, Object value);
    Object get(String key);
    void getListValue(String key, int index);
    void remove(String key);
}
