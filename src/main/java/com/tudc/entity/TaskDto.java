package com.tudc.entity;

import com.tudc.util.jsonTransObject.model.enumpackage.SensitiveStrategy;
import com.tudc.util.jsonTransObject.annotation.Sensitive;
import lombok.Data;

import java.util.List;

/**
 * @author
 * @create 2020-07-28 15:42
 * @contact
 **/
@Data
public class TaskDto {
    private List<ApproverDto> approverDtoList;
    @Sensitive(SensitiveStrategy.USERNAME)
    private String name;
    private Integer age;
}
