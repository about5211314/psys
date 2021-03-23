package com.cn.psys.activity.controller;

import com.cn.psys.activity.service.ITaskService;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.history.HistoricActivityInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("task")
public class NewTaskController {

    @Autowired(required = false)
    private ITaskService iTaskService;


    @RequestMapping("elements")
    @ResponseBody
    public List<FlowElement> getFlowElements(@RequestParam(name = "processDefinitionId")  String processDefinitionId){
        return iTaskService.getFlowElements(processDefinitionId);
    }


    @RequestMapping("backavtivity")
    @ResponseBody
    public List<HistoricActivityInstance> getBackAvtivity() throws Exception {
        return iTaskService.findBackAvtivity("267546");
    }

    @RequestMapping("turnTransition")
    @ResponseBody
    public void turnTransition(@RequestParam(name = "taskId")  String taskId,@RequestParam(name = "activityId")  String activityId //,@RequestBody Map<String, Object> params
    ) throws Exception {
        iTaskService.turnTransition(taskId,activityId);
    }
}
