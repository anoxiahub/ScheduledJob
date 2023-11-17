package com.ayit.scheduled.job.admin.core.scheduler;

import com.ayit.scheduled.job.admin.core.thread.JobRegistryHelper;
import com.ayit.scheduled.job.admin.core.thread.JobScheduleHelper;
import com.ayit.scheduled.job.admin.core.thread.JobTriggerPoolHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
