package com.wanghang.projectgateway.model.to.user;

import lombok.Data;

import java.util.Date;

/**
 * @author wanghang
 * 2020/5/15
 **/
@Data
public class UserUpdateTo {
    private String no;
    private String phone;
    private String name;
    private String email;
    private String sex;
    private String address;
    private String img;
    private Date birth;
    private String password;
    private String newPassword;
}
