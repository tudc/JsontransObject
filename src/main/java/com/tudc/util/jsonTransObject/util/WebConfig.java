package com.tudc.util.jsonTransObject.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @author
 * @create 2020-07-29 9:50
 * @contact
 **/
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Override protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new MyHandlerMethodArgumentResolver());
        super.addArgumentResolvers(argumentResolvers);
    }
}
