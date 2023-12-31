package com.ayit.scheduled.job.core.biz;


import com.ayit.scheduled.job.core.biz.model.IdleBeatParam;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;

public interface ExecutorBiz {

    //远程调用的方法
    ReturnT<String> run(TriggerParam triggerParam);

    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam);
    public ReturnT<String> beat();
}
