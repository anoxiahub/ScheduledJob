package com.ayit.scheduled.job.core.biz.model;

import java.io.Serializable;

/**
 * @author linq
 * @version 1.0
 * @date 2023/11/16 20:42
 * @Description:这个就是用于执行器回调定时任务执行结果的包装类
 * 执行结果的信息用这个类的对象封装
 */
public class HandleCallbackParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private long logId;
    private long logDateTim;
    private int handleCode;
    private String handleMsg;

    public HandleCallbackParam(){}

    public HandleCallbackParam(long logId, long logDateTim, int handleCode, String handleMsg) {
        this.logId = logId;
        this.logDateTim = logDateTim;
        this.handleCode = handleCode;
        this.handleMsg = handleMsg;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getLogDateTim() {
        return logDateTim;
    }

    public void setLogDateTim(long logDateTim) {
        this.logDateTim = logDateTim;
    }

    public int getHandleCode() {
        return handleCode;
    }

    public void setHandleCode(int handleCode) {
        this.handleCode = handleCode;
    }

    public String getHandleMsg() {
        return handleMsg;
    }

    public void setHandleMsg(String handleMsg) {
        this.handleMsg = handleMsg;
    }

    @Override
    public String toString() {
        return "HandleCallbackParam{" +
                "logId=" + logId +
                ", logDateTim=" + logDateTim +
                ", handleCode=" + handleCode +
                ", handleMsg='" + handleMsg + '\'' +
                '}';
    }

}
