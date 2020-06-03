package com.wanghang.projectgateway.model.vo.remark;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RemarkQueryVo {

    private BigDecimal star;

    private String content;

    private String date;

}