package ${package.Controller};
import org.springframework.web.bind.annotation.RequestMapping;
<#if restControllerStyle??>import org.springframework.web.bind.annotation.RestController;<#else>import org.springframework.stereotype.Controller;</#if>
<#if superControllerClassPackage??>import ${superControllerClassPackage};</#if>
import ${package.Service}.${table.serviceName};
import ${package.Entity}.${entity};
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.cn.psys.base.BaseResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.cn.psys.mybatisplus.MybatisPlusQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.hutool.core.util.IdUtil;
/**
* <p>
    * ${table.comment!} 前端控制器
    * </p>
*
* @author ${author}
* @since ${date}
*/
<#if restControllerStyle??>@RestController<#else>@Controller</#if>
@RequestMapping("<#if package.ModuleName??>/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
    class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
    public class ${table.controllerName} extends ${superControllerClass} {
<#else>
    public class ${table.controllerName} {
</#if>
</#if>
    private ${table.serviceName} targetService;

    @Autowired
    public ${table.controllerName}(${table.serviceName} targetService){
        this.targetService = targetService;
    }


    /**
    * showdoc
    * @catalog ${entity}
    * @title 分页列表
    * @description 分页列表接口
    * @method post
    * @url http://localhost/user/sys-user/list
    * @param page 非必选 string 页数（不传默认第一页）
    * @param rows 非必选 string 行数（不传默认一页10行）
    * @remark 无
    * @number 1
    */
    @RequiresPermissions("${entity}:list")
    @RequestMapping("/list")
    @ResponseBody
    public BaseResponse findListByPage(@RequestParam(required=false) String paramData,@RequestParam(name = "page", defaultValue = "1") int pageIndex,@RequestParam(name = "rows", defaultValue = "10") int step){
        QueryWrapper<${entity}> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        Page page = new Page(pageIndex,step);
        return BaseResponse.success(targetService.page(page,queryWrapper));
    }


    /**
    * showdoc
    * @catalog ${entity}
    * @title 所有数据列表
    * @description 所有数据列表
    * @method post
    * @url http://localhost/user/sys-user/all
    * @param 无 无 无 无
    * @remark 无
    * @number 2
    */
    @RequiresPermissions("${entity}:all")
    @RequestMapping("/all")
    @ResponseBody
    public BaseResponse findAll(@RequestParam(required=false) String paramData) {
        QueryWrapper<${entity}> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        return BaseResponse.success(targetService.list(queryWrapper));
     }


    /**
    * showdoc
    * @catalog ${entity}
    * @title 详情页面
    * @description 详情页面接口
    * @method post
    * @url http://localhost/user/sys-user/find
    * @param id 必选 Long 页数
    * @remark 无
    * @number 3
    */
    @RequiresPermissions("${entity}:find")
    @RequestMapping("/find")
    @ResponseBody
    public BaseResponse find(@RequestParam(required=false) String paramData) {
        QueryWrapper<${entity}> queryWrapper = MybatisPlusQuery.parseWhereSql(paramData);
        ${entity} ${entity} = null;
        try{
            ${entity} = targetService.getOne(queryWrapper,true);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (${entity} == null) {
            return BaseResponse.error("无查询结果");
        }
        return BaseResponse.success(${entity});
    }

    /**
    * showdoc
    * @catalog ${entity}
    * @title 新增数据
    * @description 新增数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
    * @param ${entity} 必选 ${entity} 保存对象
    * @remark 无
    * @number 4
    */
    @RequiresPermissions("${entity}:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse addItem(@RequestBody ${entity} ${entity}){
        String uuid = IdUtil.randomUUID();
        ${entity}.setUuid(uuid);
        boolean isOk = targetService.save(${entity});
        if(isOk){
            return BaseResponse.success(uuid);
        }
            return BaseResponse.error("数据添加失败");
    }


    /**
    * showdoc
    * @catalog ${entity}
    * @title 更新数据
    * @description 更新数据接口
    * @method post
    * @url http://localhost/user/sys-user/add
    * @param ${entity} 必选 ${entity} 保存对象
    * @remark 无
    * @number 5
    */
    @RequiresPermissions("${entity}:update")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse updateItem(@RequestBody ${entity} ${entity}){
        boolean isOk = targetService.updateById(${entity});
        if(isOk){
            return BaseResponse.success("数据更改成功！");
        }
            return BaseResponse.error("数据更改失败");
    }


    /**
    * showdoc
    * @catalog ${entity}
    * @title 删除数据
    * @description 删除数据接口
    * @method post
    * @url http://localhost/user/sys-user/del
    * @param ids 必选 List<Long> 页数
        * @remark 无
        * @number 7
        */
        @RequiresPermissions("${entity}:del")
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

