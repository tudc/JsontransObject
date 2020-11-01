package com.tudc.entity;

import com.tudc.util.jsonTransObject.model.enumpackage.SensitiveStrategy;
import com.tudc.util.jsonTransObject.annotation.Sensitive;
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
    @Sensitive(SensitiveStrategy.PHONE)
    private String phone;
}
