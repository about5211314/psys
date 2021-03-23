package com.cn.psys.system.controller;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cn.psys.system.entity.Role;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.psys.system.service.IRoleService;
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
/**
* <p>
    *  前端控制器
    * </p>
*
* @author zhuwei
* @since 2020-03-21
*/
@RestController
@RequestMapping("/role")
    public class RoleController {
    private IRoleService targetService;

    @Autowired
    public RoleController(IRoleService targetService){
        this.targetService = targetService;
    }


    /**
    * showdoc
    * @catalog Role
    * @title 分页列表
    * @description 分页列表接口
    * @method post
    * @url http://localhost/user/sys-user/list
    * @param page 非必选 string 页数（不传默认第一页）
    * @param rows 非必选 string 行数（不传默认一页10行）
    * @remark 无
    * @number 1
    */
    @RequiresPermissions("Role:list")
    @RequestMapping("/list")
    @ResponseBody
    public Map<String,Object> findListByPage(@RequestParam(required=false) String paramData,@RequestParam(name = "page", defaultValue = "1") int pageIndex,@RequestParam(name = "rows", defaultValue = "10") int step){
        QueryWrapper<Role> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        Page page = new Page(pageIndex,step);
        Map<String,Object> map = new HashMap<>();
        IPage obj = targetService.page(page,queryWrapper);
        map.put("total", obj.getTotal());
        map.put("rows",obj.getRecords());
        return map;
    }


    /**
    * showdoc
    * @catalog Role
    * @title 所有数据列表
    * @description 所有数据列表
    * @method post
    * @url http://localhost/user/sys-user/all
    * @param 无 无 无 无
    * @remark 无
    * @number 2
    */
    @RequiresPermissions("Role:all")
    @RequestMapping("/all")
    @ResponseBody
    public BaseResponse findAll(@RequestParam(required=false) String paramData) {
        QueryWrapper<Role> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        return BaseResponse.success(targetService.list(queryWrapper));
     }


    /**
    * showdoc
    * @catalog Role
    * @title 详情页面
    * @description 详情页面接口
    * @method post
    * @url http://localhost/user/sys-user/find
    * @param id 必选 Long 页数
    * @remark 无
    * @number 3
    */
    @RequiresPermissions("Role:find")
    @RequestMapping("/find")
    @ResponseBody
    public BaseResponse find(@RequestParam(required=false) String paramData) {
        QueryWrapper<Role> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        Role Role = null;
        try{
            Role = targetService.getOne(queryWrapper,true);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (Role == null) {
            return BaseResponse.error("无查询结果");
        }
        return BaseResponse.success(Role);
    }

    /**
    * showdoc
    * @catalog Role
    * @title 新增数据
    * @description 新增数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
    * @param Role 必选 Role 保存对象
    * @remark 无
    * @number 4
    */
    @RequiresPermissions("Role:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse addItem(@RequestBody Role Role){
        String roleId = IdUtil.randomUUID();
        Role.setRoleid(roleId);
        boolean isOk = targetService.save(Role);
        if(isOk){
            return BaseResponse.success(roleId);
        }
            return BaseResponse.error("数据添加失败");
    }


    /**
    * showdoc
    * @catalog Role
    * @title 更新数据
    * @description 更新数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
    * @param Role 必选 Role 保存对象
    * @remark 无
    * @number 5
    */
    @RequiresPermissions("Role:update")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse updateItem(@RequestBody Role Role){
        boolean isOk = targetService.updateById(Role);
        if(isOk){
            return BaseResponse.success(Role.getRoleid());
        }
            return BaseResponse.error("数据更改失败");
    }


    /**
    * showdoc
    * @catalog Role
    * @title 删除数据
    * @description 删除数据接口
    * @method post
    * @url http://localhost/user/sys-user/del
    * @param ids 必选 List<Long> 页数
        * @remark 无
        * @number 7
        */
        @RequiresPermissions("Role:del")
        @RequestMapping("/del")
        @ResponseBody
        public BaseResponse deleteItems(@RequestParam("ids") List<String> ids){
            boolean isOk = targetService.removeByIds(ids);
            if(isOk){
                return BaseResponse.success("数据删除成功！");
            }
                return BaseResponse.error("数据删除失败");
            }
         }

