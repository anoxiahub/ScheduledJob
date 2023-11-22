package com.ayit.scheduled.job.core.handler;
public abstract class IJobHandler {


	public abstract void execute() throws Exception;


	public void init() throws Exception {

	}


	public void destroy() throws Exception {

	}


}
