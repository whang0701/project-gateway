package com.wanghang.projectgateway.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderListQueryVo {
    private String no;

    private String productNo;

    private Integer productSource;

    private String sourceName;

    private String productName;

    private String productBrand;

    private Integer productType;

    private String typeName;

    private String productImg;

    private Integer num;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private Integer status;

    private String statusName;

    private String store;
}