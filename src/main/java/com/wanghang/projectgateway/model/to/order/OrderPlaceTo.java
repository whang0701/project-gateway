package com.wanghang.projectgateway.model.to.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderPlaceTo {

    private String userNo;

    private String productNo;

    private Integer productSource;

    private Integer num;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private String receiverNo;

}