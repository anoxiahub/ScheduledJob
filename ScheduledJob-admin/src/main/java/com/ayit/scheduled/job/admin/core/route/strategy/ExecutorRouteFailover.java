package com.ayit.scheduled.job.admin.core.route.strategy;



import com.ayit.scheduled.job.admin.core.route.ExecutorRouter;
import com.ayit.scheduled.job.admin.core.scheduler.XxlJobScheduler;
import com.ayit.scheduled.job.admin.core.util.I18nUtil;
import com.ayit.scheduled.job.core.biz.ExecutorBiz;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;

import java.util.List;

/**
 * @author linq
 * @version 1.0
 * @date 2023/11/16 20:42
 * @Description:故障转移策略
 */
public class ExecutorRouteFailover extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        StringBuffer beatResultSB = new StringBuffer();
        for (String address : addressList) {
            ReturnT<String> beatResult = null;
            try {
                ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(address);
                beatResult = executorBiz.beat();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                beatResult = new ReturnT<String>(ReturnT.FAIL_CODE, ""+e );
            }
            beatResultSB.append( (beatResultSB.length()>0)?"<br><br>":"")
                    .append(I18nUtil.getString("jobconf_beat") + "：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(beatResult.getCode())
                    .append("<br>msg：").append(beatResult.getMsg());
            //心跳检测没问题，就直接使用该执行器
            if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {

                beatResult.setMsg(beatResultSB.toString());
                beatResult.setContent(address);
                return beatResult;
            }
        }
        return new ReturnT<String>(ReturnT.FAIL_CODE, beatResultSB.toString());

    }
}
