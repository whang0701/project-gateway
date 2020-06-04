package com.wanghang.projectgateway.service;


import com.google.common.collect.Lists;
import com.wanghang.projectgateway.model.to.product.ProductDetailQueryTo;
import com.wanghang.projectgateway.model.to.product.ProductListQueryTo;
import com.wanghang.projectgateway.model.vo.product.ProductDetailQueryVo;
import com.wanghang.projectgateway.model.vo.product.ProductListQueryVo;
import com.wanghang.projectsdk.base.entity.Product;
import com.wanghang.projectsdk.base.enumeration.ProductSourceType;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.third.controller.IProductController;
import com.wanghang.projectsdk.third.factory.ThirdSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
public class ProductService {

    /**
     * option: -1精选  0全部  其他ProductSourceType.key
     */
    public List<ProductListQueryVo> getListByNumAndOption(ProductListQueryTo to) {
        if (Objects.isNull(to.getOption())) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        List<Product> list = Lists.newArrayList();
        if (Objects.equals(to.getOption(), -1)) {
            // 精选
            list.addAll(getListFromAllSourceByNum(to.getNum()));
        } else if (Objects.equals(to.getOption(), 0)) {
            // 全部
            list.addAll(getListFromAllSourceByNum(to.getNum()));
        } else {
            // 某一渠道
            list.addAll(getListByNumAndSource(to.getNum(), to.getOption()));
        }
        // 转换为vo
        return list.stream().map(this::convertToProductListQueryVo).collect(Collectors.toList());
    }

    private List<Product> getListFromAllSourceByNum(int totalNum) {
        List<Product> list = Lists.newArrayList();
        int size = ProductSourceType.values().length;
        int i = 0;
        for (ProductSourceType type : ProductSourceType.values()) {
            i++;
            IProductController productController = ThirdSourceFactory.createSourceFactory(type).createProductFeign();
            if (Objects.isNull(productController)) {
                log.warn("未获取到该渠道服务:{}", type.getValue());
                continue;
            }
            int num;
            if (i == size) {
                num = totalNum;
            } else {
                num = (int) (System.currentTimeMillis() % totalNum);
            }
            totalNum = totalNum - num;
            list.addAll(productController.getListByRandom(num));
        }
        return list;
    }

    private List<Product> getListByNumAndSource(Integer num, Integer sourceKey) {
        List<Product> list = Lists.newArrayList();
        ProductSourceType type = ProductSourceType.getType(sourceKey);
        if (Objects.isNull(type)) {
            log.error("未识别该渠道option:{}", sourceKey);
            throw new ServiceException(CodeType.BUSINESS_ERROR, "未识别该渠道");
        }
        IProductController productController = ThirdSourceFactory.createSourceFactory(type).createProductFeign();
        if (Objects.isNull(productController)) {
            log.error("未获取到该渠道服务:{}", type.getValue());
            throw new ServiceException(CodeType.SYSTEM_ERROR, "未获取该渠道服务");
        }
        list.addAll(productController.getListByRandom(num));
        return list;
    }

    private ProductListQueryVo convertToProductListQueryVo(Product product) {
        ProductListQueryVo vo = new ProductListQueryVo();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }


    public ProductDetailQueryVo getDetailByNoAndSource(ProductDetailQueryTo to) {
        return convertToProductDetailQueryVo(getProductByNoAndSource(to.getNo(), to.getSource()));
    }

    public Product getProductByNoAndSource(String no, Integer source) {
        if (StringUtils.isBlank(no) || Objects.isNull(source)) {
            throw new ServiceException(CodeType.PARAMS_ERROR);
        }
        ProductSourceType type = ProductSourceType.getType(source);
        if (Objects.isNull(type)) {
            log.error("未识别该渠道source:{}", source);
            throw new ServiceException(CodeType.BUSINESS_ERROR, "未识别该渠道");
        }
        IProductController productController = ThirdSourceFactory.createSourceFactory(type).createProductFeign();
        if (Objects.isNull(productController)) {
            log.error("未获取到该渠道服务:{}", type.getValue());
            throw new ServiceException(CodeType.SYSTEM_ERROR, "未获取该渠道服务");
        }
        return productController.getOneByNo(no);
    }

    private ProductDetailQueryVo convertToProductDetailQueryVo(Product product) {
        ProductDetailQueryVo vo = new ProductDetailQueryVo();
        BeanUtils.copyProperties(product, vo);
        vo.setBirth(DateFormatUtils.format(product.getBirth(), "yyyy-MM-dd", Locale.CHINA));
        return vo;
    }
}
