package com.cn.psys;

import com.cn.psys.activity.service.ITaskService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.RepositoryService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Collection;

public class testTask {

    @Resource
    private RepositoryService repositoryService;
    @Autowired(required = false)
    private ITaskService iTaskService;



//    @Test
//    public void FlowElement(){
//        BpmnModel model = repositoryService.getBpmnModel("262508");
//        if(model != null) {
//            Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
//            for(FlowElement e : flowElements) {
//                System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
//            }
//        }
//    }



}
