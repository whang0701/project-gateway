package com.wanghang.projectgateway.model.to.remark;

import lombok.Data;

/**
 * @author wanghang
 * 2020/5/25
 **/
@Data
public class RemarkListQueryTo {
    private String productNo;
    private Integer source;
    private Integer skip = 0;
    private Integer limit = 2;
}
