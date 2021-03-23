package com.cn.psys.system.controller;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cn.psys.system.entity.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.psys.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cn.psys.base.BaseResponse;
import com.cn.psys.mybatisplus.MybatisPlusQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
/**
* <p>
    *  前端控制器
    * </p>
*
* @author shenl
* @since 2020-03-12
*/
@RestController
@RequestMapping("/user")
    public class UserController {
    private IUserService targetService;

    @Autowired
    public UserController(IUserService targetService){
        this.targetService = targetService;
    }


    /**
    * showdoc
    * @catalog User
    * @title 分页列表
    * @description 分页列表接口
    * @method post
    * @url http://localhost/user/sys-user/list
    * @param page 非必选 string 页数（不传默认第一页）
    * @param rows 非必选 string 行数（不传默认一页10行）
    * @remark 无
    * @number 1
    */
   // @RequiresPermissions("User:list")
    @RequestMapping("/list")
    @ResponseBody
    public Map<String,Object> findListByPage(@RequestParam(required=false) String paramData,@RequestParam(name = "page", defaultValue = "1") int pageIndex,@RequestParam(name = "rows", defaultValue = "10") int step){
        QueryWrapper<User> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        Page page = new Page(pageIndex,step);
        Map<String,Object> map = new HashMap<>();
        IPage obj = targetService.page(page,queryWrapper);
        map.put("total", obj.getTotal());
        map.put("rows",obj.getRecords());
        return map;
    }


    /**
    * showdoc
    * @catalog User
    * @title 所有数据列表
    * @description 所有数据列表
    * @method post
    * @url http://localhost/user/sys-user/all
    * @param 无 无 无 无
    * @remark 无
    * @number 2
    */
  //  @RequiresPermissions("User:all")
    @RequestMapping("/all")
    @ResponseBody
    public BaseResponse findAll(@RequestParam(required=false) String paramData) {
        QueryWrapper<User> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        return BaseResponse.success(targetService.list(queryWrapper));
     }


    /**
    * showdoc
    * @catalog User
    * @title 详情页面
    * @description 详情页面接口
    * @method post
    * @url http://localhost/user/sys-user/find
    * @param id 必选 Long 页数
    * @remark 无
    * @number 3
    */
   // @RequiresPermissions("User:find")
    @RequestMapping("/find")
    @ResponseBody
    public BaseResponse find(@RequestParam(required=false) String paramData) {
        //System.out.println(paramData);
        QueryWrapper<User> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        User User = null;
        try{
            User = targetService.getOne(queryWrapper,true);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (User == null) {
            return BaseResponse.error("无查询结果");
        }
        return BaseResponse.success(User);
    }

    /**
    * showdoc
    * @catalog User
    * @title 新增数据
    * @description 新增数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
    * @param User 必选 User 保存对象
    * @remark 无
    * @number 4
    */
  //  @RequiresPermissions("User:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse addItem(@RequestBody User User){
        User.setCreateTime(DateUtil.now());
        User.setUserId(IdUtil.randomUUID());
        boolean isOk = targetService.save(User);
        if(isOk){
            return BaseResponse.success("数据添加成功！");
        }
            return BaseResponse.error("数据添加失败");
    }


    /**
    * showdoc
    * @catalog User
    * @title 更新数据
    * @description 更新数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
     *
    * @param User 必选 User 保存对象
    * @remark 无
    * @number 5
    */
   // @RequiresPermissions("User:update")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse updateItem(@RequestBody User User){
        boolean isOk = targetService.updateById(User);
        if(isOk){
            return BaseResponse.success("数据更改成功！");
        }
            return BaseResponse.error("数据更改失败");
    }


    /**
    * showdoc
    * @catalog User
    * @title 删除数据
    * @description 删除数据接口
    * @method post
    * @url http://localhost/user/sys-user/del
    * @param ids 必选 List<Long> 页数
        * @remark 无
        * @number 7
        */
       // @RequiresPermissions("User:del")
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

