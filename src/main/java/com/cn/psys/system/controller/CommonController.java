package com.cn.psys.system.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.cn.psys.base.BaseResponse;
import com.cn.psys.base.DbOPeration;
import com.cn.psys.mybatisplus.BaseSelect;
import com.cn.psys.system.entity.Department;
import com.cn.psys.system.entity.RolePermission;
import com.cn.psys.system.entity.UserRole;
import com.cn.psys.system.mapper.CommonMapper;
import com.cn.psys.system.service.IRolePermissionService;
import com.cn.psys.system.service.IUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制器公共类
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private CommonMapper mapperD;

    @Autowired
    private IUserRoleService iUserRoleService;

    @Autowired
    private IRolePermissionService  iRolePermissionService;

    /**
     * 部门组合树查询
     *
     * @param pId
     * @return
     */
    @RequestMapping("/selectDept")
    @ResponseBody
    public BaseResponse findDeptList(@RequestParam(required = false) String pId) {
        String selectDeptJson = DbOPeration.getSelectDataForJson(
                "department", "PAR_ID", "dp_id", "dept_name", mapperD,0);
        return BaseResponse.success(selectDeptJson);
    }

    /**
     * 部门ztree查询
     * @return
     */
    @RequestMapping("/selectDeptZtree")
    @ResponseBody
    public List<BaseSelect> selectDeptZtree() {
        return mapperD.selectDeptZtree();
    }

    /**
     * 组织结构树
     *
     * @param pId
     * @return
     */
    @RequestMapping("/selectUserList")
    @ResponseBody
    public BaseResponse selectUserList(@RequestParam(required = false) String pId) {
        List<BaseSelect> list = mapperD.selectUserList();
        return BaseResponse.success(list);
    }

    /**
     * @param rId     角色id
     * @param userIds 用户id，user1,user2,user3
     * @return
     */
    @RequestMapping("/editUserRole")
    @ResponseBody
    public BaseResponse editUserRole(@RequestParam("rId") String rId, @RequestParam("userIds") String userIds) {
        //先删除之前的关联
        mapperD.deleteUserRole(rId);
        //定义一个用户角色关联对象的集合
        List<UserRole> list = new ArrayList<>();
        if(StrUtil.isNotEmpty(userIds) && StrUtil.isNotBlank(userIds)){
            String arr[] = userIds.split(",");
            UserRole uSerRole = null;
            for(String tmp : arr){
                uSerRole = new UserRole();
                uSerRole.setRid(rId);
                uSerRole.setUid(tmp);
                //添加新的关联
                list.add(uSerRole);
            }
            iUserRoleService.saveBatch(list);
        }
        return BaseResponse.success(userIds);
    }

    /**
     * 权限树
     *
     * @return
     */
    @RequestMapping("/selectPermissionList")
    @ResponseBody
    public BaseResponse selectPermissionList() {
        String selectDeptJson = DbOPeration.getSelectDataForJson(
                "permission", "par_id", "pid", "modelname", mapperD,1);
        return BaseResponse.success(selectDeptJson);
    }

    /**
     * 权限ztree结构
     *
     * @return
     */
    @RequestMapping("/zTreePerList")
    @ResponseBody
    public BaseResponse zTreePerList() {
        List<BaseSelect> list = mapperD.zTreePerList();
        return BaseResponse.success(list);
    }

    /**
     * @param rId     角色id
     * @param pIds 权限id
     * @return
     */
    @RequestMapping("/editURolePermission")
    @ResponseBody
    public BaseResponse editURolePermission(@RequestParam("rId") String rId, @RequestParam("pIds") String pIds) {
        //先删除之前的关联
        mapperD.deleteRolePermission(rId);
        //定义一个用户角色关联对象的集合
        List<RolePermission> list = new ArrayList<>();
        if(StrUtil.isNotEmpty(pIds) && StrUtil.isNotBlank(pIds)){
            String arr[] = pIds.split(",");
            RolePermission rolePermission = null;
            for(String tmp : arr){
                rolePermission = new RolePermission();
                rolePermission.setRid(rId);
                rolePermission.setPid(tmp);
                //添加新的关联
                list.add(rolePermission);
            }
            iRolePermissionService.saveBatch(list);
        }
        return BaseResponse.success("保存成功");
    }
}
