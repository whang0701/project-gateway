package com.wanghang.projectgateway.controller;


import com.wanghang.projectgateway.model.to.order.*;
import com.wanghang.projectgateway.model.to.receiver.ReceiverAddTo;
import com.wanghang.projectgateway.model.to.receiver.ReceiverUpdateTo;
import com.wanghang.projectgateway.model.to.user.UserInfoTo;
import com.wanghang.projectgateway.model.vo.order.OrderListQueryVo;
import com.wanghang.projectgateway.model.vo.order.OrderPlaceVo;
import com.wanghang.projectgateway.model.vo.order.OrderQueryVo;
import com.wanghang.projectgateway.model.vo.receiver.ReceiverVo;
import com.wanghang.projectgateway.service.OrderService;
import com.wanghang.projectgateway.service.ReceiverService;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author wanghang
 * 2020/5/15
 **/
@RestController
@RequestMapping("api/order/")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/place", method = RequestMethod.POST)
    public Response<OrderPlaceVo> place(@RequestBody OrderPlaceTo to) {
        try {
            return new Response<>(orderService.place(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/getOneByNoAndSource", method = RequestMethod.POST)
    public Response<OrderQueryVo> getOneByNoAndSource(@RequestBody OrderQueryTo to) {
        try {
            return new Response<>(orderService.getOneByNoAndSource(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/getListByOption", method = RequestMethod.POST)
    public Response<List<OrderListQueryVo>> getListByOption(@RequestBody OrderListQueryTo to) {
        try {
            return new Response<>(orderService.getListByOption(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/notify", method = RequestMethod.GET)
    public Response notify(@RequestParam("source") Integer source, @RequestParam("no") String no, @RequestParam("status") Integer status) {
        try {
            orderService.notify(source, no, status);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            log.error("第三方通知订单状态变更-业务出错, source:{}, no:{}, status:{}",source, no, status, e);
            return new Response(CodeType.BUSINESS_ERROR);
        } catch (Exception e) {
            log.error("第三方通知订单状态变更-系统出错, source:{}, no:{}, status:{}",source, no, status, e);
            return new Response(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public Response receive(@RequestBody OrderReceiveTo to) {
        try {
            orderService.receive(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            log.error("订单收货失败, source:{}, no:{}",to.getSource(), to.getNo(), e);
            return new Response(CodeType.BUSINESS_ERROR);
        } catch (Exception e) {
            log.error("订单收货失败, source:{}, no:{}", to.getSource(), to.getNo(), e);
            return new Response(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/remark", method = RequestMethod.POST)
    public Response remark(@RequestBody OrderRemarkTo to) {
        try {
            orderService.remark(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            return new Response(CodeType.BUSINESS_ERROR);
        } catch (Exception e) {
            return new Response(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/refund", method = RequestMethod.POST)
    public Response refund(@RequestBody OrderRefundTo to) {
        try {
            orderService.refund(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            return new Response(CodeType.BUSINESS_ERROR);
        } catch (Exception e) {
            return new Response(CodeType.SYSTEM_ERROR);
        }
    }

}
