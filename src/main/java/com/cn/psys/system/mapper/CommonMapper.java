package com.cn.psys.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cn.psys.mybatisplus.BaseSelect;
import com.cn.psys.system.entity.Department;
import com.cn.psys.system.entity.FormInfo;
import com.cn.psys.system.entity.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * Mapper公共接口
 * </p>
 *
 * @author zhuwei
 * @since 2020-03-17
 */
@Mapper
public interface CommonMapper extends BaseMapper<Department> {
    /*
    部门组合树查询
     */
    @Select("SELECT dp_id AS id,dept_name AS name,(case when par_id is null or par_id = '' then '-1' else par_id end) AS pid FROM department")
    List<BaseSelect> selectByMyWrapper();

    /*
    部门组合树查询
     */
    @Select("SELECT dp_id AS id,dept_name AS name,(case when par_id is null or par_id = '' then '-1' else par_id end) AS pid FROM department")
    List<BaseSelect> selectDeptZtree();

    /*
    根据人员的userId查询该人员所有能访问的权限
     */
    @Select("SELECT permission FROM permission WHERE pid IN (SELECT pid FROM role_permission WHERE rid IN (SELECT rid FROM user_role WHERE uid IN (SELECT user_id FROM `user` WHERE use_sys_name = #{userId})))")
    List<Permission> findPermission(@Param("userId") String userId);

    /*
    获取组织结构
     */
    @Select("SELECT dp_id AS id,dept_name AS name,(case when par_id is null or par_id = '' then '-1' else par_id end) AS pid FROM department UNION ALL select user_id as id,user_name as name,dept_id as pid from `user`")
    List<BaseSelect> selectUserList();

    /*
      根据角色ID删除用户与角色的关联
     */
    @Delete("delete from user_role where rid=#{rid}")
    void deleteUserRole(@Param("rid") String rid);

    /*
   获取权限架构，用combotree展示
    */
    @Select("SELECT pid as id,modelname as name ,(case when par_id is null or par_id = '' then '-1' else par_id end) AS pid from permission")
    List<BaseSelect> selectPermissionList();

    /*
  获取权限架构，用ztree展示
   */
    @Select("SELECT pid as id,modelname as name ,(case when par_id is null or par_id = '' then '-1' else par_id end) AS pid from permission")
    List<BaseSelect> zTreePerList();

    /*
     根据角色ID删除角色与权限的关联
    */
    @Delete("delete from role_permission where rid=#{rid}")
    void deleteRolePermission(@Param("rid") String rid);

    /*
    获取表单列表
   */
    @Select("SELECT * FROM (SELECT * FROM form_info where form_id not in (select form_id from form_info where is_delete=1) ORDER BY create_time desc limit 100000) as t GROUP BY t.form_id")
    List<FormInfo> formList();

    /*
  自定义表单根据uuid删除
 */
    @Update("update form_info set is_delete = 1 where uuid =#{uuids} ")
    void formDelete(@Param("uuids") String uuids);

}
