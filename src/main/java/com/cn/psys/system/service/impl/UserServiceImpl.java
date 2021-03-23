package com.cn.psys.system.service.impl;

import com.cn.psys.system.entity.User;
import com.cn.psys.system.mapper.UserMapper;
import com.cn.psys.system.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shenl
 * @since 2020-03-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
