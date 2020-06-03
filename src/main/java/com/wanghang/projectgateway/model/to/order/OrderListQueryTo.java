package com.wanghang.projectgateway.model.to.order;

import lombok.Data;

@Data
public class OrderListQueryTo {

    private String userNo;

    private Integer option;

    private Integer skip = 0;
    private Integer limit = 5;

}