package com.cn.psys.activity.service;

import com.cn.psys.base.BaseResponse;

public interface IBaseService<T> {

     boolean save(T entity);

     boolean update(T entity);

     boolean remove(String[] ids);

     BaseResponse page(int page, int rows);
}
