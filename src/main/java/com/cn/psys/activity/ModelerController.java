package com.cn.psys.activity;

import com.cn.psys.activity.service.IModelerService;
import com.cn.psys.base.BaseResponse;
import com.cn.psys.tools.DateTimeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程控制器
 * liuzhize 2019年3月7日下午3:28:14
 */
@SuppressWarnings("ALL")
@Controller
@Slf4j
@RequestMapping("activiti")
public class ModelerController {

    private static final Logger logger = LoggerFactory.getLogger(ModelerController.class);

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Autowired
    private ProcessEngine processEngine;

    @Autowired(required=false)
//    @Qualifier("ModelerServiceImpl")
 //   @Resource
    private IModelerService iModelerService;

    @RequiresPermissions("Modeler:add")
    @GetMapping("activiti")
    public void index(HttpServletResponse response) {

        try {
            response.sendRedirect("/create");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 跳转编辑器页面
     *
     * @return
     */
    @RequiresPermissions("Modeler:update")
    @GetMapping("editor")
    public String editor() {
        return "modeler";
    }


    /**
     * 创建模型
     *
     * @param response
     * @throws IOException
     */
    @RequiresPermissions("Modeler:add")
    @RequestMapping("/create")
    public void create(HttpServletResponse response, String name, String key) throws IOException {
        logger.info("创建模型入参name：{},key:{}", name, key);


        Model model = repositoryService.newModel();
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "");
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);
        createObjectNode(model.getId());
        response.sendRedirect("/activiti/editor?modelId=" + model.getId());
        logger.info("创建模型结束，返回模型ID：{}", model.getId());
    }

    /**
     * 创建模型时完善ModelEditorSource
     *
     * @param modelId
     */
    @SuppressWarnings("deprecation")
    private void createObjectNode(String modelId) {
        logger.info("创建模型完善ModelEditorSource入参模型ID：{}", modelId);
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        try {
            repositoryService.addModelEditorSource(modelId, editorNode.toString().getBytes("utf-8"));
        } catch (Exception e) {
            logger.info("创建模型时完善ModelEditorSource服务异常：{}", e);
        }
        logger.info("创建模型完善ModelEditorSource结束");
    }

    //获取模型清单
    @RequestMapping("/selectAll")
    @ResponseBody
    public String selectAll() {
        List<Model> resultList = repositoryService.createModelQuery().orderByCreateTime().desc().list();
        JSONObject resultJson = new JSONObject();
        resultJson.put("data", resultList);
        return resultJson.toString();
    }

    /**
     * 模型批量删除
     *
     * @param ids
     * @param request
     * @return
     */
    @RequiresPermissions("Modeler:del")
    @RequestMapping(value = "deleteByIds")
    @ResponseBody
    public String deleteByIds(String[] ids, HttpServletRequest request) {
        JSONObject result = new JSONObject();
        for (String id : ids) {
            repositoryService.deleteModel(id);
        }
        result.put("msg", "删除成功");
        result.put("type", "success");
        return result.toString();
    }

    //获取流程清单


    /**
     * 发布流程
     *
     * @param modelId 模型ID
     * @return
     */
    @RequiresPermissions("process:add")
    @ResponseBody
    @RequestMapping("/publish")
    public Object publish(String modelId) {
        logger.info("流程部署入参modelId：{}", modelId);
        Map<String, String> map = new HashMap<String, String>();
        try {
            Model modelData = repositoryService.getModel(modelId);
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            System.out.println(modelId + ":::" + modelData.getId() + ":::" + modelData.getKey());
            if (bytes == null) {
                logger.info("部署ID:{}的模型数据为空，请先设计流程并成功保存，再进行发布", modelId);
                map.put("code", "FAILURE");
                return map;
            }
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addBpmnModel(modelData.getKey() + ".bpmn20.xml", model)
                    .deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
            map.put("code", "SUCCESS");
        } catch (Exception e) {
            logger.info("部署modelId:{}模型服务异常：{}", modelId, e);
            map.put("code", "FAILURE");
        }
        logger.info("流程部署出参map：{}", map);
        return map;
    }

    /**
     * 撤销流程定义
     *
     * @param modelId 模型ID
     * @return
     */
    @RequiresPermissions("process:revoke")
    @ResponseBody
    @RequestMapping("/revokePublish")
    public Object revokePublish(String modelId) {
        logger.info("撤销发布流程入参modelId：{}", modelId);
        Map<String, String> map = new HashMap<String, String>();
        Model modelData = repositoryService.getModel(modelId);
        if (null != modelData) {
            try {
                /**
                 * 参数不加true:为普通删除，如果当前规则下有正在执行的流程，则抛异常
                 * 参数加true:为级联删除,会删除和当前规则相关的所有信息，包括历史
                 */
                repositoryService.deleteDeployment(modelData.getDeploymentId(), true);
                map.put("code", "SUCCESS");
            } catch (Exception e) {
                logger.error("撤销已部署流程服务异常：{}", e);
                map.put("code", "FAILURE");
            }
        }
        logger.info("撤销发布流程出参map：{}", map);
        return map;
    }

