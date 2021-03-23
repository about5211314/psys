package com.cn.psys.activity;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.psys.base.BaseResponse;
import com.cn.psys.tools.DateTimeUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("process")
public class ProcessController {

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

    @Resource
    private TaskService taskService;


    /**
     * 启动一个流程
     * <p>
     * 参数：
     * key:  流程定义KEY
     * <p>
     * bussId：  业务对象序号
     * bussName：业务对象名称
     * bussType：业务对象类型
     * startUserId: 发起人id
     * startUnitId: 发起人单位id
     *
     * @auther:
     * @date:
     */
    @RequiresPermissions("process:add")
    @PostMapping("run/{key}")
    public String run(@PathVariable String key, @RequestBody Map<String, Object> variables) {
        //需获取当前登录人进行赋值
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        variables.put("userId", username);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
        //获取自定义表单id  根据processInstance.getProcessDefinitionId() 如：ccsq:5:132504
//        System.out.println(processInstance.getId());
//        System.out.println(processInstance.getProcessDefinitionId());
//        System.out.println(formService.getStartFormData(processInstance.getProcessDefinitionId()).getFormKey());
//
//
//        List<Task> taskQuery = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).list();
//
//       for(Task task : taskQuery){
//
//           System.out.println(task.getFormKey());
//
//       }
        return processInstance.getId();
    }


    @PostMapping("runor/{key}")
    public String runor(@PathVariable String key, @RequestBody Map<String, Object> variables) {

        String formKey = "";
        String taskId = "";
        //需获取当前登录人进行赋值
        String username = (String) SecurityUtils.getSubject().getPrincipal();
        variables.put("userId", username);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);

