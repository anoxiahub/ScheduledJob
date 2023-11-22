package com.ayit.scheduled.job.admin.core.route.strategy;


import com.ayit.scheduled.job.admin.core.route.ExecutorRouter;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;

import java.util.List;
import java.util.Random;

/**
 * @author linq
 * @version 1.0
 * @date 2023/11/16 20:42
 * @Description:随机选择一个执行器地址
 */
public class ExecutorRouteRandom extends ExecutorRouter {

    private static Random localRandom = new Random();

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return new ReturnT<String>(address);
    }

}
