package com.tudc.controller;

import com.tudc.entity.OrderDto;
import com.tudc.util.jsonTransObject.annotation.Jsontransobject;
import com.tudc.util.jsonTransObject.util.JsonTransObjectUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @create 2020-07-28 15:40
 * @contact
 **/
@RestController
@RequestMapping("/boostrap")
public class BootstrapController {

    /**
     * 第一种正常发起请求
     * @param dto
     * @return
     * @throws Exception
     */
    @PostMapping("/create-order-normal/{orderId}")
    @Jsontransobject("create-order-post")
    public Object createOrderNormal(OrderDto dto) throws Exception {
        return dto;
    }

    /**
     * 第二种正常发起请求
     * @param dto
     * @return
     * @throws Exception
     */
    @PostMapping("/create-order-normal-second/{orderId}")
    @Jsontransobject("create-order-post-second")
    public Object createOrderNormalSecond(OrderDto dto) throws Exception {
        return dto;
    }

    /**
     * 被处理过后的返回
     * @param dto
     * @return
     * @throws Exception
     */
    @PostMapping("/return-order-handle/{orderId}")
    @Jsontransobject("create-order-post")
    public Object returnOrderHandled(OrderDto dto) throws Exception {
        return JsonTransObjectUtil.parseObject("create-order-post-response",dto);
    }
}
