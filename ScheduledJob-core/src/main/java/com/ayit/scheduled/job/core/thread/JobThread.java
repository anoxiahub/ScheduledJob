package com.ayit.scheduled.job.core.thread;

import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;
import com.ayit.scheduled.job.core.executor.XxlJobExecutor;
import com.ayit.scheduled.job.core.handler.IJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class JobThread extends Thread{

	private static Logger logger = LoggerFactory.getLogger(JobThread.class);

	private int jobId;
	private IJobHandler handler;
	private LinkedBlockingQueue<TriggerParam> triggerQueue;
	private volatile boolean toStop = false;
	private String stopReason;
    private boolean running = false;
	private int idleTimes = 0;

	public JobThread(int jobId, IJobHandler handler) {
		this.jobId = jobId;
		this.handler = handler;
		//初始化队列
		this.triggerQueue = new LinkedBlockingQueue<TriggerParam>();
		//设置工作线程名称
		this.setName("xxl-job, JobThread-"+jobId+"-"+System.currentTimeMillis());
	}


	public IJobHandler getHandler() {
		return handler;
	}


	public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
		triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
	}

	public void toStop(String stopReason) {
		this.toStop = true;
		this.stopReason = stopReason;
	}


    public boolean isRunningOrHasQueue() {
        return running || triggerQueue.size()>0;
    }


    @Override
	public void run() {
    	try {
			handler.init();
		} catch (Throwable e) {
    		logger.error(e.getMessage(), e);
		}
		while(!toStop){
			running = false;
			idleTimes++;
            TriggerParam triggerParam = null;
            try {
				triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
				if (triggerParam!=null) {
					running = true;
					idleTimes = 0;
					handler.execute();
				} else {
					if (idleTimes > 30) {
						if(triggerQueue.size() == 0) {
							XxlJobExecutor.removeJobThread(jobId, "excutor idel times over limit.");
						}
					}
				}
			} catch (Throwable e) {
				if (toStop) {
					logger.info("<br>----------- JobThread toStop, stopReason:" + stopReason);
				}
			}
        }
		try {
			handler.destroy();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		logger.info(">>>>>>>>>>> xxl-job JobThread stoped, hashCode:{}", Thread.currentThread());
	}
}
