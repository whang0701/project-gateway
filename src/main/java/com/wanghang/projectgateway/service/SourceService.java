package com.wanghang.projectgateway.service;

import com.wanghang.projectgateway.model.vo.SourceVo;
import com.wanghang.projectsdk.base.enumeration.ProductSourceType;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wanghang
 * 2020/4/28
 **/
@Service
public class SourceService {

    public List<SourceVo> getSources() {
        return Arrays.stream(ProductSourceType.values()).map(type -> {
            SourceVo sourceVo = new SourceVo();
            sourceVo.setName(type.getValue());
            sourceVo.setOption(type.getKey());
            return sourceVo;
        }).collect(Collectors.toList());
    }
}
