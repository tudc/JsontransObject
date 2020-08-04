package com.tudc.entity;

import com.tudc.entity.Enum.SexEnum;
import lombok.Data;

/**
 * @author
 * @create 2020-07-28 15:42
 * @contact
 **/
@Data
public class ApproverDto {
    private String name;
    private int age;
    private SexEnum sex;

    public void setSexEnum(int sex){
      this.sex=SexEnum.getSexEnum(sex);
    }
}
