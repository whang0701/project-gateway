package com.wanghang.projectgateway.controller;


import com.wanghang.projectgateway.model.to.receiver.ReceiverAddTo;
import com.wanghang.projectgateway.model.to.receiver.ReceiverUpdateTo;
import com.wanghang.projectgateway.model.to.user.UserInfoTo;
import com.wanghang.projectgateway.model.vo.receiver.ReceiverVo;
import com.wanghang.projectgateway.service.ReceiverService;
import com.wanghang.projectsdk.base.exception.ServiceException;
import com.wanghang.projectsdk.base.model.CodeType;
import com.wanghang.projectsdk.base.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author wanghang
 * 2020/5/15
 **/
@RestController
@RequestMapping("api/receiver/")
public class ReceiverController {
    @Autowired
    private ReceiverService receiverService;

    @RequestMapping(value = "/getList", method = RequestMethod.POST)
    public Response<List<ReceiverVo>> getList(@RequestBody UserInfoTo to) {
        try {
            return new Response<>(receiverService.getList(to.getNo()));
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Response add(@RequestBody ReceiverAddTo to) {
        try {
            receiverService.add(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Response delete(@RequestBody ReceiverUpdateTo to) {
        try {
            receiverService.delete(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response update(@RequestBody ReceiverUpdateTo to) {
        try {
            receiverService.update(to);
            return new Response(CodeType.OK);
        } catch (ServiceException e) {
            return new Response<>(e);
        } catch (Exception e) {
            return new Response<>(CodeType.SYSTEM_ERROR);
        }
    }

}