    /**
     * 删除流程实例
     *
     * @param modelId 模型ID
     * @return
     */
    @RequiresPermissions("process:del")
    @ResponseBody
    @RequestMapping("/delete")
    public Object deleteProcessInstance(String modelId) {
        logger.info("删除流程实例入参modelId：{}", modelId);
        Map<String, String> map = new HashMap<>();
        Model modelData = repositoryService.getModel(modelId);

        if (null != modelData) {
            try {
                ProcessInstance pi = runtimeService.createProcessInstanceQuery().processDefinitionKey(modelData.getKey()).singleResult();
                if (null != pi) {
                    runtimeService.deleteProcessInstance(pi.getId(), "");
                    historyService.deleteHistoricProcessInstance(pi.getId());

                }

                map.put("code", "SUCCESS");
            } catch (Exception e) {
                logger.error("删除流程实例服务异常：{}", e);
                map.put("code", "FAILURE");
            }
        }

        logger.info("删除流程实例出参map：{}", map);
        return map;
    }

    @RequestMapping(value = "/image/{pid}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] definitionImage(@PathVariable("pid") String processDefinitionId) throws IOException {

        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        if (model != null && model.getLocationMap().size() > 0) {
            ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            InputStream imageStream = generator.generateDiagram(model, "png", new ArrayList<>());
            byte[] buffer = new byte[imageStream.available()];
            imageStream.read(buffer);
            imageStream.close();
            return buffer;
        }


        return new byte[0];
    }

    @GetMapping("/showImage")
    public String image() {

        return "image";
    }


    @RequestMapping(value = "/image2/{pid}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] getProcessImage(@PathVariable("pid") String processInstanceId) throws Exception {

        //  获取历史流程实例

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (historicProcessInstance == null) {
            throw new Exception();
        } else {
            // 获取流程定义
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService
                    .getProcessDefinition(historicProcessInstance.getProcessDefinitionId());

            // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
            List<HistoricActivityInstance> historicActivityInstanceList = historyService
                    .createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                    .orderByHistoricActivityInstanceId().asc().list();

            // 已执行的节点ID集合
            List<String> executedActivityIdList = new ArrayList<>();
            @SuppressWarnings("unused") int index = 1;
            logger.info("获取已经执行的节点ID");
            for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                executedActivityIdList.add(activityInstance.getActivityId());
                logger.info("第[" + index + "]个已执行节点=" + activityInstance.getActivityId() + " : " + activityInstance
                        .getActivityName());

                index++;
            }
            // 获取流程图图像字符流
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            InputStream imageStream = generator.generateDiagram(bpmnModel, "png", executedActivityIdList);
            byte[] buffer = new byte[imageStream.available()];
            imageStream.read(buffer);
            imageStream.close();

            return buffer;
        }

    }

    @RequiresPermissions("Modeler:list")
    @RequestMapping("list")
    @ResponseBody
    public BaseResponse list(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {

        return iModelerService.page(page,rows);

    }


    //test easyui
    @RequiresPermissions("Modeler:list")
    @RequestMapping("list1")
    @ResponseBody
    public JSONObject list1(@RequestParam(name = "sort", defaultValue = "1")  String sort,@RequestParam(name = "order", defaultValue = "1")  String order,@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        List<Model>  resultList;
        try {
            JSONObject jsStr = JSONObject.fromObject(paramData); //将字符串{“id”：1}
            System.out.println(jsStr.getString("name"));

            resultList = repositoryService.createModelQuery()
                    .orderByCreateTime()
                    .desc()
                    .listPage(page*rows-rows,rows);
        }catch (Exception e){
            resultList = repositoryService.createModelQuery()
                    .orderByCreateTime()
                    .desc()
                    .listPage(page*rows-rows,rows);
        }
        List<String> obj=new ArrayList<>();
        for (Model ml : resultList){
            JSONObject jo = new JSONObject();
            jo.put("id",ml.getId());
            jo.put("name",ml.getName());
            jo.put("createTime", DateTimeUtils.activityDateToString(ml.getCreateTime()));
            jo.put("lastUpdateTime", DateTimeUtils.activityDateToString(ml.getLastUpdateTime()));
            jo.put("deploymentId",ml.getDeploymentId());
            jo.put("key",ml.getKey());
            jo.put("version",ml.getVersion());
            //createTime、deploymentId、id、key、lastUpdateTime、name、version
            obj.add(jo.toString());

        }
        List<Model> resultList1 = repositoryService.createModelQuery()
                .orderByCreateTime()
                .desc().list();


        JSONObject jsonResult = new JSONObject();
        jsonResult.put("total", resultList1.size());
        jsonResult.put("rows", obj.toString());
        return jsonResult;
    }


}
