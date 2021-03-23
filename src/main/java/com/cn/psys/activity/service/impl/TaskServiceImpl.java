package com.cn.psys.activity.service.impl;

import com.cn.psys.activity.service.ITaskService;
import com.cn.psys.base.BaseResponse;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.*;

/**
 * 流程操作核心类<br>
 * 此核心类主要处理：流程通过、驳回、会签、转办、中止、挂起等核心操作<br>
 *
 * @author
 */
@Service
public class TaskServiceImpl extends BaseServiceImpl implements ITaskService {
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ProcessEngine processEngine;
    @Resource
    private TaskService taskService;


    /**
     * 根据当前任务ID，查询可以驳回的任务节点
     *
     * @param taskId 当前任务ID
     * @return
     */
    @Override
    public List<HistoricActivityInstance> findBackAvtivity(String taskId) throws Exception {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery().activityType("userTask")
                .executionId(task.getExecutionId()).finished().list();
        for(HistoricActivityInstance hai : haiList) {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(hai.getProcessDefinitionId());
            FlowNode myFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(hai.getActivityId());
            getSequenceFlow(myFlowNode);


            // getFlowElements(hai.getProcessDefinitionId());
//            System.out.println(hai.getActivityId() + hai.getActivityName() + hai.getActivityName()+ hai.getActivityType()+hai.getProcessDefinitionId());
//            //if(task.equals(hai.getTaskId())) {
//             //   myActivityId = hai.getActivityId();
//            //    break;
//            //}
        }
        return haiList;
    }


    /**
     * 审批通过(驳回直接跳回功能需后续扩展)
     *
     * @param taskId    当前任务ID
     * @param variables 流程存储参数
     * @throws Exception
     */
    public void passProcess(String taskId, Map<String, Object> variables)
            throws Exception {
        List<Task> tasks = taskService.createTaskQuery().taskId(taskId).list();
        for (Task task : tasks) {// 级联结束本节点发起的会签任务
            commitProcess(task.getId(), variables, null);
        }
        commitProcess(taskId, variables, null);
    }

    /**
     * 驳回流程
     *
     * @param taskId     当前任务ID
     * @param activityId 驳回节点ID
     * @param variables  流程存储参数
     * @throws Exception
     */
    public void backProcess(String taskId, String activityId,
                            Map<String, Object> variables) throws Exception {

        // 查找所有并行任务节点，同时驳回
        List<Task> taskList = findTaskListByKey(findProcessInstanceByTaskId(
                taskId).getId(), findTaskById(taskId).getTaskDefinitionKey());
        for (Task task : taskList) {
            commitProcess(task.getId(), variables, activityId);
        }

    }


    /**
     * 取回流程
     *
     * @param taskId     当前任务ID
     * @param activityId 取回节点ID
     * @throws Exception
     */
    public void callBackProcess(String taskId, String activityId)
            throws Exception {
        // 查找所有并行任务节点，同时取回
        List<Task> taskList = findTaskListByKey(findProcessInstanceByTaskId(
                taskId).getId(), findTaskById(taskId).getTaskDefinitionKey());
        for (Task task : taskList) {
            commitProcess(task.getId(), null, activityId);
        }

    }


    /**
     * 中止流程(特权人直接审批通过等)
     *
     * @param taskId
     */
    public void endProcess(String taskId) throws Exception {

    }


    /**
     * 会签操作
     *
     * @param taskId    当前任务ID
     * @param userCodes 会签人账号集合
     * @throws Exception
     */
    public void jointProcess(String taskId, List<String> userCodes)
            throws Exception {

    }

    /**
     * 转办流程
     *
     * @param taskId   当前任务节点ID
     * @param userCode 被转办人Code
     */
    public void transferAssignee(String taskId, String userCode) {
        taskService.setAssignee(taskId, userCode);
    }


    /**
     * ***********以下为流程会签操作核心逻辑
     */

    /**
     * **********以上为流程会签操作核心逻辑
     */

    /**
     * **以下为流程转向操作核心逻辑*
     */


    /**
     * @param taskId     当前任务ID
     * @param variables  流程变量
     * @param activityId 流程转向执行任务节点ID<br>
     *                   此参数为空，默认为提交操作
     * @throws Exception
     */
    private void commitProcess(String taskId, Map<String, Object> variables,
                               String activityId) throws Exception {
        if (variables == null) {
            variables = new HashMap<String, Object>();
        }

        // 跳转节点为空，默认提交操作
        if ("null".equals(activityId)) {
            taskService.complete(taskId, variables);
        } else {// 流程转向操作
            //turnTransition(taskId, activityId, variables);
        }


    }

    /**
     * 清空指定活动节点流向
     *
     * @param
     * @return 节点流向集合
     */
    private List<SequenceFlow> clearTransition(FlowNode flowNode) {
        // 存储当前节点所有流向临时变量
        List<SequenceFlow> oriSequenceFlows = new ArrayList<SequenceFlow>();

        // 获取当前节点所有流向，存储到临时变量，然后清空
//        List<SequenceFlow> pvmSequenceFlows = flowNode.getOutgoingFlows();
//
//        for(SequenceFlow pvmSequenceFlow :pvmSequenceFlows){
//            oriSequenceFlows.add(pvmSequenceFlow);
//        }
//        pvmSequenceFlows.clear();
        oriSequenceFlows.addAll(flowNode.getOutgoingFlows());
        flowNode.getOutgoingFlows().clear();

        return oriSequenceFlows;

    }


