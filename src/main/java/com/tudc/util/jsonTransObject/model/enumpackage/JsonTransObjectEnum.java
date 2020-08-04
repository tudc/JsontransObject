package com.tudc.util.jsonTransObject.model.enumpackage;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @create 2020-07-29 17:32
 * @contact
 **/
public enum JsonTransObjectEnum {
;


    /**
     * 请求参数来自哪里
     */
    @Getter
    public enum FROM_WHERE{
        PATH_VARIABLE("pathvariable","URI路径"),
        REQUEST_BODY("requestBody","请求体"),
        REQUEST_LINE("requestLine","请求行");

        private String Code;
        private String Desc;
        private static Map<String,FROM_WHERE> fromWhereMap=new HashMap<>();

        FROM_WHERE(String code, String desc) {
            Code = code;
            Desc = desc;
        }
        static {
            for (FROM_WHERE fromWhere:FROM_WHERE.values()) {
                fromWhereMap.put(fromWhere.getCode(),fromWhere);
            }
        }

        public static FROM_WHERE getFromWhere(String code){
            return fromWhereMap.get(code);
        }

    }

}
