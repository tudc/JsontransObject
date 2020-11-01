package com.tudc.util.jsonTransObject.annotation;

import com.tudc.util.jsonTransObject.model.enumpackage.SensitiveStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 脱敏注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Sensitive {
    SensitiveStrategy value();
}
