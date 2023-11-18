package com.ayit.scheduled.job.admin.core.scheduler;

import com.ayit.scheduled.job.admin.core.conf.XxlJobAdminConfig;
import com.ayit.scheduled.job.admin.core.thread.JobRegistryHelper;
import com.ayit.scheduled.job.admin.core.thread.JobScheduleHelper;
import com.ayit.scheduled.job.admin.core.thread.JobTriggerPoolHelper;
import com.ayit.scheduled.job.core.biz.ExecutorBiz;
import com.ayit.scheduled.job.core.biz.client.ExecutorBizClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author linq
 * @version 1.0
 * @description: TODO
 * @date 2023/11/17 17:18
 */

public class XxlJobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobScheduler.class);

    public void init() throws Exception {
        JobTriggerPoolHelper.toStart();
        JobRegistryHelper.getInstance().start();
        JobScheduleHelper.getInstance().start();
    }

    public void destroy() throws Exception {

        JobScheduleHelper.getInstance().toStop();
        JobRegistryHelper.getInstance().toStop();
        JobTriggerPoolHelper.toStop();

    }
    private static ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<String, ExecutorBiz>();
    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        //判断远程地址是否为空
        if (address==null || address.trim().length()==0) {
            return null;
        }
        //规整一下地址，去掉空格
        address = address.trim();
        //从远程调用的Map集合中获得远程调用的客户端
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            //如果有就直接返回
            return executorBiz;
        }
        //如果没有就创建一个客户端，然后存放到Map中，我现在是根据最新版本的源码来迭代手写代码的
        //但是，在旧版本，也就是2.0.2版本之前的版本，在xxl-job客户端，也就是执行器实例中，是用jetty进行通信的
        //在2.0.2版本之后，将jetty改成了netty，这个大家了解一下即可
        //这时候，本来作为客户端的执行器，在使用Netty构建了服务端后，又拥有服务端的身份了
        executorBiz = new ExecutorBizClient(address, XxlJobAdminConfig.getAdminConfig().getAccessToken());
        //把创建好的客户端放到Map中
        executorBizRepository.put(address, executorBiz);
        return executorBiz;
    }
}
