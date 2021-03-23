package com.cn.psys.activity.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.psys.activity.service.IModelerService;
import com.cn.psys.base.BaseResponse;
import com.cn.psys.base.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.repository.Model;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("modeler1")
public class NewModelerController {

    @Autowired(required=false)
    private IModelerService iModelerService;


    @RequiresPermissions("Modeler:list")
    @RequestMapping("list")
    @ResponseBody
    public BaseResponse list(@RequestParam(name = "page", defaultValue = "1")  int page, @RequestParam(name = "rows", defaultValue = "10")  int rows, @RequestParam(required=false) String paramData) {
        return iModelerService.page(page,rows);

    }

    @RequiresPermissions("Modeler:del")
    @RequestMapping(value = "deleteByIds")
    @ResponseBody
    public BaseResponse deleteByIds(String[] ids) {
        boolean isOk = iModelerService.remove(ids);
        if(isOk){
            return BaseResponse.success("数据删除成功！");
        }
        return BaseResponse.error("数据删除失败");
    }


    @RequiresPermissions("Modeler:add")
    @GetMapping("modeler")
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




    //*************************以下流程前后端分离接口*****************
    /**
     * 创建模型
     */
    @RequiresPermissions("Modeler:add")
    @RequestMapping(value = "/model/insert", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public RestResult createModel(@RequestParam String key, @RequestParam String name, @RequestParam String category, @RequestParam String descp) throws UnsupportedEncodingException {
        return iModelerService.createModel(key, name, category, descp);
    }
    /**
     * 模型列表
     */
    @RequiresPermissions("Modeler:list")
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    public RestResult listModel() {
        RestResult listModel = iModelerService.listModel();
        return  listModel;
    }

    /**
     * 保存模型
     */
    @RequestMapping(value = "/model/{modelId}/xml/save", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public RestResult saveModelXml(@PathVariable String modelId,@RequestParam MultiValueMap<String, String> values) {

        return iModelerService.saveModelXml(modelId, values);
    }
    /**
     * 模型列表
     */
    @RequiresPermissions("Modeler:del")
    @ResponseBody
    @GetMapping(value = "/deleteModel")
    public RestResult flowDelete(@RequestParam(name = "modelId") String modelId){
        return iModelerService.deleteModel(modelId);
    }

    /**
     * 根据生成的ID获取模型流程编辑器
     *      * @param modelId
     *      * @return
     */
    @RequestMapping(value = "/model/{modelId}/xml", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public RestResult getEditorXml(@PathVariable String modelId) {
        return iModelerService.getEditorXml(modelId);
    }

    @GetMapping(value = "/model/deploy")
    public RestResult deploy(@RequestParam(name = "modelId") String modelId) throws Exception {
        return iModelerService.deployModel(modelId);
    }

}
