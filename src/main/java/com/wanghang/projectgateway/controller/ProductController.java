package com.wanghang.projectgateway.controller;


import com.alibaba.druid.sql.PagerUtils;
import com.wanghang.projectgateway.model.to.product.ProductDetailQueryTo;
import com.wanghang.projectgateway.model.to.product.ProductListQueryTo;
import com.wanghang.projectgateway.model.to.receiver.ReceiverAddTo;
import com.wanghang.projectgateway.model.to.receiver.ReceiverUpdateTo;
import com.wanghang.projectgateway.model.to.user.UserInfoTo;
import com.wanghang.projectgateway.model.vo.product.ProductDetailQueryVo;
import com.wanghang.projectgateway.model.vo.product.ProductListQueryVo;
import com.wanghang.projectgateway.model.vo.receiver.ReceiverVo;
import com.wanghang.projectgateway.service.ProductService;
import com.wanghang.projectgateway.service.ReceiverService;
import com.wanghang.projectsdk.base.entity.Product;
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
@RequestMapping("api/product/")
public class ProductController {
    @Autowired
    private ProductService productService;

    @RequestMapping(value = "/getListByNumAndOption", method = RequestMethod.POST)
    public Response<List<ProductListQueryVo>> getListByNumAndOption(@RequestBody ProductListQueryTo to) {
        try {
            return new Response<>(productService.getListByNumAndOption(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/getDetailByNoAndSource", method = RequestMethod.POST)
    public Response<ProductDetailQueryVo> getDetailByNoAndSource(@RequestBody ProductDetailQueryTo to) {
        try {
            return new Response<>(productService.getDetailByNoAndSource(to));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

}
