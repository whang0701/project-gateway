package com.wanghang.projectgateway.model.vo.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ProductListQueryVo {

    private String no;

    private Integer source;

    private String sourceName;

    private String name;

    private String typeName;

    private String brand;

    private BigDecimal price;

    private String img0;

    private BigDecimal star;

    private List<String> tags;

}