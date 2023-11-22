package com.ayit.scheduled.job.admin.core.route.strategy;


import com.ayit.scheduled.job.admin.core.route.ExecutorRouter;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;

import java.util.List;

/**
 * @author linq
 * @version 1.0
 * @date 2023/11/16 20:42
 * @Description:路由策略之一，选择集合中的第一个地址使用
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList){
        return new ReturnT<String>(addressList.get(0));
    }

}
