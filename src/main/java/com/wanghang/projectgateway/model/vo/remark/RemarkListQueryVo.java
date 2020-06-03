package com.wanghang.projectgateway.model.vo.remark;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RemarkListQueryVo {

    private BigDecimal star;

    private String content;

    private String date;

}