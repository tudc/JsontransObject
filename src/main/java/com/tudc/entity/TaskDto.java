package com.tudc.entity;

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
    private String name;
    private Integer age;
}
