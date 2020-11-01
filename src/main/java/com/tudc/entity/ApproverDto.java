package com.tudc.entity;

import com.tudc.entity.Enum.SexEnum;
import com.tudc.util.jsonTransObject.model.enumpackage.SensitiveStrategy;
import com.tudc.util.jsonTransObject.annotation.Sensitive;
import lombok.Data;

/**
 * @author
 * @create 2020-07-28 15:42
 * @contact
 **/
@Data
public class ApproverDto {
    @Sensitive(SensitiveStrategy.USERNAME)
    private String name;
    private Integer age;
    private SexEnum sex = SexEnum.MAN;

    public void setSexNum(int sex){
      this.sex=SexEnum.getSexEnum(sex);
    }
    public Integer getSexNum(){
        return sex.getCode();
    }
}
