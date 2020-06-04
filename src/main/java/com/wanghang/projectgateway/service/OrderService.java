package com.wanghang.projectgateway.service;


import com.alibaba.fastjson.JSON;
import com.wanghang.projectgateway.model.to.order.*;
import com.wanghang.projectgateway.model.vo.order.OrderListQueryVo;
import com.wanghang.projectgateway.model.vo.order.OrderPlaceVo;
import com.wanghang.projectgateway.model.vo.order.OrderQueryVo;
import com.wanghang.projectsdk.base.dao.OrderMapper;
import com.wanghang.projectsdk.base.entity.Order;
import com.wanghang.projectsdk.base.entity.OrderExample;
import com.wanghang.projectsdk.base.entity.Product;
import com.wanghang.projectsdk.base.entity.Receiver;
import com.wanghang.projectsdk.base.enumeration.OrderStatusType;
import com.wanghang.projectsdk.base.enumeration.ProductSourceType;
import com.wanghang.projectsdk.base.enumeration.ProductType;
import com.wanghang.projectsdk.base.enumeration.StateFlagType;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.third.controller.IOrderController;
import com.wanghang.projectsdk.third.factory.ThirdSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
public class OrderService {
    @Autowired
    private ReceiverService receiverService;
    @Autowired
    private ProductService productService;
    @Autowired
    private RemarkService remarkService;
    @Autowired
    private OrderMapper orderMapper;

