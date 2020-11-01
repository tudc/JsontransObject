package com.tudc.util.jsonTransObject.annotation;

import com.tudc.util.jsonTransObject.model.enumpackage.SensitiveStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author
 * @create 2020-07-28 16:06
 * @contact
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Sensitive {
    SensitiveStrategy value();
}
