package com.wanghang.projectgateway.service;


import com.google.common.collect.Lists;
import com.wanghang.projectgateway.model.to.remark.RemarkListQueryTo;
import com.wanghang.projectgateway.model.to.remark.RemarkQueryTo;
import com.wanghang.projectgateway.model.vo.remark.RemarkListQueryVo;
import com.wanghang.projectgateway.model.vo.remark.RemarkQueryVo;
import com.wanghang.projectsdk.base.dao.RemarkMapper;
import com.wanghang.projectsdk.base.entity.Order;
import com.wanghang.projectsdk.base.entity.Remark;
import com.wanghang.projectsdk.base.entity.RemarkExample;
import com.wanghang.projectsdk.base.enumeration.ProductSourceType;
import com.wanghang.projectsdk.base.enumeration.StateFlagType;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import com.wanghang.projectsdk.third.controller.IRemarkController;
import com.wanghang.projectsdk.third.factory.FeignClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wanghang
 * 2020/5/15
 **/
@Service
@Slf4j
public class RemarkService {

    @Autowired
    private RemarkMapper remarkMapper;

    public List<RemarkListQueryVo> getListByProductNoAndSkipAndLimit(RemarkListQueryTo to) {
        if (Objects.isNull(to.getProductNo()) || Objects.isNull(to.getSource())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        List<Remark> list = Lists.newArrayList();
        ProductSourceType type = ProductSourceType.getType(to.getSource());
        if (Objects.isNull(type)) {
            log.error("未识别该渠道source:{}", to.getSource());
            throw new ServiceException(CodeType.BUSINESS_ERROR, "未识别该渠道");
        }
        IRemarkController remarkController = FeignClientFactory.createRemarkFeign(type);
        if (Objects.isNull(remarkController)) {
            log.error("未获取到该渠道服务:{}", type.getValue());
            throw new ServiceException(CodeType.SYSTEM_ERROR, "未获取该渠道服务");
        }
        list.addAll(remarkController.getListByProductNoAndSkipAndLimit(to.getProductNo(), to.getSkip(), to.getLimit()));
        // 转换为vo
        return list.stream().map(this::convertToRemarkListQueryVo).collect(Collectors.toList());
    }

    private RemarkListQueryVo convertToRemarkListQueryVo(Remark remark) {
        RemarkListQueryVo vo = new RemarkListQueryVo();
        BeanUtils.copyProperties(remark, vo);
        vo.setDate(DateFormatUtils.format(remark.getCreateTime(), "yyyy-MM-dd HH:mm", Locale.CHINA));
        return vo;
    }

    public void doRemark(Order order, BigDecimal star, String content) {
        Remark remark = buildRemark(order, star, content);
        // 调用第三方下单接口获取第三方订单号
        ProductSourceType type = ProductSourceType.getType(order.getProductSource());
        if (Objects.isNull(type)) {
            log.error("未识别该渠道option:{}", order.getProductSource());
            throw new ServiceException(CodeType.BUSINESS_ERROR, "未识别该渠道");
        }
        IRemarkController remarkController = FeignClientFactory.createRemarkFeign(type);
        if (Objects.isNull(remarkController)) {
            log.error("未获取到该渠道服务:{}", type.getValue());
            throw new ServiceException(CodeType.SYSTEM_ERROR, "未获取该渠道服务");
        }
        String remarkNo = remarkController.doRemark(remark);
        if (StringUtils.isBlank(remarkNo)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "评价失败");
        }
        // 补全订单
        remark.setNo(remarkNo);
        remark.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        // 入库
        if (!Objects.equals(remarkMapper.insertSelective(remark), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR);
        }
    }

    private Remark buildRemark(Order order, BigDecimal star, String content) {
        Remark remark = new Remark();
        remark.setUserNo(order.getUserNo());
        remark.setOrderNo(order.getNo());
        remark.setProductNo(order.getProductNo());
        remark.setProductSource(order.getProductSource());
        remark.setProductName(order.getProductName());
        remark.setStar(star);
        remark.setStateFlag(StateFlagType.VALID.getKey());
        remark.setContent(content);
        remark.setCreateTime(new Date());
        remark.setUpdateTime(new Date());

        return remark;
    }

    public RemarkQueryVo getOneByUserAndOrder(RemarkQueryTo to) {
        if (Objects.isNull(to.getUserNo()) || Objects.isNull(to.getSource()) || Objects.isNull(to.getOrderNo())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        ProductSourceType type = ProductSourceType.getType(to.getSource());
        if (Objects.isNull(type)) {
            log.error("未识别该渠道source:{}", to.getSource());
            throw new ServiceException(CodeType.BUSINESS_ERROR, "未识别该渠道");
        }
        RemarkExample example = new RemarkExample();
        example.createCriteria()
                .andUserNoEqualTo(to.getUserNo())
                .andOrderNoEqualTo(to.getOrderNo())
                .andProductSourceEqualTo(to.getSource())
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<Remark> list = remarkMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(list)) {
            log.info("该用户未评价");
            return null;
        }
        // 转换为vo
        return convertToRemarkQueryVo(list.get(0));
    }

    private RemarkQueryVo convertToRemarkQueryVo(Remark remark) {
        RemarkQueryVo vo = new RemarkQueryVo();
        BeanUtils.copyProperties(remark, vo);
        vo.setDate(DateFormatUtils.format(remark.getCreateTime(), "yyyy-MM-dd HH:mm", Locale.CHINA));
        return vo;
    }
}
