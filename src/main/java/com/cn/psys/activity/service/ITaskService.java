package com.cn.psys.activity.service;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.task.Task;

import java.util.List;
import java.util.Map;

/**
 * 流程操作核心类<br>
 * 此核心类主要处理：流程通过、驳回、会签、转办、中止、挂起等核心操作<br>
 *
 * @author
 *
 */
public interface ITaskService extends IBaseService<Task> {


    /**
     * 审批通过(驳回直接跳回功能需后续扩展)
     *
     * @param taskId
     *            当前任务ID
     * @param variables
     *            流程存储参数
     * @throws Exception
     */
    void passProcess(String taskId, Map<String, Object> variables) throws Exception;

    /**
     * 驳回流程
     *
     * @param taskId
     *            当前任务ID
     * @param activityId
     *            驳回节点ID
     * @param variables
     *            流程存储参数
     * @throws Exception
     */
    void backProcess(String taskId, String activityId, Map<String, Object> variables) throws Exception;

    /**
     * 根据当前任务ID，查询可以驳回的任务节点
     *
     * @param taskId
     *            当前任务ID
     * @return
     */
    List<HistoricActivityInstance> findBackAvtivity(String taskId) throws Exception;



    /**
     * 转办流程
     *
     * @param taskId
     *            当前任务节点ID
     * @param userCode
     *            被转办人Code
     */
    void transferAssignee(String taskId, String userCode);

    /**
     * 流程转向操作
     *
     * @param taskId     当前任务ID
     * @param activityId 目标节点任务ID
   //  * @param variables  流程变量
     * @throws Exception
     */
    void turnTransition(String taskId, String activityId//, Map<String, Object> variables
                         ) throws Exception;


    /**
     * 获取所有节点信息
     *
     * @param processDefinitionId     流程定义ID processcj:14:262511
     * @throws Exception
     * @return
     */
     List<FlowElement> getFlowElements(String processDefinitionId);

}
