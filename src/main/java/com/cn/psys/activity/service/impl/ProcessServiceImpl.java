package com.cn.psys.activity.service.impl;

import com.cn.psys.activity.ActivityTools;
import com.cn.psys.activity.service.IProcessService;
import com.cn.psys.base.BaseResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessServiceImpl extends BaseServiceImpl implements IProcessService {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ProcessEngine processEngine;
    @Override
    public boolean save(ProcessInstance entity) {
        try {
            Model modelData = repositoryService.getModel(entity.getId());
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            if (bytes == null) {
                //logger.info("部署ID:{}的模型数据为空，请先设计流程并成功保存，再进行发布", modelId);

            }
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addBpmnModel(modelData.getKey() + ".bpmn20.xml", model)
                    .deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
            return true;


        } catch (Exception e) {
           // logger.info("部署modelId:{}模型服务异常：{}", modelId, e);

        }
        //logger.info("流程部署出参map：{}", map);
        return false;
    }

    @Override
    public boolean update(ProcessInstance entity) {
        return false;
    }

    @Override
    public boolean remove(String[] ids) {
        for (String id : ids) {

            Model modelData = repositoryService.getModel(id);

            if (null != modelData) {
                try {
                    ProcessInstance pi = runtimeService.createProcessInstanceQuery().processDefinitionKey(modelData.getKey()).singleResult();
                    if (null != pi) {
                        runtimeService.deleteProcessInstance(pi.getId(), "");
                        historyService.deleteHistoricProcessInstance(pi.getId());
                    }
                } catch (Exception e) {
                    //logger.error("删除流程实例服务异常：{}", e);
                }
            }

            return true;

        }
        return false;
    }

    @Override
    public BaseResponse page(int page, int rows) {
        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
                .orderByProcessDefinitionVersion().latestVersion().asc()//使用流程定义的版本升序排列
                .listPage(page, rows);

        List<String> obj = new ArrayList<>();

        for (ProcessDefinition processDefinition : list) {
            JSONObject jo = new JSONObject();
            jo.put("id",processDefinition.getId());
            jo.put("name",processDefinition.getName());
            jo.put("deploymentId",processDefinition.getDeploymentId());
            jo.put("key",processDefinition.getKey());
            jo.put("version",processDefinition.getVersion());
            jo.put("resourceName", processDefinition.getResourceName());
            jo.put("diagramResourceName", processDefinition.getDiagramResourceName());
            obj.add(jo.toString());

        }
        List<ProcessDefinition> list1 = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
                .orderByProcessDefinitionVersion().latestVersion().asc()//使用流程定义的版本升序排列
                .list();

        ActivityTools activityTools = new ActivityTools();
        BaseResponse baseResponse = new BaseResponse(200, "success", activityTools.list(page,rows,list1.size(),obj));
        return baseResponse;
    }

    @Override
    public boolean revokePublish(String[] ids) {
        for (String id : ids) {
            Model modelData = repositoryService.getModel(id);
            if (null != modelData) {
                try {
                    /**
                     * 参数不加true:为普通删除，如果当前规则下有正在执行的流程，则抛异常
                     * 参数加true:为级联删除,会删除和当前规则相关的所有信息，包括历史
                     */
                    repositoryService.deleteDeployment(modelData.getDeploymentId(), true);
                } catch (Exception e) {
                    //logger.error("撤销已部署流程服务异常：{}", e);
                }
            }
            return true;
            //logger.info("撤销发布流程出参map：{}", map);
        }
        return false;
    }
}
