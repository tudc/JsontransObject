package com.tudc.util.jsonTransObject.model.entity;

import lombok.Data;

import java.util.List;

/**
 * @author
 * @create 2020-07-29 14:13
 * @contact
 **/
@Data
public class JsonTransObject {
    private String id;
    private List<JsonTransObjectProperty> propertyList;
}
