package com.wanghang.projectgateway.model.to.user;

import lombok.Data;

/**
 * @author wanghang
 * 2020/5/15
 **/
@Data
public class UserLoginTo {
    private String phone;
    private String password;
}
