package com.wanghang.projectgateway.controller;

import com.wanghang.projectgateway.model.to.user.UserInfoTo;
import com.wanghang.projectgateway.model.to.user.UserLoginTo;
import com.wanghang.projectgateway.model.to.user.UserRegisterTo;
import com.wanghang.projectgateway.model.to.user.UserUpdateTo;
import com.wanghang.projectgateway.model.vo.user.UserInfoVo;
import com.wanghang.projectgateway.model.vo.user.UserLoginVo;
import com.wanghang.projectgateway.service.UserService;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wanghang
 * 2020/5/15
 **/
@RestController
@RequestMapping("api/user/")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Response<UserLoginVo> login(@RequestBody UserLoginTo to) {
        try {
            return new Response<>(userService.login(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Response register(@RequestBody UserRegisterTo to) {
        try {
            userService.register(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/getInfo", method = RequestMethod.POST)
    public Response<UserInfoVo> getInfo(@RequestBody UserInfoTo to) {
        try {
            return new Response<>(userService.getInfo(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/uploadImg", method = RequestMethod.POST)
    public Response<String> uploadImg(MultipartFile file) {
        try {
            userService.uploadImg(file);
            return new Response<>(CodeType.OK);
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response update(@RequestBody UserUpdateTo to) {
        try {
            userService.update(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }
}
