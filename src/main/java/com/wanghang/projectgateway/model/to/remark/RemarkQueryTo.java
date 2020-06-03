package com.wanghang.projectgateway.model.to.remark;

import lombok.Data;

/**
 * @author wanghang
 * 2020/5/25
 **/
@Data
public class RemarkQueryTo {
    private String userNo;
    private String orderNo;
    private Integer source;
}
