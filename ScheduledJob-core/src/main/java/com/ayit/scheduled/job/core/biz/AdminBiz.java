package com.ayit.scheduled.job.core.biz;


import com.ayit.scheduled.job.core.biz.model.RegistryParam;
import com.ayit.scheduled.job.core.biz.model.ReturnT;

public interface AdminBiz {


    public ReturnT<String> registry(RegistryParam registryParam);


    public ReturnT<String> registryRemove(RegistryParam registryParam);


}
