package com.cn.psys.activity.service.impl;

import com.cn.psys.activity.service.IRuntimeService;
import com.cn.psys.base.BaseResponse;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Service;

@Service
public class RuntimeSeriveImpl extends BaseServiceImpl implements IRuntimeService {
    @Override
    public boolean save(Task entity) {
        return false;
    }

    @Override
    public boolean update(Task entity) {
        return false;
    }

    @Override
    public boolean remove(String[] ids) {
        return false;
    }

    @Override
    public BaseResponse page(int page, int rows) {
        return null;
    }
}
