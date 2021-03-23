package com.cn.psys.system.mapper;

import com.cn.psys.mybatisplus.BaseSelect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cn.psys.system.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhuwei
 * @since 2020-03-17
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    /**
     *
     * 如果自定义的方法还希望能够使用MP提供的Wrapper条件构造器，则需要如下写法
     * @return
     */
    @Select("SELECT dp_id AS id,dept_name AS name,(case when par_id is null or par_id = '' then '-1' else par_id end) AS pid FROM department")
    List<BaseSelect> selectByMyWrapper(@Param("map") Map<String, Object> map);


}
