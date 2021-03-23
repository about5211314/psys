package com.cn.psys.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.cn.psys.activity.ActivityTools;
import com.cn.psys.activity.service.IModelerService;
import com.cn.psys.activity.utils.BpmnConverterUtil;
import com.cn.psys.base.BaseResponse;
import com.cn.psys.base.RestResult;
import com.cn.psys.base.ResultCode;
import com.cn.psys.base.ResultGenerator;
import com.cn.psys.tools.DateTimeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sf.json.JSONObject;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.activiti.editor.constants.ModelDataJsonConstants.*;

@Service
public class ModelerServiceImpl extends BaseServiceImpl implements IModelerService {

    @Resource
    private RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean save(Model entity) {
        return false;
    }

    @Override
    public boolean update(Model entity) {
        return false;
    }

    @Override
    public boolean remove(String[] ids) {
        boolean ok = false;
        for (String id : ids) {
            repositoryService.deleteModel(id);
        }
        return ok;
    }


    @Override
    public BaseResponse page(int page, int rows) {
        List<Model> resultList = repositoryService.createModelQuery()
                .orderByCreateTime()
                .desc()
                .listPage(page,rows);
        //.list();
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
        long resultList1 = repositoryService.createModelQuery().count();
        ActivityTools activityTools = new ActivityTools();
        BaseResponse baseResponse = new BaseResponse(200,"success",activityTools.list(page,rows,resultList1,obj));
        return baseResponse;
    }



    /**
     * 保存模型
     * @param key
     * @param name
     * @param category
     * @param descp
     * @throws UnsupportedEncodingException
     * @return
     */
    @Override
    public RestResult createModel(String key, String name, String category, String descp) throws UnsupportedEncodingException{
        //初始化一个空模型
        Model model = repositoryService.newModel();
        //设置一些默认信息
        String modelName = name;
        String description = descp;
        int revision = 1;
        String modelKey = key;

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(MODEL_NAME,modelName);
        modelNode.put(MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setName(modelName);
        model.setKey(modelKey);
        model.setMetaInfo(modelNode.toString());

        repositoryService.saveModel(model);
        String id = model.getId();

        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://activiti.org/bpmn");
        editorNode.put("stencilset", stencilSetNode);

        repositoryService.addModelEditorSource(id,editorNode.toString().getBytes("utf-8"));
        return new ResultGenerator().getSuccessResult();
    }



    /**
     * 保存模型
     * @return
     */
    public RestResult saveModelXml(@PathVariable String modelId,
                                   @RequestBody MultiValueMap<String, String> values) {
        ByteArrayOutputStream outStream = null;
        try {


            Model model = repositoryService.getModel(modelId);
            // 获取模型信息
            ObjectNode modelJson = (ObjectNode) objectMapper
                    .readTree(model.getMetaInfo());
            // 获取value第一个元素
            modelJson.put(MODEL_NAME, model.getName());
            modelJson.put(MODEL_DESCRIPTION, modelJson.get("description"));
            model.setMetaInfo(modelJson.toString());
            // 版本
            model.setVersion(model.getVersion() + 1);
            repositoryService.saveModel(model);
            String bpmnXml = BpmnConverterUtil
                    .converterXmlToJson(values.getFirst("bpmn_xml")).toString();
            repositoryService.addModelEditorSource(model.getId(), bpmnXml.getBytes("utf-8"));
            repositoryService.addModelEditorSourceExtra(model.getId(),
                    values.getFirst("svg_xml").getBytes("utf-8"));
            return new ResultGenerator().getSuccessResult();
        } catch (Exception e) {
            //LOG.error("Error saving model", e);
            System.out.println(e.toString());
            //throw new ActivitiException("Error saving model", e);
            return new ResultGenerator().getFreeResult(ResultCode.FAIL,"error",e);
        }
    }

    /**
     * 删除模型
     * @param modelId
     * @return
     */
    @Override
    public RestResult deleteModel(String modelId) {

        repositoryService.deleteModel(modelId);
        return new ResultGenerator().getSuccessResult();
    }

    /**
     * 部署流程
     * @param modelId
     * @return
     */
    @Override
    public RestResult deployModel(String modelId) throws Exception {
        // 获取模型
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if(null == bytes) {
            //return "模型数据为空，请先设计流程并成功保存，再进行发布。";
            return new ResultGenerator().getFreeResult(ResultCode.INTERNAL_SERVER_ERROR,"模型数据为空，请先设计流程并成功保存，再进行发布!","");
        }
        JsonNode modelNode = objectMapper.readTree(bytes);
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if (model.getProcesses().size() == 0){
            //return "数据模型不符合要求，请至少设计一条主线程流。";
            return new ResultGenerator().getFreeResult(ResultCode.INTERNAL_SERVER_ERROR,"数据模型不符合要求，请至少设计一条主线程流!","");

        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return new ResultGenerator().getSuccessResult();
    }


    /**
     * 根据生成的ID获取模型流程编辑器
     * @param modelId
     * @return
     */
    @Override
    public RestResult getEditorXml(@PathVariable String modelId) {
        com.alibaba.fastjson.JSONObject jsonObject = null;
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
               // System.out.println(model.getMetaInfo());
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    jsonObject = JSON.parseObject(model.getMetaInfo());
                } else {
                    jsonObject = new com.alibaba.fastjson.JSONObject();
                    jsonObject.put(MODEL_NAME, model.getName());
                }
                jsonObject.put(MODEL_ID, model.getId());
                com.alibaba.fastjson.JSONObject editorJsonNode = JSON.parseObject(new String(repositoryService.getModelEditorSource(model.getId())));
                //将json流程转为标准xml流程图
                String bpmnXml = BpmnConverterUtil.converterJsonToWebXml(editorJsonNode.toJSONString());
                //System.out.println("bpmnXml11111------000"+bpmnXml);
                jsonObject.put("bpmnXml", bpmnXml);
            } catch (Exception e) {
               // LOG.error("创建model的json串失败", e);
              //  System.out.println("创建model的json串失败" + e.toString());
                throw new ActivitiException("无法读取model信息", e);
            }
        } else {
            //LOG.error("创建model的json串失败[{}]", modelId);
           // System.out.println("创建model的json串失败[{}]" + modelId);
            throw new ActivitiException("未找到对应模型信息");
        }
        //System.out.println("创建model的:::" + jsonObject.toJSONString());
        return new ResultGenerator().getSuccessResult(jsonObject);

        //return jsonObject;
    }

    /**
     * 查询模型
     * @return
     */
    @Override
    public RestResult listModel() {
        List<Model> lsit = repositoryService.createModelQuery().list();
        return new ResultGenerator().getSuccessResult(lsit);
    }

}
