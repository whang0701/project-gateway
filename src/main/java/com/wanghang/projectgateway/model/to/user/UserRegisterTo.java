package com.wanghang.projectgateway.model.to.user;

import lombok.Data;

import java.util.Date;

/**
 * @author wanghang
 * 2020/5/15
 **/
@Data
public class UserRegisterTo {
    private String phone;
    private String password;
    private String name;
    private String email;
    private String sex;
    private String address;
    private Date birth;
}