    public OrderPlaceVo place(OrderPlaceTo to) {
        // 查询收货信息
        Receiver receiver = receiverService.getOneByNo(to.getReceiverNo());
        // 获取商品详情
        Product product = productService.getProductByNoAndSource(to.getProductNo(), to.getProductSource());
        if (Objects.isNull(product)) {
            throw new ServiceException(CodeType.THIRD_ERROR, "未查询到第三方商品");
        }
        // 比较价格
        if (to.getPrice().compareTo(product.getPrice()) != 0) {
            log.info("查询到商品价格为:{}", to.getPrice());
            throw new ServiceException(CodeType.THIRD_ERROR, "商品价格已变化，请刷新");
        }
        BigDecimal totalPrice = to.getPrice().multiply(BigDecimal.valueOf(to.getNum()));
        if (to.getTotalPrice().compareTo(totalPrice) != 0) {
            log.info("计算得到订单总价:{}, 传入订单总价:{}", totalPrice, to.getTotalPrice());
            throw new ServiceException(CodeType.THIRD_ERROR, "校验订单价格出错，请刷新");
        }
        // 封装order
        Order order = buildInitOrder(to, receiver, product);
        // 调用第三方下单接口获取第三方订单号
        ProductSourceType type = ProductSourceType.getType(to.getProductSource());
        if (Objects.isNull(type)) {
            log.error("未识别该渠道option:{}", to.getProductSource());
            throw new ServiceException(CodeType.BUSINESS_ERROR, "未识别该渠道");
        }
        IOrderController orderController = ThirdSourceFactory.createSourceFactory(type).createOrderFeign();
        if (Objects.isNull(orderController)) {
            log.error("未获取到该渠道服务:{}", type.getValue());
            throw new ServiceException(CodeType.SYSTEM_ERROR, "未获取该渠道服务");
        }
        String orderNo = orderController.doPlace(order);
        if (StringUtils.isBlank(orderNo)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "下单失败");
        }
        // 补全订单
        order.setNo(orderNo);
        order.setStatus(OrderStatusType.BEFORE_DELIVERY.getKey());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        // 入库
        if (!Objects.equals(orderMapper.insertSelective(order), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR);
        }
        OrderPlaceVo vo = new OrderPlaceVo();
        vo.setId(order.getId());
        vo.setNo(order.getNo());
        vo.setSource(order.getProductSource());
        return vo;
    }

    private Order buildInitOrder(OrderPlaceTo to, Receiver receiver, Product product) {
        Order order = new Order();
        order.setNum(to.getNum());
        order.setPrice(to.getPrice());
        order.setProductBrand(product.getBrand());
        order.setProductImg(product.getImg0());
        order.setProductName(product.getName());
        order.setProductNo(product.getNo());
        order.setProductSource(product.getSource());
        order.setProductType(product.getType());
        order.setReceiverAddress(receiver.getAddress());
        order.setReceiverName(receiver.getName());
        order.setReceiverNo(receiver.getNo());
        order.setReceiverPhone(receiver.getPhone());
        order.setStateFlag(StateFlagType.VALID.getKey());
        order.setStatus(OrderStatusType.INIT.getKey());
        order.setStore(product.getStore());
        order.setTotalPrice(to.getTotalPrice());
        order.setUserNo(to.getUserNo());

        return order;
    }

    public OrderQueryVo getOneByNoAndSource(OrderQueryTo to) {
        if (StringUtils.isBlank(to.getNo()) || Objects.isNull(to.getSource())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        Order order = getOrderBySourceAndNo(to.getNo(), to.getSource());
        OrderQueryVo vo = new OrderQueryVo();
        BeanUtils.copyProperties(order, vo);
        vo.setSourceName(ProductSourceType.getType(order.getProductSource()).getValue());
        vo.setTypeName(ProductType.getType(order.getProductType()).getValue());
        vo.setStatusName(OrderStatusType.getType(order.getStatus()).getValue());
        vo.setCreateTime(DateFormatUtils.format(order.getCreateTime(), "yyyy-MM-dd HH:mm", Locale.CHINA));
        vo.setUpdateTime(DateFormatUtils.format(order.getUpdateTime(), "yyyy-MM-dd HH:mm", Locale.CHINA));
        return vo;
    }

    /**
     * option: -1未完成  0已完成  1退款单
     */
    public List<OrderListQueryVo> getListByOption(OrderListQueryTo to) {
        if (StringUtils.isBlank(to.getUserNo()) || Objects.isNull(to.getOption())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        int statusStart = 0;
        int statusEnd = 0;
        if (Objects.equals(to.getOption(), -1)) {
            // 未完成
            statusStart = 0;
            statusEnd = 3;
        } else if (Objects.equals(to.getOption(), 0)) {
            // 已完成
            statusStart = 4;
            statusEnd = 5;
        } else if (Objects.equals(to.getOption(), 1)) {
            // 退款单
            statusStart = 6;
            statusEnd = 7;
        } else {
            throw new ServiceException(CodeType.PARAMS_ERROR, "未识别的option");
        }
        List<Order> list = orderMapper.selectByUserNoAndStatusAndLimitOrderByDesc(to.getUserNo(), statusStart, statusEnd,
                StateFlagType.VALID.getKey(), to.getSkip(), to.getLimit(), "update_time");
        // 转换为vo
        return list.stream().map(order -> {
            OrderListQueryVo vo = new OrderListQueryVo();
            BeanUtils.copyProperties(order, vo);
            vo.setSourceName(ProductSourceType.getType(order.getProductSource()).getValue());
            vo.setTypeName(ProductType.getType(order.getProductType()).getValue());
            vo.setStatusName(OrderStatusType.getType(order.getStatus()).getValue());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     *
     * @param source ProductSourceType
     * @param no 第三方订单号
     * @param status 订单状态 OrderStatusType
     */
    public void notify(Integer source, String no, Integer status) {
        log.info("第三方通知系统-订单状态变更:source:{},no:{},status:{}", source, no, status);
        OrderStatusType nextStatusType = OrderStatusType.getType(status);
        if (Objects.isNull(nextStatusType)) {
            throw new ServiceException(CodeType.BUSINESS_ERROR, "未识别的订单状态");
        }
        Order order = getOrderBySourceAndNo(no, source);
        // 校验订单状态
        OrderStatusType lastStatusType = OrderStatusType.getType(order.getStatus());
        if (!Objects.equals(lastStatusType.getKey() + 1, nextStatusType.getKey())) {
            log.error("订单原状态:{}, 订单新状态:{}", JSON.toJSONString(lastStatusType), JSON.toJSONString(nextStatusType));
            throw new ServiceException(CodeType.THIRD_ERROR, "订单状态越级");
        }
        Order newOrder = new Order();
        newOrder.setStatus(nextStatusType.getKey());
        newOrder.setUpdateTime(new Date());
        newOrder.setId(order.getId());
        if (!Objects.equals(orderMapper.updateByPrimaryKeySelective(newOrder), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR, "更新订单数据库异常");
        }
    }

    public void receive(OrderReceiveTo to) {
        Order order = getOrderBySourceAndNo(to.getNo(), to.getSource());
        // 校验订单状态
        OrderStatusType lastStatusType = OrderStatusType.getType(order.getStatus());
        if (!Objects.equals(lastStatusType, OrderStatusType.BEFORE_RECEIVE)) {
            log.error("订单状态不可收货,当前订单状态:{}", JSON.toJSONString(lastStatusType));
            throw new ServiceException(CodeType.THIRD_ERROR, "订单非待收货状态，不可收货");
        }
        // 调用第三方接口收货
        IOrderController orderController = ThirdSourceFactory.createSourceFactory(ProductSourceType.getType(to.getSource())).createOrderFeign();
        if (!Objects.equals(orderController.doReceive(order.getNo()), OrderStatusType.RCEIVED.getKey())) {
            throw new ServiceException(CodeType.THIRD_ERROR, "第三方订单收货失败");
        }
        // 更新数据库
        Order newOrder = new Order();
        newOrder.setStatus(OrderStatusType.RCEIVED.getKey());
        newOrder.setUpdateTime(new Date());
        newOrder.setId(order.getId());
        if (!Objects.equals(orderMapper.updateByPrimaryKeySelective(newOrder), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR, "更新订单数据库异常");
        }
    }

    public void remark(OrderRemarkTo to) {
        Order order = getOrderBySourceAndNo(to.getNo(), to.getSource());
        // 校验订单状态
        OrderStatusType lastStatusType = OrderStatusType.getType(order.getStatus());
        if (!Objects.equals(lastStatusType, OrderStatusType.RCEIVED)) {
            log.error("订单状态不可评价,当前订单状态:{}", JSON.toJSONString(lastStatusType));
            throw new ServiceException(CodeType.THIRD_ERROR, "订单非已收货状态，不可评价");
        }
        // 写评价
        remarkService.doRemark(order, to.getStar(), to.getContent());
        // 调用第三方接口评价
        IOrderController orderController = ThirdSourceFactory.createSourceFactory(ProductSourceType.getType(to.getSource())).createOrderFeign();
        if (!Objects.equals(orderController.doRemark(order.getNo()), OrderStatusType.REMARKED.getKey())) {
            throw new ServiceException(CodeType.THIRD_ERROR, "第三方订单评价失败");
        }
        // 更新数据库
        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setStatus(OrderStatusType.REMARKED.getKey());
        newOrder.setUpdateTime(new Date());
        if (!Objects.equals(orderMapper.updateByPrimaryKeySelective(newOrder), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR, "更新订单数据库异常");
        }
    }

    public void refund(OrderRefundTo to) {
        Order order = getOrderBySourceAndNo(to.getNo(), to.getSource());
        // 校验订单状态
        OrderStatusType lastStatusType = OrderStatusType.getType(order.getStatus());
        if (!Objects.equals(lastStatusType, OrderStatusType.RCEIVED) && !Objects.equals(lastStatusType, OrderStatusType.REMARKED)) {
            log.error("订单状态不可退单,当前订单状态:{}", JSON.toJSONString(lastStatusType));
            throw new ServiceException(CodeType.THIRD_ERROR, "订单非已收货/已评价状态，不可退单");
        }
        // 调用第三方接口退单
        IOrderController orderController = ThirdSourceFactory.createSourceFactory(ProductSourceType.getType(to.getSource())).createOrderFeign();
        if (!Objects.equals(orderController.doRefund(order.getNo(), to.getContent()), OrderStatusType.REFUNDIND.getKey())) {
            throw new ServiceException(CodeType.THIRD_ERROR, "第三方订单退款申请失败");
        }
        // 更新数据库
        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setStatus(OrderStatusType.REFUNDIND.getKey());
        newOrder.setDesc(to.getContent());
        newOrder.setUpdateTime(new Date());
        if (!Objects.equals(orderMapper.updateByPrimaryKeySelective(newOrder), 1)) {
            throw new ServiceException(CodeType.DATABASE_ERROR, "更新订单数据库异常");
        }
    }


    private Order getOrderBySourceAndNo(String no, Integer source) {
        ProductSourceType sourceType = ProductSourceType.getType(source);
        if (Objects.isNull(sourceType)) {
            throw new ServiceException(CodeType.PARAMS_ERROR, "未识别的第三方来源");
        }
        OrderExample example = new OrderExample();
        example.createCriteria()
                .andNoEqualTo(no)
                .andProductSourceEqualTo(source)
                .andStateFlagEqualTo(StateFlagType.VALID.getKey());
        List<Order> list = orderMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(CodeType.DATABASE_ERROR, "未查询到订单");
        }
        return list.get(0);
    }

}
