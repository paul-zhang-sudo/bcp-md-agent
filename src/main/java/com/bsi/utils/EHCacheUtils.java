package com.bsi.utils;

import com.bsi.framework.core.utils.EHCacheUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class EHCacheUtils {
    private static final String cacheName = "taskCache";
    public static void put(Object key, Object value){
        EHCacheUtil.setValue(cacheName,key,value);
    }
    public static void put(Object key, Object value, int timeToLiveSeconds){
        EHCacheUtil.setValue(cacheName,key,value,timeToLiveSeconds);
    }

    public static Object get(Object key){
        return EHCacheUtil.getValue(cacheName,key);
    }

    public static void put (Object key, Object value,int timeToLiveSeconds,int timeToIdleSeconds) {
        Cache myCache = EHCacheUtil.initCache(cacheName);
        myCache.put(new Element(key, value,timeToIdleSeconds,timeToLiveSeconds));
    }
}
