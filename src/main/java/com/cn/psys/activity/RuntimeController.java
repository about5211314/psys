package com.cn.psys.activity;

import com.alibaba.fastjson.JSON;
import com.cn.psys.base.BaseResponse;
import com.cn.psys.tools.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 运行接口
 *
 * @Auther:
 * @Date:
 */
@Slf4j
@RestController
@RequestMapping("runtime")
public class RuntimeController {

    @Resource
    private TaskService taskService;

    @Resource
    private FormService formService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private ProcessEngine processEngine;

    /**
     * 完成任务
     * <p>
     * 这两个是本节点的业务数据---增加到所有节点
     * dealReason: 处理原因
     * dealType: 处理类型
     * <p>
     * 这两个是本节点的业务数据---增加到本节点
     * dealUserId: 操作人id
     * dealUnitId: 操作人单位id
     * <p>
     * 注意顺序，先给值，后完成。
     *
     * @param taskId
     * @return
     */
    @RequiresPermissions("runtime:doTasks")
    @PostMapping(value = "tasks/do/{taskId}")
    public BaseResponse tasks(@PathVariable String taskId, @RequestBody Map<String, Object> params) {

        BaseResponse baseResponse = new BaseResponse(200,"success",null);

        boolean taskDo = true;
        if (null == params || params.isEmpty()) {
            taskService.complete(taskId);
            return baseResponse;
        }
        //驳回
        String dealType = (String) params.get("dealType");
        String dealReason = (String) params.get("dealReason");

        //System.out.println("dealType:"+dealType);
        //System.out.println("dealReason:"+dealReason);

        if ("不同意".equals(dealType)) {
            //获取驳回节点定义key
            try {
                String rejectElemKey = (String) params.get("rejectElemKey");
                if ("S00000".equals(rejectElemKey)) {
                    completeTasks(taskId, params);
                } else {
                    taskService.setVariableLocal(taskId, "dealUserId", params.get("dealUserId"));
                    taskService.setVariableLocal(taskId, "dealUnitId", params.get("dealUnitId"));
                    taskService.setVariable(taskId, "dealType", dealType);
                    taskService.setVariable(taskId, "dealReason", dealReason);
                }
                taskDo = rejected(taskId, rejectElemKey, dealReason);
            } catch (Exception e) {
                taskDo = false;
                log.error("驳回处理异常：{}", e);
            }
            //通过
        } else if ("同意".equals(dealType)) {
            completeTasks(taskId, params);
        }
        return baseResponse;
    }


    private void completeTasks(@PathVariable String taskId, @RequestBody Map<String, Object> params) {
        log.info("完成任务参数：taskId={} ,params={}", taskId, params);
        Map<String, Object> variables = new HashMap<>();
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        System.out.println(username);
        variables.put("userId", username);
        variables.put("dealUserId",username);
        //variables.put("dealUnitId",params.get("dealUnitId"));
        //放置业务数据
        for (Map.Entry<String, Object> m : params.entrySet()) {
            //System.out.println("key:" + m.getKey() + " value:" + m.getValue()+m.getValue().getClass());
            if("class java.util.ArrayList".equals(m.getValue().getClass().toString())){
                variables.put(m.getKey(), JSON.toJSON(m.getValue()));
            }else{
                variables.put(m.getKey(), m.getValue());
            }

        }

        //System.out.println(params);

        taskService.setVariablesLocal(taskId, variables);
        variables = new HashMap<>();
        //放置流程数据
        variables.put("dealType", params.get("dealType"));
        variables.put("dealReason", params.get("dealReason"));
        //System.out.println(params.get("userId").toString());
        variables.put("userId", params.get("userId").toString().replaceAll("[\\[\\]]",""));


        variables.put("next", params.get("next"));
        //variables.put("next", "1");
        //获取自定义表单id
        // TaskFormData formData = formService.getTaskFormData(taskId);
        // String formKey = formData.getFormKey();
        // System.out.println(formKey);

        taskService.complete(taskId, variables);
        log.info("完成任务：任务ID：" + taskId);
    }


    public boolean rejected(String taskId, String rejectElemKey, String dealReason) {
        int res = 0;
        //1.历史表
        //判断是否结束
        //    Map<String,Object> endEvent = historyMapper.selectEndEventByTaskId(taskId);
        //   log.info("查询hi_taskinst结束事件的结果，{}",endEvent);
        //   List<Map<String,Object>> hiTask=historyMapper.selectHiTaskByTaskId(taskId);
        //   String ruExcutionId=(String) hiTask.get(0).get("EXECUTION_ID_");
        //    String _processId=(String) hiTask.get(0).get("PROC_INST_ID_");

        //2.运行表
        //判断是驳回到原点：运行表ru_task，act_ru_identitylink，ru_variable，ru_execution清除节点信息
        if ("S00000".equals(rejectElemKey)) {
            // if (null==endEvent || endEvent.isEmpty()){
            //删variables
            //    res = runtimeMapper.deleteRuVariable(taskId);
            //   log.info("删ru_variables结束，{}",res);

            //删除当前的任务
            //不能删除当前正在执行的任务，所以要先清除掉关联
            //    TaskEntity currentTaskEntity = (TaskEntity)taskService.createTaskQuery()
            //            .processInstanceId(_processId).singleResult();
            //    currentTaskEntity.setExecutionId(null);
            //    taskService.saveTask(currentTaskEntity);
            //    taskService.deleteTask(currentTaskEntity.getId(), true);
            //    log.info("删ru_task结束，{}",currentTaskEntity);

            //删execution
            //   res = runtimeMapper.deleteRuExecution(taskId);
            //  log.info("删ru_execution结束，{}",res);

            //删identitylink
            //  res = runtimeMapper.deleteRuIdentity(taskId);
            //  log.info("删ru_identitylink结束，{}",res);

            //    }else {
            //结束了，act_hi_actinst删掉结束event
            //   res = historyMapper.deleteHiEndEvent(taskId);
            //   log.info("删掉hi_actinst中endEvent结束，{}",res);
            //    }
        } else {
            //判断是驳回到节点：运行表ru_task，ru_execution更改节点信息
            //   jumpEndActivity(ruExcutionId,rejectElemKey,dealReason);


        }
        return true;
    }


