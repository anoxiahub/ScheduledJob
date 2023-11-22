package com.ayit.scheduled.job.admin.core.route.strategy;


import com.ayit.scheduled.job.admin.core.route.ExecutorRouter;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linq
 * @version 1.0
 * @date 2023/11/16 20:42
 * @Description:通过轮训的策略选择执行器地址
 */
public class ExecutorRouteRound extends ExecutorRouter {
    private static ConcurrentMap<Integer, AtomicInteger> routeCountEachJob = new ConcurrentHashMap<>();
    private static long CACHE_VALID_TIME = 0;

    private static int count(int jobId) {
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            routeCountEachJob.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000*60*60*24;
        }
        AtomicInteger count = routeCountEachJob.get(jobId);
        if (count == null || count.get() > 1000000) {
            count = new AtomicInteger(new Random().nextInt(100));
        } else {
            count.addAndGet(1);
        }
        routeCountEachJob.put(jobId, count);
        return count.get();
    }

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(count(triggerParam.getJobId())%addressList.size());
        return new ReturnT<String>(address);
    }

}