    /**
     * 还原指定活动节点流向
     *
     * @param
     * @param
     */
    public void restoreTransition(FlowNode flowNode, List<SequenceFlow> oriSequenceFlows) {
            // 清空现有流向
            List<SequenceFlow> pvmSequenceFlows = flowNode.getOutgoingFlows();
            pvmSequenceFlows.clear();
            // 还原以前流向
            for (SequenceFlow pvmSequenceFlow : oriSequenceFlows) {
                pvmSequenceFlows.add(pvmSequenceFlow);
            }
        }

    /**
     * 流程转向操作
     *
     * @param taskId     当前任务ID
     * @param activityId 目标节点任务ID
    // * @param variables  流程变量
     * @throws Exception
     */
    @Override
    public void turnTransition(String taskId, String activityId//, Map<String, Object> variables
                                ) {
        try{
            // 当前节点
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            String processDefinitionId = task.getProcessDefinitionId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
            FlowNode myFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(execution.getActivityId());
            // 清空当前流向
            List<SequenceFlow> oriSequenceFlows = clearTransition(myFlowNode);
            // 目标节点
            FlowNode reFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityId);
            //获取目标节点的所有来向
            List<SequenceFlow> SequenceFlows = getSequenceFlow(reFlowNode);
            String newSequenceFlowId = null;
            for(SequenceFlow sf : SequenceFlows){
                newSequenceFlowId = sf.getId();
            }

            // 创建新流向
            List<SequenceFlow> newSequenceFlowList = new ArrayList<SequenceFlow>();
            SequenceFlow newSequenceFlow = new SequenceFlow();
            newSequenceFlow.setId(newSequenceFlowId);
            newSequenceFlow.setSourceFlowElement(myFlowNode);
            newSequenceFlow.setTargetFlowElement(reFlowNode);
            newSequenceFlowList.add(newSequenceFlow);
            myFlowNode.setOutgoingFlows(newSequenceFlowList);
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "跳转");
            Map<String,Object> currentVariables = new HashMap<String,Object>();
            //currentVariables.put("applier", username);
            //完成任务
            taskService.complete(task.getId(),currentVariables);
            //恢复原方向
            myFlowNode.setOutgoingFlows(oriSequenceFlows);
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }


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

    /**
     * 获取所有节点信息
     *
     * @param processDefinitionId     流程定义ID processcj:14:262511
     * @throws Exception
     * @return
     */
    @Override
    public List<FlowElement> getFlowElements(String processDefinitionId){

        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        Collection<FlowElement> flowElements = null;
        List<FlowElement> tempElements = null;
        if(model != null) {
            flowElements = model.getMainProcess().getFlowElements();
            for(FlowElement e : flowElements) {
                //对比下已经走过的任务，获取可退回的节点清单
               // if (e.getClass().toString() =="class org.activiti.bpmn.model.UserTask"){
                    tempElements.add(e);
                    System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
               // }
                //System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
            }
        }
        return (List<FlowElement>) flowElements;
    }

    /**
     * 获取节点的所有来源线
     *
     * @param flowElement    节点对象
     * @throws Exception
     */
    private List<SequenceFlow> getSequenceFlow(FlowElement flowElement){
        //获取节点定义
        FlowNode flowNode = (FlowNode) flowElement;
        //获取节点定义的来源线
        List<SequenceFlow> flows = flowNode.getIncomingFlows();
        if (flows == null || flows.size() < 1) {

        }else {
            for(SequenceFlow sequenceFlow : flows){
                System.out.println(sequenceFlow.getId()+sequenceFlow.getName());
            }

            return flows ;
        }
        return flows;
    }



    /**
     * 根据流程实例ID和任务key值查询所有同级任务集合
     *
     * @param processInstanceId
     * @param key
     * @return
     */
    private List<Task> findTaskListByKey(String processInstanceId, String key) {
        return taskService.createTaskQuery().processInstanceId(
                processInstanceId).taskDefinitionKey(key).list();
    }

    /**
     * 根据任务ID获取对应的流程实例
     *
     * @param taskId
     *            任务ID
     * @return
     * @throws Exception
     */
    private ProcessInstance findProcessInstanceByTaskId(String taskId)
            throws Exception {
        // 找到流程实例
        ProcessInstance processInstance = runtimeService
                .createProcessInstanceQuery().processInstanceId(
                        findTaskById(taskId).getProcessInstanceId())
                .singleResult();
        if (processInstance == null) {
            throw new Exception("流程实例未找到!");
        }
        return processInstance;
    }

    /**
     * 根据任务ID获得任务实例
     *
     * @param taskId
     *            任务ID
     * @return
     * @throws Exception
     */
    private TaskEntity findTaskById(String taskId) throws Exception {
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(
                taskId).singleResult();
        if (task == null) {
            throw new Exception("任务实例未找到!");
        }
        return task;
    }
}
