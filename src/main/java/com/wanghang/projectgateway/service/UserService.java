package com.wanghang.projectgateway.service;


import com.wanghang.projectgateway.model.to.user.UserInfoTo;
import com.wanghang.projectgateway.model.to.user.UserLoginTo;
import com.wanghang.projectgateway.model.to.user.UserRegisterTo;
import com.wanghang.projectgateway.model.to.user.UserUpdateTo;
import com.wanghang.projectgateway.model.vo.user.UserInfoVo;
import com.wanghang.projectgateway.model.vo.user.UserLoginVo;
import com.wanghang.projectsdk.base.dao.UserMapper;
import com.wanghang.projectsdk.base.entity.User;
import com.wanghang.projectsdk.base.entity.UserExample;
import com.wanghang.projectsdk.base.enumeration.StateFlagType;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import com.wanghang.projectsdk.util.ImgUploadUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * @author wanghang
 * 2020/5/15
 **/
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public UserLoginVo login(UserLoginTo to) {
        if (StringUtils.isBlank(to.getPhone()) || StringUtils.isBlank(to.getPassword())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        UserExample example = new UserExample();
        example.createCriteria()
                .andPhoneEqualTo(to.getPhone())
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<User> list = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "该手机号未注册");
        }
        User user = list.get(0);
        if (!Objects.equals(user.getPassword(), to.getPassword())) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "密码不正确");
        }
        UserLoginVo vo = new UserLoginVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    public void register(UserRegisterTo to) {
        UserExample example = new UserExample();
        example.createCriteria()
                .andPhoneEqualTo(to.getPhone())
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<User> list = userMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "该手机号已被注册");
        }
        User user = new User();
        BeanUtils.copyProperties(to, user);
        user.setNo(UUID.randomUUID().toString());
        user.setStateFlag(StateFlagType.VALID.getKey());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        if (!Objects.equals(userMapper.insertSelective(user), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR);
        }
    }

    public UserInfoVo getInfo(UserInfoTo to) {
        if (StringUtils.isBlank(to.getNo())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        UserExample example = new UserExample();
        example.createCriteria()
                .andNoEqualTo(to.getNo())
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<User> list = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "该用户不存在");
        }
        User user = list.get(0);
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(user, vo);
        vo.setBirth(DateFormatUtils.format(user.getBirth(), "yyyy-MM-dd", Locale.CHINA));
        vo.setCreateTime(DateFormatUtils.format(user.getCreateTime(), "yyyy-MM-dd", Locale.CHINA));
        return vo;
    }

    public String uploadImg(MultipartFile file) throws IOException {

        return ImgUploadUtil.doUploadForUser(file);
    }

    public void update(UserUpdateTo to)  {
        // 校验参数
        if (StringUtils.isBlank(to.getNo())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        // 校验用户是否存在
        UserExample example = new UserExample();
        example.createCriteria()
                .andNoEqualTo(to.getNo())
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<User> list = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "该用户不存在");
        }
        // 校验修改的手机号是否已被注册
        if (StringUtils.isNotBlank(to.getPhone())) {
            UserExample example1 = new UserExample();
            example1.createCriteria()
                    .andPhoneEqualTo(to.getPhone())
                    .andStateFlagEqualTo(StateFlagType.VALID.getKey());
            List<User> list1 = userMapper.selectByExample(example1);
            if (!CollectionUtils.isEmpty(list1) && !Objects.equals(list1.get(0).getNo(), to.getNo())) {
                throw new ServiceException(CodeType.BUSINESS_ERROR, "新手机号已被注册");
            }
        }
        // 校验旧密码是否正确
        if (StringUtils.isNotBlank(to.getPassword())) {
            if (!Objects.equals(list.get(0).getPassword(), to.getPassword())) {
                throw new ServiceException(CodeType.BUSINESS_ERROR, "原密码不正确");
            }
            to.setPassword(to.getNewPassword());
        }

        User user = new User();
        BeanUtils.copyProperties(to, user);
        user.setUpdateTime(new Date());
        user.setId(list.get(0).getId());
        if (!Objects.equals(userMapper.updateByPrimaryKeySelective(user), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR);
        }


    }
}
