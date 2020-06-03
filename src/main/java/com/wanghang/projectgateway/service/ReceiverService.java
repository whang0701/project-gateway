package com.wanghang.projectgateway.service;


import com.wanghang.projectgateway.model.to.receiver.ReceiverAddTo;
import com.wanghang.projectgateway.model.to.receiver.ReceiverUpdateTo;
import com.wanghang.projectgateway.model.vo.receiver.ReceiverVo;
import com.wanghang.projectsdk.base.dao.ReceiverMapper;
import com.wanghang.projectsdk.base.entity.Receiver;
import com.wanghang.projectsdk.base.entity.ReceiverExample;
import com.wanghang.projectsdk.base.enumeration.StateFlagType;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author wanghang
 * 2020/5/15
 **/
@Service
public class ReceiverService {
    @Autowired
    private ReceiverMapper receiverMapper;

    public Receiver getOneByNo(String no) {
        if (StringUtils.isBlank(no)) {
            throw new ServiceException(CodeType.PARAMS_ERROR, "请选择收货信息");
        }
        ReceiverExample example = new ReceiverExample();
        example.createCriteria()
                .andNoEqualTo(no)
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<Receiver> list = receiverMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.PARAMS_ERROR, "未查询到该收货信息");
        }
        return list.get(0);
    }

    public List<ReceiverVo> getList(String userNo) {
        if (StringUtils.isBlank(userNo)) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        ReceiverExample example = new ReceiverExample();
        example.createCriteria()
                .andUserNoEqualTo(userNo)
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<Receiver> list = receiverMapper.selectByExample(example);

        return list.stream().map(receiver -> {
            ReceiverVo vo = new ReceiverVo();
            BeanUtils.copyProperties(receiver, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    public void add(ReceiverAddTo to) {
        if (StringUtils.isBlank(to.getUserNo())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        Receiver receiver = new Receiver();
        BeanUtils.copyProperties(to, receiver);
        receiver.setNo(UUID.randomUUID().toString());
        receiver.setStateFlag(StateFlagType.VALID.getKey());
        receiver.setCreateTime(new Date());
        receiver.setUpdateTime(new Date());
        if (!Objects.equals(receiverMapper.insertSelective(receiver), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR);
        }
    }

    public void delete(ReceiverUpdateTo to) {
        if (StringUtils.isBlank(to.getNo())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        // 校验是否存在
        ReceiverExample example = new ReceiverExample();
        example.createCriteria()
                .andNoEqualTo(to.getNo())
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<Receiver> list = receiverMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "收货信息不存在");
        }

        Receiver receiver = new Receiver();
        receiver.setStateFlag(StateFlagType.INVALID.getKey());
        receiver.setUpdateTime(new Date());
        receiver.setId(list.get(0).getId());
        if (!Objects.equals(receiverMapper.updateByPrimaryKeySelective(receiver), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR);
        }
    }

    public void update(ReceiverUpdateTo to)  {
        // 校验参数
        if (StringUtils.isBlank(to.getNo())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        // 校验是否存在
        ReceiverExample example = new ReceiverExample();
        example.createCriteria()
                .andNoEqualTo(to.getNo())
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<Receiver> list = receiverMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "收货信息不存在");
        }

        Receiver receiver = new Receiver();
        BeanUtils.copyProperties(to, receiver);
        receiver.setUpdateTime(new Date());
        receiver.setId(list.get(0).getId());
        if (!Objects.equals(receiverMapper.updateByPrimaryKeySelective(receiver), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR);
        }

    }
}
