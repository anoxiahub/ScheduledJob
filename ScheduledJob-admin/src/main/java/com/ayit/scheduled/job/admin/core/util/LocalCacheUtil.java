package com.ayit.scheduled.job.admin.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalCacheUtil {

    //这个Map就是缓存数据的容器
    private static ConcurrentMap<String, LocalCacheData> cacheRepository = new ConcurrentHashMap<String, LocalCacheData>();

    //内部类，其实存储的键值对，要放在这个内部类中，Map中存储的value是这个类的对象
    private static class LocalCacheData{
        private String key;
        private Object val;
        private long timeoutTime;

        public LocalCacheData() {
        }

        public LocalCacheData(String key, Object val, long timeoutTime) {
            this.key = key;
            this.val = val;
            this.timeoutTime = timeoutTime;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getVal() {
            return val;
        }

        public void setVal(Object val) {
            this.val = val;
        }

        public long getTimeoutTime() {
            return timeoutTime;
        }

        public void setTimeoutTime(long timeoutTime) {
            this.timeoutTime = timeoutTime;
        }
    }


    public static boolean set(String key, Object val, long cacheTime){
        //先清除一次缓存超时的数据
        cleanTimeoutCache();
        //对key-value-缓存时间做判空
        if (key==null || key.trim().length()==0) {
            return false;
        }
        if (val == null) {
            remove(key);
        }
        if (cacheTime <= 0) {
            remove(key);
        }
        //获取当前缓存数据的超时时间
        long timeoutTime = System.currentTimeMillis() + cacheTime;
        //创建缓存键值对的对象
        LocalCacheData localCacheData = new LocalCacheData(key, val, timeoutTime);
        //放入Map中
        cacheRepository.put(localCacheData.getKey(), localCacheData);
        return true;
    }


    public static boolean remove(String key){
        if (key==null || key.trim().length()==0) {
            return false;
        }
        cacheRepository.remove(key);
        return true;
    }

    public static Object get(String key){
        if (key==null || key.trim().length()==0) {
            return null;
        }
        LocalCacheData localCacheData = cacheRepository.get(key);
        if (localCacheData!=null && System.currentTimeMillis()<localCacheData.getTimeoutTime()) {
            return localCacheData.getVal();
        } else {
            remove(key);
            return null;
        }
    }


    public static boolean cleanTimeoutCache(){
        if (!cacheRepository.keySet().isEmpty()) {
            for (String key: cacheRepository.keySet()) {
                LocalCacheData localCacheData = cacheRepository.get(key);
                if (localCacheData!=null && System.currentTimeMillis()>=localCacheData.getTimeoutTime()) {
                    cacheRepository.remove(key);
                }
            }
        }
        return true;
    }

}
