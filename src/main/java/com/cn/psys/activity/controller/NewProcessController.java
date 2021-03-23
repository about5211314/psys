package com.cn.psys.activity.controller;

import com.cn.psys.activity.service.IProcessService;
import com.cn.psys.base.BaseResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("process1")
public class NewProcessController {

    @Autowired(required = false)
    private IProcessService iProcessService;

    @RequiresPermissions("process:list")
    @RequestMapping("list")
    @ResponseBody
    public BaseResponse list(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "rows", defaultValue = "10") int rows, @RequestParam(required = false) String paramData) {

        return iProcessService.page(page, rows);
    }
}
