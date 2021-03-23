package com.cn.psys.activity;


import com.cn.psys.tools.DateTimeUtils;
import net.sf.json.JSONObject;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.mgt.SessionContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 任务管理
 *
 * @Auther:
 * @Date:
 */
@RestController
@RequestMapping("tasks")
public class TasksController {

    @Resource
    private ProcessEngine processEngine;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;

    /**
     * 任务列表-待办事宜 flag=1  已办事宜 flag=0
     *
     * @Auther:
     * @Date:
     */
    @RequiresPermissions("Tasks:list")
    @RequestMapping("list")
    @ResponseBody
    public JSONObject list(@RequestParam(name = "sort", defaultValue = "1")  String sort, @RequestParam(name = "order", defaultValue = "1")  String order, @RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        JSONObject jsStr = JSONObject.fromObject(paramData); //将字符串{“id”：1}
        TaskService taskService = this.processEngine.getTaskService();

        String username = (String) SecurityUtils.getSubject().getPrincipal();
        String assignee = username;

        JSONObject jsonResult = new JSONObject();
        if("0".equals(jsStr.getString("flag"))){

            List<HistoricTaskInstance> list = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                    .createHistoricTaskInstanceQuery().orderByTaskCreateTime().desc()//创建历史任务实例查询
                    .taskAssignee(assignee)//指定历史任务的办理人
                    .listPage(page*rows-rows,rows);
            List<HistoricTaskInstance> list1 = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                    .createHistoricTaskInstanceQuery().orderByTaskCreateTime().desc()//创建历史任务实例查询
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
            jsonResult.put("total", list1.size());
            jsonResult.put("rows", obj.toString());
            return jsonResult;
        }else{
            List<Task> list = taskService.createTaskQuery().orderByTaskCreateTime().desc()
                  .taskAssignee(assignee)
                    .listPage(page*rows-rows,rows);
            List<Task> list1 = taskService.createTaskQuery().orderByTaskCreateTime().desc()
                 .taskAssignee(assignee)
                    .list();
            List<String> obj=new ArrayList<>();
            for (Task ml : list){
                JSONObject jo = new JSONObject();
                jo.put("id",ml.getId());
                jo.put("name",ml.getName());
                jo.put("taskDefinitionKey",ml.getTaskDefinitionKey());
                jo.put("processInstanceId",ml.getProcessInstanceId());
                jo.put("executionId",ml.getExecutionId());
                jo.put("createTime", DateTimeUtils.activityDateToString(ml.getCreateTime()));
                jo.put("assignee", ml.getAssignee());
                jo.put("formKey",ml.getFormKey());
                obj.add(jo.toString());
            }

            jsonResult.put("total", list1.size());
            jsonResult.put("rows", obj.toString());
            return jsonResult;
        }
    }
    @RequiresPermissions("Tasks:deleteByIds")
    @RequestMapping(value = "deleteByIds")
    @ResponseBody
    public String deleteByIds(String[] ids, HttpServletRequest request) {
        JSONObject result = new JSONObject();
        for (String id : ids) {
            taskService.deleteTask(id, true);
        }
        result.put("msg", "删除成功");
        result.put("type", "success");
        return result.toString();
    }

    /**
     *
     *    任务 撤回、跳转、回退
     *
     * @Auther:
     * @Date:
     */
    //@RequiresPermissions("Tasks:revoke")
    @RequestMapping("revoke")
    @ResponseBody
    public boolean revoke(@RequestParam(name = "objId", defaultValue = "1")  String objId) throws Exception {

        return action(objId,"testr","撤回");
    }



    /**
     *    processInstanceId
     *    type 撤回、跳转、回退
     *    sequenceFlowId 流程流向id
     *
     * @Auther:
     * @Date:
     */
    public boolean action(String processInstanceId,String sequenceFlowId,String type) throws Exception {

            System.out.println(processInstanceId);

        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId)
                //.processInstanceBusinessKey(objId)
                .singleResult();
        if(task==null) {
            System.out.println("System.out.println(objId)="+processInstanceId);
            throw new ServiceException("流程未启动或已执行完成，无法撤回");
        }
        String username = (String) SecurityUtils.getSubject().getPrincipal();
       // LoginUser loginUser = SessionContext.getLoginUser();
        List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
                //.processInstanceBusinessKey(objId)
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .asc()
                .list();
        String myTaskId = null;
        HistoricTaskInstance myTask = null;
        for(HistoricTaskInstance hti : htiList) {

            System.out.println(username +":"+ hti.getAssignee());

            if(username.equals(hti.getAssignee())) {
                myTaskId = hti.getId();
                myTask = hti;
                break;
            }
        }
        if(null==myTaskId) {
            throw new ServiceException("该任务非当前用户提交，无法撤回");
        }

        String processDefinitionId = myTask.getProcessDefinitionId();
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        //变量
//		Map<String, VariableInstance> variables = runtimeService.getVariableInstances(currentTask.getExecutionId());
        String myActivityId = null;
        List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery()
                .executionId(myTask.getExecutionId()).finished().list();
        for(HistoricActivityInstance hai : haiList) {
            if(myTaskId.equals(hai.getTaskId())) {
                myActivityId = hai.getActivityId();
                break;
            }
        }
        FlowNode myFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(myActivityId);


        Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        String activityId = execution.getActivityId();
        //logger.warn("------->> activityId:" + activityId);
        FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityId);

        //记录原活动方向
        List<SequenceFlow> oriSequenceFlows = new ArrayList<SequenceFlow>();
        oriSequenceFlows.addAll(flowNode.getOutgoingFlows());

        //清理活动方向
        flowNode.getOutgoingFlows().clear();
        //建立新方向
        List<SequenceFlow> newSequenceFlowList = new ArrayList<SequenceFlow>();
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId(sequenceFlowId);
        newSequenceFlow.setSourceFlowElement(flowNode);
        newSequenceFlow.setTargetFlowElement(myFlowNode);
        newSequenceFlowList.add(newSequenceFlow);
        flowNode.setOutgoingFlows(newSequenceFlowList);

        Authentication.setAuthenticatedUserId(username);
        taskService.addComment(task.getId(), task.getProcessInstanceId(), type);

        Map<String,Object> currentVariables = new HashMap<String,Object>();
        currentVariables.put("applier", username);
        currentVariables.put("type", type);
        //完成任务
        taskService.complete(task.getId(),currentVariables);
        //恢复原方向
        flowNode.setOutgoingFlows(oriSequenceFlows);

        return true;
    }


    public String getTaskVariabless(String taskId){
        List<HistoricVariableInstance> list = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询
                .taskId(taskId)
                .list();
        // JSONArray jsonArray = new JSONArray();
        String str = "";

        if(list != null && list.size()>0){
            for(HistoricVariableInstance hiv : list){
                //   System.out.println(hiv.getTaskId()+"  "+hiv.getVariableName()+"		"+hiv.getValue()+"		"+hiv.getVariableTypeName());
                //  JSONObject jo = new JSONObject();
                //  jo.put(hiv.getVariableName(),hiv.getValue());
                // jsonArray.add(jo);
                if(str==""){
                    str = "\""+hiv.getVariableName() +"\""+":"+ "\""+hiv.getValue()+"\"";
                }else{
                    str = str+ "," + "\""+hiv.getVariableName() +"\""+":"+ "\""+hiv.getValue()+"\"";
                }
                System.out.println(str);
            }
        }
        //{"title":"测","time":"13:13:10"}
        return "{"+str+"}";
    }

}
