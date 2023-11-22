package com.ayit.scheduled.job.admin.core.route;

import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author linq
 * @version 1.0
 * @date 2023/11/16 20:42
 * @Description:路由策略的抽象类，现在还用不到路由策略，所以我们暂且只引入这一个抽象类，只是为了代码不报错
 */
public abstract class ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    public abstract ReturnT<String> route(TriggerParam triggerParam, List<String> addressList);

}
