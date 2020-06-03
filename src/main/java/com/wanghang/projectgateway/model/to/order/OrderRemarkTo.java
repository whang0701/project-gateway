package com.wanghang.projectgateway.model.to.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRemarkTo {

    private String no;

    private Integer source;

    private BigDecimal star;

    private String content;

}