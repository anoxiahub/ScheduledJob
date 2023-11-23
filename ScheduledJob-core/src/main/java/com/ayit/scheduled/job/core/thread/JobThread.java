package com.ayit.scheduled.job.core.thread;

import com.ayit.scheduled.job.core.biz.model.HandleCallbackParam;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.biz.model.TriggerParam;
import com.ayit.scheduled.job.core.context.XxlJobContext;
import com.ayit.scheduled.job.core.context.XxlJobHelper;
import com.ayit.scheduled.job.core.executor.XxlJobExecutor;
import com.ayit.scheduled.job.core.handler.IJobHandler;
import com.ayit.scheduled.job.core.log.XxlJobFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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

	private Set<Long> triggerLogIdSet;

	public JobThread(int jobId, IJobHandler handler) {
		this.jobId = jobId;
		this.handler = handler;
		//初始化队列
		this.triggerQueue = new LinkedBlockingQueue<TriggerParam>();

		this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<Long>());
		//设置工作线程名称
		this.setName("xxl-job, JobThread-"+jobId+"-"+System.currentTimeMillis());
	}


	public IJobHandler getHandler() {
		return handler;
	}


	public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
		if (triggerLogIdSet.contains(triggerParam.getLogId())) {
			logger.info(">>>>>>>>>>> repeate trigger job, logId:{}", triggerParam.getLogId());
			return new ReturnT<String>(ReturnT.FAIL_CODE, "repeate trigger job, logId:" + triggerParam.getLogId());
		}
		triggerQueue.add(triggerParam);
		triggerLogIdSet.add(triggerParam.getLogId());
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
					triggerLogIdSet.remove(triggerParam.getLogId());
					String logFileName = XxlJobFileAppender.makeLogFileName(new Date(triggerParam.getLogDateTime()), triggerParam.getLogId());
					XxlJobContext xxlJobContext = new XxlJobContext(
							triggerParam.getJobId(),
							triggerParam.getExecutorParams(),
							logFileName,
							triggerParam.getBroadcastIndex(),
							triggerParam.getBroadcastTotal());
					XxlJobContext.setXxlJobContext(xxlJobContext);
					XxlJobHelper.log("<br>----------- xxl-job job execute start -----------<br>----------- Param:" + xxlJobContext.getJobParam());
					handler.execute();
					if (XxlJobContext.getXxlJobContext().getHandleCode() <= 0) {
						XxlJobHelper.handleFail("job handle result lost.");
					}else {
						String tempHandleMsg = XxlJobContext.getXxlJobContext().getHandleMsg();
						tempHandleMsg = (tempHandleMsg!=null&&tempHandleMsg.length()>50000)
								?tempHandleMsg.substring(0, 50000).concat("...")
								:tempHandleMsg;
						XxlJobContext.getXxlJobContext().setHandleMsg(tempHandleMsg);
					}
					XxlJobHelper.log("<br>----------- xxl-job job execute end(finish) -----------<br>----------- Result: handleCode="
							+ XxlJobContext.getXxlJobContext().getHandleCode()
							+ ", handleMsg = "
							+ XxlJobContext.getXxlJobContext().getHandleMsg()
					);
				} else {
					if (idleTimes > 30) {
						if(triggerQueue.size() == 0) {
							XxlJobExecutor.removeJobThread(jobId, "excutor idel times over limit.");
						}
					}
				}
			} catch (Throwable e) {
				if (toStop) {
					XxlJobHelper.log("<br>----------- JobThread toStop, stopReason:" + stopReason);
					StringWriter stringWriter = new StringWriter();
					e.printStackTrace(new PrintWriter(stringWriter));
					String errorMsg = stringWriter.toString();
					XxlJobHelper.handleFail(errorMsg);
					XxlJobHelper.log("<br>----------- JobThread Exception:" + errorMsg + "<br>----------- xxl-job job execute end(error) -----------");
				}
			}finally {
				if(triggerParam != null) {
					if (!toStop) {
						TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
								triggerParam.getLogId(),
								triggerParam.getLogDateTime(),
								XxlJobContext.getXxlJobContext().getHandleCode(),
								XxlJobContext.getXxlJobContext().getHandleMsg() )
						);
					} else {
						TriggerCallbackThread.pushCallBack(new HandleCallbackParam(
								triggerParam.getLogId(),
								triggerParam.getLogDateTime(),
								XxlJobContext.HANDLE_CODE_FAIL,
								stopReason + " [job running, killed]" )
						);
					}
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
