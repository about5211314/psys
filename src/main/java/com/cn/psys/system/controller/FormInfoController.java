package com.cn.psys.system.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cn.psys.system.entity.FormInfo;
import com.cn.psys.system.mapper.CommonMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.psys.system.service.IFormInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cn.psys.base.BaseResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.cn.psys.mybatisplus.MybatisPlusQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.hutool.core.util.IdUtil;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zhuwei
 * @since 2020-03-26
 */
@RestController
@RequestMapping("/form-info")
public class FormInfoController {
    private IFormInfoService targetService;

    @Autowired
    public FormInfoController(IFormInfoService targetService) {
        this.targetService = targetService;
    }

    @Autowired
    private CommonMapper mapperD;


    /**
     * showdoc
     *
     * @param page 非必选 string 页数（不传默认第一页）
     * @param rows 非必选 string 行数（不传默认一页10行）
     * @catalog FormInfo
     * @title 分页列表
     * @description 分页列表接口
     * @method post
     * @url http://localhost/user/sys-user/list
     * @remark 无
     * @number 1
     */
    @RequiresPermissions("FormInfo:list")
    @RequestMapping("/list")
    @ResponseBody
    public BaseResponse findListByPage(@RequestParam(required = false) String paramData, @RequestParam(name = "page", defaultValue = "1") int pageIndex, @RequestParam(name = "rows", defaultValue = "10") int step) {
        QueryWrapper<FormInfo> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        Page page = new Page(pageIndex, step);
        return BaseResponse.success(targetService.page(page, queryWrapper));
    }


    /**
     * showdoc
     *
     * @param 无 无 无 无
     * @catalog FormInfo
     * @title 所有数据列表
     * @description 所有数据列表
     * @method post
     * @url http://localhost/user/sys-user/all
     * @remark 无
     * @number 2
     */
    @RequiresPermissions("FormInfo:all")
    @RequestMapping("/all")
    @ResponseBody
    public Map<String,Object> findAll(@RequestParam(required = false) String paramData) {
        //QueryWrapper<FormInfo> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        Map<String,Object> map = new HashMap<>();
        List<FormInfo> list = mapperD.formList();
        map.put("total",list.size());
        map.put("rows",list);
        return map;
    }


    /**
     * showdoc
     *
     * @param id 必选 Long 页数
     * @catalog FormInfo
     * @title 详情页面
     * @description 详情页面接口
     * @method post
     * @url http://localhost/user/sys-user/find
     * @remark 无
     * @number 3
     */
   // @RequiresPermissions("FormInfo:find")
    @RequestMapping("/find")
    @ResponseBody
    public BaseResponse find(@RequestParam(required = false) String paramData) {
        QueryWrapper<FormInfo> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        FormInfo FormInfo = null;
        try {
            FormInfo = targetService.getOne(queryWrapper, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (FormInfo == null) {
            return BaseResponse.error("无查询结果");
        }
        return BaseResponse.success(FormInfo);
    }

    /**
     * showdoc
     *
     * @param FormInfo 必选 FormInfo 保存对象
     * @catalog FormInfo
     * @title 新增数据
     * @description 新增数据接口
     * @method post
     * @url http://localhost/user/sys-user/add
     * @remark 无
     * @number 4
     */
    @RequiresPermissions("FormInfo:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse addItem(@RequestBody FormInfo FormInfo) {
        if (StringUtils.isNotEmpty(FormInfo.getUuid())) {
            //修改,增加一条相同ID的数据
            String formId = FormInfo.getFormId();
            String version = FormInfo.getVersion().split("-")[1];
            FormInfo.setVersion(formId + "-" + (Integer.parseInt(version) + 1));
        } else {
            FormInfo.setVersion(FormInfo.getFormId() + "-1");
        }
        FormInfo.setUuid(IdUtil.randomUUID());
        FormInfo.setCreateTime(DateUtil.now());
        boolean isOk = targetService.save(FormInfo);
        if (isOk) {
            return BaseResponse.success("数据添加成功");
        }
        return BaseResponse.error("数据添加失败");
    }


    /**
     * showdoc
     *
     * @param FormInfo 必选 FormInfo 保存对象
     * @catalog FormInfo
     * @title 更新数据
     * @description 更新数据接口
     * @method post
     * @url http://localhost/user/sys-user/add
     * @remark 无
     * @number 5
     */
    @RequiresPermissions("FormInfo:update")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse updateItem(@RequestBody FormInfo FormInfo) {
        String version = null;
        String flowId = null;
        if (FormInfo != null) {
            flowId = FormInfo.getFormId();
            version = FormInfo.getVersion().split("-")[1];
            FormInfo.setVersion(flowId + "-" + (Integer.parseInt(version) + 1));
            FormInfo.setUuid(IdUtil.randomUUID());
            FormInfo.setCreateTime(DateUtil.now());
        }
        boolean isOk = targetService.save(FormInfo);
        if (isOk) {
            return BaseResponse.success("数据更改成功！");
        }
        return BaseResponse.error("数据更改失败");
    }


    /**
     * showdoc
     *
     * @param ids 必选 List<Long> 页数
     * @catalog FormInfo
     * @title 删除数据
     * @description 删除数据接口
     * @method post
     * @url http://localhost/user/sys-user/del
     * @remark 无
     * @number 7
     */
    @RequiresPermissions("FormInfo:del")
    @RequestMapping("/del")
    @ResponseBody
    public BaseResponse deleteItems(@RequestParam("uuid") String uuid) {
        mapperD.formDelete(uuid);
        return BaseResponse.success("数据删除成功！");

    }
}

