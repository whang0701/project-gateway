package com.wanghang.projectgateway.controller;


import com.wanghang.projectgateway.model.to.product.ProductDetailQueryTo;
import com.wanghang.projectgateway.model.to.product.ProductListQueryTo;
import com.wanghang.projectgateway.model.to.remark.RemarkListQueryTo;
import com.wanghang.projectgateway.model.to.remark.RemarkQueryTo;
import com.wanghang.projectgateway.model.vo.product.ProductDetailQueryVo;
import com.wanghang.projectgateway.model.vo.product.ProductListQueryVo;
import com.wanghang.projectgateway.model.vo.remark.RemarkListQueryVo;
import com.wanghang.projectgateway.model.vo.remark.RemarkQueryVo;
import com.wanghang.projectgateway.service.ProductService;
import com.wanghang.projectgateway.service.RemarkService;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author wanghang
 * 2020/5/15
 **/
@RestController
@RequestMapping("api/remark/")
public class RemarkController {
    @Autowired
    private RemarkService remarkService;

    @RequestMapping(value = "/getListByProductNoAndSkipAndLimit", method = RequestMethod.POST)
    public Response<List<RemarkListQueryVo>> getListByProductNoAndSkipAndLimit(@RequestBody RemarkListQueryTo to) {
        try {
            return new Response<>(remarkService.getListByProductNoAndSkipAndLimit(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/getOneByUserAndOrder", method = RequestMethod.POST)
    public Response<RemarkQueryVo> getOneByUserAndOrder(@RequestBody RemarkQueryTo to) {
        try {
            return new Response<>(remarkService.getOneByUserAndOrder(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }
}
