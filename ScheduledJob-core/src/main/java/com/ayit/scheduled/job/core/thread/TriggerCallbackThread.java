package com.ayit.scheduled.job.core.thread;

import com.ayit.scheduled.job.core.biz.AdminBiz;
import com.ayit.scheduled.job.core.biz.model.HandleCallbackParam;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import com.ayit.scheduled.job.core.context.XxlJobContext;
import com.ayit.scheduled.job.core.context.XxlJobHelper;
import com.ayit.scheduled.job.core.enums.RegistryConfig;
import com.ayit.scheduled.job.core.executor.XxlJobExecutor;
import com.ayit.scheduled.job.core.log.XxlJobFileAppender;
import com.ayit.scheduled.job.core.util.FileUtil;
import com.ayit.scheduled.job.core.util.JdkSerializeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class TriggerCallbackThread {

    private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);
    private static TriggerCallbackThread instance = new TriggerCallbackThread();

    public static TriggerCallbackThread getInstance(){
        return instance;
    }

    private LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<HandleCallbackParam>();

    public static void pushCallBack(HandleCallbackParam callback){
        getInstance().callBackQueue.add(callback);
        logger.debug(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    private Thread triggerCallbackThread;
    private Thread triggerRetryCallbackThread;
    private volatile boolean toStop = false;

    public void start() {
        if (XxlJobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor callback config fail, adminAddresses is null.");
            return;
        }
        triggerCallbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!toStop){
                    try {
                        HandleCallbackParam callback = getInstance().callBackQueue.take();
                        if (callback != null) {
                            List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                            int drainToNum = getInstance().callBackQueue.drainTo(callbackParamList);
                            callbackParamList.add(callback);
                            if (callbackParamList!=null && callbackParamList.size()>0) {
                                //在这里执行回调给调度中心的操作
                                doCallback(callbackParamList);
                            }
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                try {
                    List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                    int drainToNum = getInstance().callBackQueue.drainTo(callbackParamList);
                    if (callbackParamList!=null && callbackParamList.size()>0) {
                        doCallback(callbackParamList);
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, executor callback thread destroy.");
            }
        });
        triggerCallbackThread.setDaemon(true);
        triggerCallbackThread.setName("xxl-job, executor TriggerCallbackThread");
        triggerCallbackThread.start();


        triggerRetryCallbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!toStop){
                    try {
                        //重新回调一次
                        retryFailCallbackFile();
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        //休息30秒再次重试
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, executor retry callback thread destroy.");
            }
        });
        triggerRetryCallbackThread.setDaemon(true);
        triggerRetryCallbackThread.start();

    }


    public void toStop(){
        toStop = true;
        if (triggerCallbackThread != null) {
            triggerCallbackThread.interrupt();
            try {
                triggerCallbackThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (triggerRetryCallbackThread != null) {
            triggerRetryCallbackThread.interrupt();
            try {
                triggerRetryCallbackThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    private void doCallback(List<HandleCallbackParam> callbackParamList){
        boolean callbackRet = false;
        for (AdminBiz adminBiz: XxlJobExecutor.getAdminBizList()) {
            try {
                ReturnT<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult!=null && ReturnT.SUCCESS_CODE == callbackResult.getCode()) {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                    callbackRet = true;
                    break;
                } else {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Exception e) {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
            }
        }
        if (!callbackRet) {
            appendFailCallbackFile(callbackParamList);
        }
    }


    private void callbackLog(List<HandleCallbackParam> callbackParamList, String logContent){
        for (HandleCallbackParam callbackParam: callbackParamList) {
            String logFileName = XxlJobFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTim()), callbackParam.getLogId());
            XxlJobContext.setXxlJobContext(new XxlJobContext(
                    -1,
                    null,
                    logFileName,
                    -1,
                    -1));
            XxlJobHelper.log(logContent);
        }
    }

    //设置回调失败日志的本地存储路径
    private static String failCallbackFilePath = XxlJobFileAppender.getLogPath().concat(File.separator).concat("callbacklog").concat(File.separator);

    //设置回调失败日志存储的文件名
    private static String failCallbackFileName = failCallbackFilePath.concat("xxl-job-callback-{x}").concat(".log");

    private void appendFailCallbackFile(List<HandleCallbackParam> callbackParamList){
        //判空校验
        if (callbackParamList==null || callbackParamList.size()==0) {
            return;
        }
        //把回调数据序列化
        byte[] callbackParamList_bytes = JdkSerializeTool.serialize(callbackParamList);
        //创建文件名，把x用当前时间戳替换
        File callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis())));
        if (callbackLogFile.exists()) {
            for (int i = 0; i < 100; i++) {
                callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis()).concat("-").concat(String.valueOf(i))));
                if (!callbackLogFile.exists()) {
                    break;
                }
            }
        }
        FileUtil.writeFileContent(callbackLogFile, callbackParamList_bytes);
    }


    private void retryFailCallbackFile(){
        //得到文件夹路径
        File callbackLogPath = new File(failCallbackFilePath);
        if (!callbackLogPath.exists()) {
            return;
        }
        if (callbackLogPath.isFile()) {
            callbackLogPath.delete();
        }
        if (!(callbackLogPath.isDirectory() && callbackLogPath.list()!=null && callbackLogPath.list().length>0)) {
            return;
        }
        //遍历文件夹中的日志文件
        for (File callbaclLogFile: callbackLogPath.listFiles()) {
            //读取日志信息
            byte[] callbackParamList_bytes = FileUtil.readFileContent(callbaclLogFile);
            if(callbackParamList_bytes == null || callbackParamList_bytes.length < 1){
                //如果文件是空的就删除
                callbaclLogFile.delete();
                continue;
            }
            //反序列化一下
            List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) JdkSerializeTool.deserialize(callbackParamList_bytes, List.class);
            //删除文件
            callbaclLogFile.delete();
            //重新回调一次
            doCallback(callbackParamList);
        }

    }

}
