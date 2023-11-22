package com.ayit.scheduled.job.core.biz.client;


import com.ayit.scheduled.job.core.biz.ExecutorBiz;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;
import com.ayit.scheduled.job.core.util.XxlJobRemotingUtil;

public class ExecutorBizClient implements ExecutorBiz {


    public ExecutorBizClient() {
    }

    //构造方法
    public ExecutorBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
    }

    private String addressUrl ;
    private String accessToken;
    private int timeout = 3;



    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        //可以看到，在这里直接用一个工具类用post请求发送消息了
        return XxlJobRemotingUtil.postBody(addressUrl + "run", accessToken, timeout, triggerParam, String.class);
    }

}