        List<Task> taskQuery = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).list();

        JSONArray jsonArray = new JSONArray();
        for (Task task : taskQuery) {
            JSONObject jo = new JSONObject();
            formKey = task.getFormKey();
            jo.put("formKey", task.getFormKey());
            jo.put("taskId", task.getId());
            taskId = task.getId();
            jsonArray.add(jo);
        }
        return jsonArray.toString();
    }


    /**
     * 获取所有流程
     *
     * @auther:
     * @date:
     */
    @GetMapping("")
    public String list1() {
        List<ProcessDefinition> list = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()//创建一个流程定义的查询
                /**指定查询条件,where条件*/
//                        .deploymentId(deploymentId)//使用部署对象ID查询
//                        .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                        .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
//                        .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /**排序*/
                // .orderByProcessDefinitionVersion().asc()//按照版本的升序排列
                .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

                /**返回的结果集*/
                .list();//返回一个集合列表，封装流程定义
//                        .singleResult();//返回惟一结果集
//                        .count();//返回结果集数量
//                        .listPage(firstResult, maxResults);//分页查询
        if (list != null && list.size() > 0) {
            for (ProcessDefinition pd : list) {
                System.out.print("流程定义ID:" + pd.getId());//流程定义的key+版本+随机生成数
                System.out.print("流程定义的名称:" + pd.getName());//对应helloworld.bpmn文件中的name属性值
                System.out.print("流程定义的key:" + pd.getKey());//对应helloworld.bpmn文件中的id属性值
                System.out.print("流程定义的版本:" + pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.print("资源名称bpmn文件:" + pd.getResourceName());
                System.out.print("资源名称png文件:" + pd.getDiagramResourceName());
                System.out.print("部署对象ID：" + pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
        return "";
    }


    /**
     * 获取所有流程的最后一个版本
     *
     * @auther:
     * @date:
     */
    @ResponseBody
    @RequestMapping("/listlast")
    public String findLastVersionProcessDefinition() {
        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
                .orderByProcessDefinitionVersion().latestVersion().asc()//使用流程定义的版本升序排列
                .list();


        JSONArray jsonArray = new JSONArray();
        for (ProcessDefinition processDefinition : list) {
            JSONObject jo = new JSONObject();
            jo.put("getId", processDefinition.getId());
            jo.put("getDeploymentId", processDefinition.getDeploymentId());
            jo.put("getName", processDefinition.getName());
            jo.put("getKey", processDefinition.getKey());
            jo.put("getVersion", processDefinition.getVersion());
            jo.put("getResourceName", processDefinition.getResourceName());
            jo.put("getDiagramResourceName", processDefinition.getDiagramResourceName());
            jsonArray.add(jo);
        }
        return jsonArray.toString();


        /**
         * Map<String,ProcessDefinition>
         map集合的key：流程定义的key
         map集合的value：流程定义的对象
         map集合的特点：当map集合key值相同的情况下，后一次的值将替换前一次的值
         */
//        Map<String, ProcessDefinition> map = new LinkedHashMap<String, ProcessDefinition>();
//        if(list!=null && list.size()>0){
//            for(ProcessDefinition pd:list){
//                map.put(pd.getKey(), pd);
//            }
//        }
//        List<ProcessDefinition> pdList = new ArrayList<ProcessDefinition>(map.values());
//
//
//
//        if(pdList!=null && pdList.size()>0){
//            for(ProcessDefinition pd:pdList){
//                System.out.print("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
//                System.out.print("流程定义的名称:"+pd.getName());//对应helloworld.bpmn文件中的name属性值
//                System.out.print("流程定义的key:"+pd.getKey());//对应helloworld.bpmn文件中的id属性值
//                System.out.print("流程定义的版本:"+pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
//                System.out.print("资源名称bpmn文件:"+pd.getResourceName());
//                System.out.print("资源名称png文件:"+pd.getDiagramResourceName());
//                System.out.print("部署对象ID："+pd.getDeploymentId());
//                System.out.println("#########################################################");
//            }
//        }


    }


    /**
     * 查看流程图
     *
     * @auther:
     * @date:
     */
    @RequiresPermissions("process:show")
    @GetMapping("show")
    public void show(@RequestParam("did") String did, @RequestParam("ext") String ext, HttpServletResponse httpServletResponse) throws IOException {
        //System.out.println(did);
        //System.out.println(ext);
        if (StringUtils.isEmpty(did) || StringUtils.isEmpty(ext)) {

            return;
        }
        InputStream in = null;
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(did).singleResult();
        if (".png".equalsIgnoreCase(ext)) {
            in = repositoryService.getProcessDiagram(processDefinition.getId());
        } else if (".bpmn".equalsIgnoreCase(ext)) {
            in = repositoryService.getResourceAsStream(did, processDefinition.getResourceName());
        }
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    //查看流程图
    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public void image(HttpServletResponse response,
                      @RequestParam String processInstanceId) {
        try {
            InputStream is = getDiagram(processInstanceId);
            if (is == null)
                return;

            response.setContentType("image/png");

            BufferedImage image = ImageIO.read(is);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "png", out);

            is.close();
            out.close();
        } catch (Exception ex) {
            // log.error("查看流程图失败", ex);
        }
    }


    public InputStream getDiagram(String processInstanceId) {
        //获得流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = StringUtils.EMPTY;
        if (processInstance == null) {
            //查询已经结束的流程实例
            HistoricProcessInstance processInstanceHistory =
                    historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId).singleResult();
            if (processInstanceHistory == null)
                return null;
            else
                processDefinitionId = processInstanceHistory.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }

        //使用宋体
        String fontName = "宋体";
        //获取BPMN模型对象
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        //获取流程实例当前的节点，需要高亮显示
        List<String> currentActs = Collections.EMPTY_LIST;
        if (processInstance != null)
            currentActs = runtimeService.getActiveActivityIds(processInstance.getId());

        return processEngine.getProcessEngineConfiguration()
                .getProcessDiagramGenerator()
                .generateDiagram(model, "png", currentActs, new ArrayList<String>(),
                        fontName, fontName, fontName, null, 1.0);
    }


    /**
     * 根据一个流程实例的id挂起流程实例
     * 流程实例id
     */
    @RequiresPermissions("process:suspendProcessInstance")
    @ResponseBody
    @RequestMapping("/suspendProcessInstance")
    public void suspendProcessInstance(@RequestBody Map<String, Object> params) {
        System.out.println((String) params.get("processInstanceId"));

        runtimeService.suspendProcessInstanceById((String) params.get("processInstanceId"));
    }


    /**
     * 根据一个流程实例的id激活流程实例
     * 流程实例id
     */
    @RequiresPermissions("process:activateProcessInstance")
    @ResponseBody
    @RequestMapping("/activateProcessInstance")
    public void activateProcessInstance(@RequestBody Map<String, Object> params) {
        System.out.println((String) params.get("processInstanceId"));
        runtimeService.activateProcessInstanceById((String) params.get("processInstanceId"));
    }

    /**
     * 根据一个流程实例的id结束流程实例
     * 流程实例id
     */
    @RequiresPermissions("process:stopProcessInstance")
    @ResponseBody
    @RequestMapping("/stopProcessInstance")
    public void stopProcessInstance(@RequestBody Map<String, Object> params) {
        System.out.println((String) params.get("processInstanceId"));
        try {
            processEngine.getRuntimeService().deleteProcessInstance((String) params.get("processInstanceId"), "我愿意，结束。咋地啦！！！");

        }catch (RuntimeException e) {

        } finally {

        }
    }


    @RequiresPermissions("process:list")
    @RequestMapping("list")
    @ResponseBody
    public BaseResponse list(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {

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

    /**
     * 根据一个流程实例的id删除流程实例
     * 流程实例id
     */
    @RequiresPermissions("process:deleteProcessInstance")
    @ResponseBody
    @RequestMapping("/deleteProcessInstance")
    public void deleteProcessInstance(@RequestBody Map<String, Object> params) {
        System.out.println((String) params.get("processInstanceId"));
        runtimeService.deleteProcessInstance((String) params.get("processInstanceId"),"删除原因");//删除流程

    }

    @RequiresPermissions("process:list")
    @RequestMapping("list2")
    @ResponseBody
    public JSONObject list2(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {

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


        JSONObject jsonResult = new JSONObject();
        jsonResult.put("total", list1.size());
        jsonResult.put("rows", obj.toString());
        return jsonResult;

    }



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
    public JSONObject findHistoryProcessInstance(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        HistoryService historyService = this.processEngine.getHistoryService();

        JSONObject jsStr = JSONObject.fromObject(paramData); //将字符串{“id”：1}
        System.out.println(jsStr.getString("flag"));
        List<HistoricProcessInstance> list;
        List<HistoricProcessInstance> list1;
        if("0".equals(jsStr.getString("flag"))){
            list = historyService.createHistoricProcessInstanceQuery().finished().orderByProcessInstanceEndTime().desc().listPage(page*rows-rows,rows);
            list1 = historyService.createHistoricProcessInstanceQuery().finished().orderByProcessInstanceEndTime().desc().list();
        }else{
            list = historyService.createHistoricProcessInstanceQuery().unfinished().orderByProcessInstanceStartTime().desc().listPage(page*rows-rows,rows);
            list1 = historyService.createHistoricProcessInstanceQuery().unfinished().orderByProcessInstanceStartTime().desc().list();
        }
        List<String> obj=new ArrayList<>();
        for (HistoricProcessInstance ml : list){
            JSONObject jo = new JSONObject();
            jo.put("id",ml.getId());
            jo.put("name",ml.getName());
            jo.put("processDefinitionId",ml.getProcessDefinitionId());


            System.out.println(ml.getSuperProcessInstanceId());
            List<HistoricTaskInstance> list7=processEngine.getHistoryService() // 历史相关Service
                    .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                    .processInstanceId(ml.getId()) // 用流程实例id查询
                    .unfinished().orderByTaskCreateTime().desc() // 查询已经完成的任务
                    .list();
            for(HistoricTaskInstance hti:list7){
                jo.put("taskId",hti.getId());
                jo.put("taskName",hti.getName());
                jo.put("formKey",hti.getFormKey());

            }
            jo.put("deploymentId",ml.getDeploymentId());
            jo.put("processDefinitionName",ml.getProcessDefinitionName());
            jo.put("processDefinitionVersion",ml.getProcessDefinitionVersion());
            jo.put("startTime", DateTimeUtils.activityDateToString(ml.getStartTime()));
            obj.add(jo.toString());
        }
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("total", list1.size());
        jsonResult.put("rows", obj.toString());
        return jsonResult;
    }

}
