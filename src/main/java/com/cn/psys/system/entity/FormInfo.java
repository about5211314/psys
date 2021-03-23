package com.cn.psys.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhuwei
 * @since 2020-04-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="FormInfo对象", description="")
public class FormInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "uuid", type = IdType.INPUT)
    private String uuid;

    @ApiModelProperty(value = "表单ID")
    private String formId;

    @ApiModelProperty(value = "表单名称")
    private String formName;

    @ApiModelProperty(value = "表单版本号")
    private String version;

    @ApiModelProperty(value = "表单组件信息")
    private String content;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;


}