    @ResponseBody
    @RequestMapping("/queryTask")
    public String queryTask(@RequestBody Map<String, Object> params) {
        TaskService taskService = this.processEngine.getTaskService();
        String assignee = (String) params.get("assignee");
        // String assignee="朱伟";
        List<Task> list = taskService.createTaskQuery()
                //条件
                //              .taskAssignee(assignee) //根据任务的办理人查询
//              .deploymentId(deploymentId)根据部署ID查询任务
//              .deploymentIdIn(deploymentIds)根据部署ID的集合查询
//              .executionId(executionId)根据执行实例查询
//              .processInstanceId(processInstanceId)//根据流程实例ID查询
//              .processInstanceBusinessKey(processInstanceBusinessKey)//根据业务ID查询
//              .processDefinitionId(processDefinitionId)//根据流程定义ID查询
//              .processDefinitionKey(processDefinitionKey)根据流程定义的KEY查询
//              .processDefinitionKeyIn(processDefinitionKeys)
//              .processDefinitionKeyLike(processDefinitionKeyLike)
//              .processDefinitionKeyLikeIgnoreCase(processDefinitionKeyLikeIgnoreCase)
//              .processDefinitionName(processDefinitionName)
                //排序
//              .orderByExecutionId()
//              .orderByProcessDefinitionId()
//              .orderByProcessInstanceId()
                //结果集
                .list();
        JSONArray jsonArray = new JSONArray();
        for (Task task : list) {
            JSONObject jo = new JSONObject();
            jo.put("getId", task.getId());
            jo.put("getExecutionId", task.getExecutionId());
            jo.put("getProcessInstanceId", task.getProcessInstanceId());
            jo.put("getName", task.getName());
            jo.put("getTaskDefinitionKey", task.getTaskDefinitionKey());
            jo.put("getAssignee", task.getAssignee());


          //  Date time =new Date(String.valueOf(task.getCreateTime()));  activiti 时间格式转换
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String timeFormat = sdf.format(task.getCreateTime());


            jo.put("getCreateTime", timeFormat);
            jo.put("getDueDate", task.getDueDate());
            jsonArray.add(jo);
        }
        return jsonArray.toString();
    }


    /**
     * 我的待办任务
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/tasks/ing")
    public Object myTasks(@RequestParam("userId") String userId) {
        List<Map<String, Object>> list = new ArrayList<>();
//        List<Map<String,Object>> tasks = runtimeInfoService.myTasks(userId);
//        if (!CollectionUtils.isEmpty(tasks)){
//            for (Map<String,Object> task:tasks){
//                Map<String, Object> variables = taskService.getVariables((String) task.get("ID_"));
//                task.putAll(variables);
//                list.add(task);
//            }
//        }
        return list;
        /*//节点指定的人
        List<RuTask> list = new ArrayList<>();
        List<Task> listTask = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime().asc()
                .active()
                .list();
        if (listTask != null && listTask.size() > 0) {
            for (Task task : listTask) {
                list.add(new RuTask(task));
                log.info("流程定义的ID："+task.getProcessDefinitionId());
                log.info("流程实例的ID："+task.getProcessInstanceId());
                log.info("执行对象ID："+task.getExecutionId());
                log.info("任务ID："+task.getId());
                log.info("任务名称："+task.getName());
                log.info("任务创建的时间："+task.getCreateTime());
                log.info("================================");
            }
        }

        //节点指定的组
        //先查出人的角色
        List<String> groupIds = authorizationService.selectRoleIdsByUserId(userId);
        if (!CollectionUtils.isEmpty(groupIds)){
            List<Task> lists = taskService.createTaskQuery()
                    .taskCandidateGroupIn(groupIds)
                    .orderByTaskCreateTime().asc()
                    .active()
                    .list();
            if (lists != null && lists.size() > 0) {
                for (Task task : lists) {
                    list.add(new RuTask(task));
                    log.info("G流程定义的ID："+task.getProcessDefinitionId());
                    log.info("G流程实例的ID："+task.getProcessInstanceId());
                    log.info("G执行对象ID："+task.getExecutionId());
                    log.info("G任务ID："+task.getId());
                    log.info("G任务名称："+task.getName());
                    log.info("G任务创建的时间："+task.getCreateTime());
                    log.info("G================================");
                }
            }
        }
        return list;*/
    }


    /**
     * 我的待办任务
     *
     * @param
     * @return
     */
    @RequiresPermissions("runtime:exeTasks")
    @ResponseBody
    @RequestMapping("/exeTasks")
    public BaseResponse exeTasks(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        TaskService taskService = this.processEngine.getTaskService();
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        String assignee = username;
        List<Task> list = taskService.createTaskQuery().taskAssignee(assignee).listPage(page,rows);
        List<Task> list1 = taskService.createTaskQuery().taskAssignee(assignee).list();
        List<String> obj=new ArrayList<>();
        for (Task ml : list){
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




}
