package com.ayit.scheduled.job.admin.core.thread;

import com.ayit.scheduled.job.admin.core.conf.XxlJobAdminConfig;
import com.ayit.scheduled.job.core.biz.model.RegistryParam;
import com.ayit.scheduled.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.*;

public class JobRegistryHelper {

	private static Logger logger = LoggerFactory.getLogger(JobRegistryHelper.class);
	private static JobRegistryHelper instance = new JobRegistryHelper();
	public static JobRegistryHelper getInstance(){
		return instance;
	}


	private ThreadPoolExecutor registryOrRemoveThreadPool = null;


	private volatile boolean toStop = false;

	public void start(){
		registryOrRemoveThreadPool = new ThreadPoolExecutor(
				2,
				10,
				30L,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(2000),
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "xxl-job, admin JobRegistryMonitorHelper-registryOrRemoveThreadPool-" + r.hashCode());
					}
				},
				new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						r.run();
						logger.warn(">>>>>>>>>>> xxl-job, registry or remove too fast, match threadpool rejected handler(run now).");
					}
				});
	}


	public void toStop(){
		toStop = true;
		registryOrRemoveThreadPool.shutdownNow();

	}
	public ReturnT<String> registry(RegistryParam registryParam) {
		//校验处理
		if (!StringUtils.hasText(registryParam.getRegistryGroup())
				|| !StringUtils.hasText(registryParam.getRegistryKey())
				|| !StringUtils.hasText(registryParam.getRegistryValue())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "Illegal Argument.");
		}
		//提交注册执行器的任务给线程池执行
		registryOrRemoveThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				//这里的意思也很简单，就是先根据registryParam参数去数据库中更新相应的数据
				//如果返回的是0，说明数据库中没有相应的信息，该执行器还没注册到注册中心呢，所以下面
				//就可以直接新增这一条数据即可
				int ret = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().registryUpdate(registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue(), new Date());
				if (ret < 1) {
					//这里就是数据库中没有相应数据，直接新增即可
					XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().registrySave(registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue(), new Date());
					//该方法从名字上看是刷新注册表信息的意思
					//但是作者还没有实现，源码中就是空的，所以这里我就照搬过来了
					freshGroupRegistryInfo(registryParam);
				}
			}
		});
		return ReturnT.SUCCESS;
	}

	private void freshGroupRegistryInfo(RegistryParam registryParam){
	}




}
