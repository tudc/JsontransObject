package com.tudc.controller;

import com.tudc.entity.OrderDto;
import com.tudc.util.jsonTransObject.annotation.Jsontransobject;
import com.tudc.util.jsonTransObject.util.JsonTransObjectXmlUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

/**
 * @author
 * @create 2020-07-28 15:40
 * @contact
 **/
@RestController
@RequestMapping("/boostrap")
public class BootstrapController {

    @PostMapping("/create-order/{orderId}")
    @Jsontransobject("create-order-post")
    public Object createOrder(OrderDto dto) throws FileNotFoundException {
        System.out.println(dto);
        return null;
    }
}
