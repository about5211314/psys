package com.cn.psys.activity;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.psys.base.BaseResponse;
import com.cn.psys.mybatisplus.MybatisPlusQuery;
import com.cn.psys.system.entity.User;
import com.cn.psys.tools.DateTimeUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 历史管理
 *
 * @Auther:
 * @Date:
 */
@RestController
@RequestMapping("history")
public class HistoryController {


    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;

    @Resource
    private HistoryService historyService;

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private FormService formService;


    /**
     *
     * 查询历史流程实例   未完成
     *
     * @auther:
     * @date:
     */
    @RequiresPermissions("history:findHistoryProcessInstance")
    @ResponseBody
    @RequestMapping("/findHistoryProcessInstance")
    public BaseResponse findHistoryProcessInstance(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        HistoryService historyService = this.processEngine.getHistoryService();
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().unfinished().listPage(page,rows);
        List<HistoricProcessInstance> list1 = historyService.createHistoricProcessInstanceQuery().unfinished().list();

        List<String> obj=new ArrayList<>();
        for (HistoricProcessInstance ml : list){
            JSONObject jo = new JSONObject();
            jo.put("id",ml.getId());
            jo.put("name",ml.getName());
            jo.put("processDefinitionId",ml.getProcessDefinitionId());
            jo.put("deploymentId",ml.getDeploymentId());
            jo.put("processDefinitionName",ml.getProcessDefinitionName());
            jo.put("processDefinitionVersion",ml.getProcessDefinitionVersion());
            jo.put("startTime", DateTimeUtils.activityDateToString(ml.getStartTime()));
            obj.add(jo.toString());
        }
        ActivityTools activityTools = new ActivityTools();
        BaseResponse baseResponse = new BaseResponse(200,"success",activityTools.list(page,rows,list1.size(),obj));


        return baseResponse;
    }

    /**
     *
     * 查询历史流程实例  已完成
     *
     * @auther:historyTaskList
     * @date:
     */
    @RequiresPermissions("history:findHistoryProcessInstanceEnd")
    @ResponseBody
    @RequestMapping("/findHistoryProcessInstanceEnd")
    public BaseResponse findHistoryProcessInstanceEnd(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        HistoryService historyService = this.processEngine.getHistoryService();
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().finished().listPage(page,rows);
        List<HistoricProcessInstance> list1 = historyService.createHistoricProcessInstanceQuery().finished().list();
        List<String> obj=new ArrayList<>();
        for (HistoricProcessInstance ml : list){
            JSONObject jo = new JSONObject();
            jo.put("id",ml.getId());
            jo.put("name",ml.getName());
            jo.put("processDefinitionId",ml.getProcessDefinitionId());
            jo.put("deploymentId",ml.getDeploymentId());
            jo.put("processDefinitionName",ml.getProcessDefinitionName());
            jo.put("processDefinitionVersion",ml.getProcessDefinitionVersion());
            jo.put("startTime", DateTimeUtils.activityDateToString(ml.getStartTime()));
            jo.put("endTime", DateTimeUtils.activityDateToString(ml.getEndTime()));
            jo.put("description",ml.getDescription());
            obj.add(jo.toString());
        }
        ActivityTools activityTools = new ActivityTools();
        BaseResponse baseResponse = new BaseResponse(200,"success",activityTools.list(page,rows,list1.size(),obj));
        return baseResponse;

    }
    /**查询历史任务   按办理人*/
    @RequiresPermissions("history:findHistoryTask")
    @ResponseBody
    @RequestMapping("/findHistoryTask")
    public BaseResponse findHistoryTask(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        String assignee = username;

        List<HistoricTaskInstance> list = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskAssignee(assignee)//指定历史任务的办理人
                .listPage(page,rows);
        List<HistoricTaskInstance> list1 = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskAssignee(assignee)//指定历史任务的办理人
                .list();

        List<String> obj=new ArrayList<>();
        for (HistoricTaskInstance ml : list){
            JSONObject jo = new JSONObject();
            jo.put("id",ml.getId());
            jo.put("name",ml.getName());
            jo.put("taskDefinitionKey",ml.getTaskDefinitionKey());
            jo.put("processInstanceId",ml.getProcessInstanceId());
            jo.put("executionId",ml.getExecutionId());
            jo.put("createTime",DateTimeUtils.activityDateToString(ml.getCreateTime()));
            jo.put("assignee", ml.getAssignee());
            obj.add(jo.toString());
        }
        ActivityTools activityTools = new ActivityTools();
        BaseResponse baseResponse = new BaseResponse(200,"success",activityTools.list(page,rows,list1.size(),obj));
        return baseResponse;
    }

