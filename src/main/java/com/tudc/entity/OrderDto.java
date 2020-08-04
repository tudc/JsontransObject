package com.tudc.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @create 2020-07-28 15:42
 * @contact
 **/
@Data
public class OrderDto {
    private String orderId;
    private TaskDto taskDto;
    private String orderName;
    private Date time;
    private String phone;
}
