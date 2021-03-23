package com.cn.psys.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cn.psys.system.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shenl
 * @since 2020-03-12
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
