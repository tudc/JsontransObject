package com.tudc.util.jsonTransObject.model.entity;

import com.tudc.util.jsonTransObject.model.enumpackage.JsonTransObjectEnum;
import lombok.Data;

import java.util.List;

/**
 * @author
 * @create 2020-07-29 14:14
 * @contact
 **/
@Data
public class JsonTransObjectProperty {

    private String value;
    private String GetterMethod;
    private String SetterMethod;
    private String type;
    private JsonTransObjectEnum.FROM_WHERE fromWhere;
    private Boolean hasChild;
    private Boolean isRequired;
    private List<JsonTransObjectProperty> childJsonTransObjectProperty;
}
