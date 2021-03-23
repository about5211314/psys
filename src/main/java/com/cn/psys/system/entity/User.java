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
 * @since 2020-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="User对象", description="")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一ID")
    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    @ApiModelProperty(value = "用户名")
    private String useSysName;

    @ApiModelProperty(value = "姓名")
    private String userName;

    @ApiModelProperty(value = "密码，默认123")
    private String passWord;

    @ApiModelProperty(value = "所属部门")
    private String deptId;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "手机")
    private String mobilePhone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "加入时间")
    private String createTime;

    private String status;

    @ApiModelProperty(value = "salt")
    private String salt;


}
