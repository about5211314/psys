package com.cn.psys.activity.controller;

import com.cn.psys.activity.service.IHistoryService;
import com.cn.psys.base.BaseResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("history1")
public class NewHistoryController {
    @Autowired(required = false)
    private IHistoryService iHistoryService;


    /**
     *
     * 查询历史流程实例   未完成
     *
     * @auther:
     * @date:
     */
    @RequiresPermissions("history:findHistoryProcessInstance")
    @RequestMapping("findHistoryProcessInstance")
    @ResponseBody
    public BaseResponse findHistoryProcessInstance(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "rows", defaultValue = "10") int rows, @RequestParam(required = false) String paramData) {

        return null;
    }
    /**
     *
     * 查询历史流程实例  已完成
     *
     * @auther:
     * @date:
     */
    @RequiresPermissions("history:findHistoryProcessInstanceEnd")
    @ResponseBody
    @RequestMapping("/findHistoryProcessInstanceEnd")
    public BaseResponse findHistoryProcessInstanceEnd(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        return null;
    }
    /**
     *
     * 查询历史任务(已办事宜)
     *
     *
     *
     *按办理人
     */
    @RequiresPermissions("history:findHistoryTask")
    @ResponseBody
    @RequestMapping("/findHistoryTask")
    public BaseResponse findHistoryTask(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {

        return null;
    }

}
