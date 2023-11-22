package com.ayit.scheduled.job.admin.core.route;


import com.ayit.scheduled.job.admin.core.route.strategy.*;
import com.ayit.scheduled.job.admin.core.util.I18nUtil;

/**
 * @author linq
 * @version 1.0
 * @date 2023/11/16 20:42
 */
public enum ExecutorRouteStrategyEnum {

    FIRST(I18nUtil.getString("jobconf_route_first"), new ExecutorRouteFirst()),
    //使用最后一个
    LAST(I18nUtil.getString("jobconf_route_last"), new ExecutorRouteLast()),
    //轮训
    ROUND(I18nUtil.getString("jobconf_route_round"), new ExecutorRouteRound()),
    //随机
    RANDOM(I18nUtil.getString("jobconf_route_random"), new ExecutorRouteRandom()),
    //一致性哈希
    CONSISTENT_HASH(I18nUtil.getString("jobconf_route_consistenthash"), new ExecutorRouteConsistentHash()),
    //最不经常使用
    LEAST_FREQUENTLY_USED(I18nUtil.getString("jobconf_route_lfu"), new ExecutorRouteLFU()),
    //最近最久未使用
    LEAST_RECENTLY_USED(I18nUtil.getString("jobconf_route_lru"), new ExecutorRouteLRU()),
    //故障转移
    FAILOVER(I18nUtil.getString("jobconf_route_failover"), new ExecutorRouteFailover()),
    //忙碌转移
    BUSYOVER(I18nUtil.getString("jobconf_route_busyover"), new ExecutorRouteBusyover()),
    //分片广播
    SHARDING_BROADCAST(I18nUtil.getString("jobconf_route_shard"), null);
    ExecutorRouteStrategyEnum(String title, ExecutorRouter router) {
        this.title = title;
        this.router = router;
    }

    private String title;
    private ExecutorRouter router;

    public String getTitle() {
        return title;
    }
    public ExecutorRouter getRouter() {
        return router;
    }

    public static ExecutorRouteStrategyEnum match(String name, ExecutorRouteStrategyEnum defaultItem){
        if (name != null) {
            for (ExecutorRouteStrategyEnum item: ExecutorRouteStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }

}
