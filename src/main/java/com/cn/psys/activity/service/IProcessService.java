package com.cn.psys.activity.service;

import org.activiti.engine.runtime.ProcessInstance;

public interface IProcessService extends IBaseService<ProcessInstance> {
    boolean revokePublish(String[] ids);
}
