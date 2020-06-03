package com.wanghang.projectgateway.controller;

import com.wanghang.projectgateway.model.vo.SourceVo;
import com.wanghang.projectgateway.service.SourceService;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wanghang
 * 2020/4/28
 **/
@RestController
@RequestMapping("api/source")
public class SourceController {
    @Autowired
    private SourceService sourceService;

    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public Response<List<SourceVo>> getSources() {
        try {
            return new Response<>(sourceService.getSources());
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }
}
