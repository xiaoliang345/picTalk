package com.oxn.aiPicturesStore.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户实体类") // 描述整个实体类
public class UserLoginRequest {

    @ApiModelProperty(value = "账号")
    private String userAccount;
    @ApiModelProperty(value = "密码")
    private String userPassword;

}