    /**历史活动查询   流程实例id
     *
     *
     * 历史活动包括所有节点（上图的圆圈）和任务（上图的矩形） 而历史任务只包含任务
     *
     *
     */
    @ResponseBody
    @RequestMapping("/historyActInstanceList")
    public String historyActInstanceList(@RequestBody Map<String, Object> params){

        List<HistoricActivityInstance>  list=processEngine.getHistoryService() // 历史相关Service
                .createHistoricActivityInstanceQuery() // 创建历史活动实例查询
                .processInstanceId((String) params.get("processInstanceId")) // 执行流程实例id
                .finished()
                .list();
        for(HistoricActivityInstance hai:list){
            //getTaskVariabless(hai.getId());
//            System.out.println("活动ID:"+hai.getId());
//            System.out.println("流程实例ID:"+hai.getProcessInstanceId());
//          //  this.historyProcessVariables(hai.getProcessInstanceId());
//            System.out.println("活动名称："+hai.getActivityName());
//            System.out.println("办理人："+hai.getAssignee());
//            System.out.println("开始时间："+hai.getStartTime());
//            System.out.println("结束时间："+hai.getEndTime());
//            System.out.println("=================================");
        }

        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
        JSONArray json = JSONArray.fromObject(list, jsonConfig);
        return json.toString();

    }
    /**历史任务查询   流程实例id
     *
     *
     * 历史活动包括所有节点（上图的圆圈）和任务（上图的矩形） 而历史任务只包含任务
     *
     * */
    @ResponseBody
    @RequiresPermissions("history:TaskList")
    @RequestMapping("/historyTaskList")
    public String historyTaskList(@RequestBody Map<String, Object> params){
        List<HistoricTaskInstance> list=processEngine.getHistoryService() // 历史相关Service
                .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                .processInstanceId((String) params.get("processInstanceId")) // 用流程实例id查询
                .finished().orderByTaskCreateTime().desc() // 查询已经完成的任务
                .list();
//        for(HistoricTaskInstance hti:list){
//            System.out.println("活动ID:"+hti.getId());
//            System.out.println("流程实例ID:"+hti.getProcessInstanceId());
//           // this.historyProcessVariables(hti.getProcessInstanceId());
//            System.out.println("活动名称："+hti.getName());
//            System.out.println("办理人："+hti.getAssignee());
//            System.out.println("开始时间："+DateTimeUtils.activityDateToString(hti.getStartTime()));
//            System.out.println("结束时间："+hti.getEndTime());
//            System.out.println("=================================");
//        }


        List<String> obj=new ArrayList<>();
        for (HistoricTaskInstance ml : list){
            JSONObject jo = new JSONObject();
            jo.put("id",ml.getId());
           // getTaskVariabless(ml.getId());
            jo.put("name",ml.getName());
            jo.put("processInstanceId",ml.getProcessInstanceId());
            jo.put("createTime",DateTimeUtils.activityDateToString(ml.getCreateTime()));
            jo.put("endTime",DateTimeUtils.activityDateToString(ml.getEndTime()));
            jo.put("assignee", ml.getAssignee());
            jo.put("formKey", ml.getFormKey());

            //ml.getProcessVariables()
            obj.add(jo.toString());
        }


        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
        JSONArray json = JSONArray.fromObject(obj, jsonConfig);
        return json.toString();
    }


    /**查询历史的流程变量*/
    @ResponseBody
    @RequestMapping("/historyProcessVariables")
    public String historyProcessVariables(String taskId){
        List<HistoricVariableInstance> list = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询
              //  .taskId(taskId)
              //  .executionId(taskId)
                .processInstanceId(taskId)
                .list();
       // System.out.println(taskId);
        if(list != null && list.size()>0){
            for(HistoricVariableInstance hiv : list){
               // System.out.println(hiv.getTaskId()+"  "+hiv.getVariableName()+"		"+hiv.getValue()+"		"+hiv.getVariableTypeName());
            }
        }
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
        JSONArray json = JSONArray.fromObject(list, jsonConfig);
        return json.toString();
    }


    /**查询历史的流程变量
     * @return*/
    @ResponseBody
    @RequestMapping("/historyProcessVariabless")
    public Map historyProcessVariabless(String taskId){
        List<HistoricVariableInstance> list = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询
                .taskId(taskId)
                .list();
        Map m1 = new HashMap();
        if(list != null && list.size()>0){
            for(HistoricVariableInstance hiv : list){
                //System.out.println(hiv.getVariableTypeName());
                m1.put(hiv.getVariableName(),hiv.getValue());
            }
        }
        return m1;
    }
}
