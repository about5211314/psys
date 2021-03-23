package com.cn.psys.system.controller;
import com.cn.psys.system.entity.RolePermission;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.psys.system.service.IRolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.cn.psys.base.BaseResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.cn.psys.mybatisplus.MybatisPlusQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
/**
* <p>
    *  前端控制器
    * </p>
*
* @author shenl
* @since 2020-03-11
*/
@RestController
@RequestMapping("/role-permission")
    public class RolePermissionController {
    private IRolePermissionService targetService;

    @Autowired
    public RolePermissionController(IRolePermissionService targetService){
        this.targetService = targetService;
    }


    /**
    * showdoc
    * @catalog RolePermission
    * @title 分页列表
    * @description 分页列表接口
    * @method post
    * @url http://localhost/user/sys-user/list
    * @param page 非必选 string 页数（不传默认第一页）
    * @param rows 非必选 string 行数（不传默认一页10行）
    * @remark 无
    * @number 1
    */
    @RequiresPermissions("RolePermission:list")
    @RequestMapping("/list")
    @ResponseBody
    public BaseResponse findListByPage(@RequestParam(required=false) String paramData,@RequestParam(name = "page", defaultValue = "1") int pageIndex,@RequestParam(name = "rows", defaultValue = "10") int step){
        QueryWrapper<RolePermission> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        Page page = new Page(pageIndex,step);
        targetService.page(page,queryWrapper);
        return BaseResponse.success(targetService.page(page));
    }


    /**
    * showdoc
    * @catalog RolePermission
    * @title 所有数据列表
    * @description 所有数据列表
    * @method post
    * @url http://localhost/user/sys-user/all
    * @param 无 无 无 无
    * @remark 无
    * @number 2
    */
    @RequiresPermissions("RolePermission:all")
    @RequestMapping("/all")
    @ResponseBody
    public BaseResponse findAll(@RequestParam(required=false) String paramData) {
        QueryWrapper<RolePermission> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        return BaseResponse.success(targetService.list(queryWrapper));
     }


    /**
    * showdoc
    * @catalog RolePermission
    * @title 详情页面
    * @description 详情页面接口
    * @method post
    * @url http://localhost/user/sys-user/find
    * @param id 必选 Long 页数
    * @remark 无
    * @number 3
    */
    @RequiresPermissions("RolePermission:find")
    @RequestMapping("/find")
    @ResponseBody
    public BaseResponse find(@RequestParam(required=false) String paramData) {
        QueryWrapper<RolePermission> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        RolePermission RolePermission = null;
        try{
            RolePermission = targetService.getOne(queryWrapper,true);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (RolePermission == null) {
            return BaseResponse.error("无查询结果");
        }
        return BaseResponse.success(RolePermission);
    }

    /**
    * showdoc
    * @catalog RolePermission
    * @title 新增数据
    * @description 新增数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
    * @param RolePermission 必选 RolePermission 保存对象
    * @remark 无
    * @number 4
    */
    @RequiresPermissions("RolePermission:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse addItem(@RequestBody RolePermission RolePermission){
        boolean isOk = targetService.save(RolePermission);
        if(isOk){
            return BaseResponse.success("数据添加成功！");
        }
            return BaseResponse.error("数据添加失败");
    }


    /**
    * showdoc
    * @catalog RolePermission
    * @title 更新数据
    * @description 更新数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
    * @param RolePermission 必选 RolePermission 保存对象
    * @remark 无
    * @number 5
    */
    @RequiresPermissions("RolePermission:update")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse updateItem(@RequestBody RolePermission RolePermission){
        boolean isOk = targetService.updateById(RolePermission);
        if(isOk){
            return BaseResponse.success("数据更改成功！");
        }
            return BaseResponse.error("数据更改失败");
    }


    /**
    * showdoc
    * @catalog RolePermission
    * @title 删除数据
    * @description 删除数据接口
    * @method post
    * @url http://localhost/user/sys-user/del
    * @param ids 必选 List<Long> 页数
        * @remark 无
        * @number 7
        */
        @RequiresPermissions("RolePermission:del")
        @RequestMapping("/del")
        @ResponseBody
        public BaseResponse deleteItems(@RequestParam("ids") List<Long> ids){
            boolean isOk = targetService.removeByIds(ids);
            if(isOk){
                return BaseResponse.success("数据删除成功！");
            }
                return BaseResponse.error("数据删除失败");
            }
         }

