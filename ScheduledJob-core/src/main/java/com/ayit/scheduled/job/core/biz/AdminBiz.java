package com.ayit.scheduled.job.core.biz;


import com.ayit.scheduled.job.core.biz.model.HandleCallbackParam;
import com.ayit.scheduled.job.core.biz.model.RegistryParam;
import com.ayit.scheduled.job.core.biz.model.ReturnT;

import java.util.List;

public interface AdminBiz {


    public ReturnT<String> registry(RegistryParam registryParam);


    public ReturnT<String> registryRemove(RegistryParam registryParam);
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);

}
