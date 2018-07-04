package com.cassandra.utils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class CacheUtils {

    private static Map<String, String> cache = Maps.newConcurrentMap();

    private CacheUtils() {
    }

    private static void startCacheThread(String key, int expiredTimeInSeconds) {
        long startTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
        Runnable runnable = () -> {
            long nowTimestamp;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    nowTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
                    if(nowTimestamp - startTimestamp >= expiredTimeInSeconds) {
                        cache.remove(key);
                        Thread.currentThread().interrupt();
                    }
                }catch (Exception e) {
                    log.error("{}", e);
                }
            }
        };
        new Thread(runnable, "cache thread").start();
    }

    public static void put(String key, Object object) {
        String json = new Gson().toJson(object);
        cache.put(key, json);
        log.info("存入缓存 key = {}, value = {}", key, json);
        startCacheThread(key, 60);
    }

    public static void put(String key, Object object, int expiredTimeInSeconds) {
        String json = new Gson().toJson(object);
        cache.put(key, json);
        log.info("存入缓存 key = {}, value = {}", key, json);
        startCacheThread(key, expiredTimeInSeconds);
    }

    public static void clearAll() {
        log.info("清除缓存");
        cache.clear();
    }

    public static <T> T get(String key, Class<T> clz) {
        String str = cache.get(key);
        if(!StringUtils.isEmpty(str)) {
            return new Gson().fromJson(str, clz);
        }
        return null;
    }
}
