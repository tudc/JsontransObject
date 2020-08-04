package com.tudc.entity.Enum;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @create 2020-07-31 15:33
 * @contact
 **/
@Getter
public enum  SexEnum {
    MAN(1,"男性"),
    WOMAN(2,"女性");
    private Integer code;
    private String desc;

    private static Map<Integer,SexEnum> sexEnumMap=new HashMap<>();
    SexEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    static {
        for (SexEnum sex: SexEnum.values()) {
            sexEnumMap.put(sex.code,sex);
        }
    }

    public static SexEnum getSexEnum(int code){
        return sexEnumMap.get(code);
    }
}
