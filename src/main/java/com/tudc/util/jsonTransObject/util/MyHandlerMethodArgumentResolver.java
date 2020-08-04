package com.tudc.util.jsonTransObject.util;

import com.tudc.util.jsonTransObject.annotation.Jsontransobject;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author
 * @create 2020-07-28 16:33
 * @contact
 **/
@Log4j2
public class MyHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private ParamHanlde paramHanlde=new ParamHanlde();

    public boolean supportsParameter(MethodParameter methodParameter) {
        //如果这个是被我的注解注释，就支持
        return methodParameter.hasMethodAnnotation(Jsontransobject.class);
    }

    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
        NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest httpServletRequest=nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Map<String,Object> requstBody=paramHanlde.obtainRequestBody(httpServletRequest);
        Map<String,Object> requestpathVariable=paramHanlde.obtainPathVariable(httpServletRequest);
        Map<String,Object> requestLine=paramHanlde.obtainRequestLine(httpServletRequest);
        Object object=paramHanlde.formObject(requstBody,requestpathVariable,requestLine,methodParameter,httpServletRequest);
        return object;
    }

}
