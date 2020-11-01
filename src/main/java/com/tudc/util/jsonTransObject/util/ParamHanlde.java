package com.tudc.util.jsonTransObject.util;

import com.alibaba.fastjson.JSON;
import com.tudc.util.jsonTransObject.annotation.Jsontransobject;
import com.tudc.util.jsonTransObject.model.entity.JsonTransObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @create 2020-08-04 15:03
 * @contact
 **/
@Log4j2
public class ParamHanlde {

    /**
     * 将请求中的参数设置到 目标对象中
     * @param requstBody 请求体中的参数
     * @param requestpathVariable 请求行中的参数
     * @param requestLine uri中的参数
     * @param methodParameter
     * @param <T>
     * @return 目标对象实体
     */
    public<T> Object formObject(Map<String, Object> requstBody,Map<String,Object> requestpathVariable,Map<String,Object> requestLine ,
        MethodParameter methodParameter, HttpServletRequest request) throws Exception {
        Jsontransobject jsontransobject=methodParameter.getMethodAnnotation(Jsontransobject.class);
        if(jsontransobject==null|| StringUtils.isEmpty(jsontransobject.value())){
            log.info("方法【{}】的配置未配置value"+request.getRequestURI());
            return null;
        }
        Type targetType=methodParameter.getNestedGenericParameterType();
        Class<T> targetClass = targetType instanceof Class ? (Class)targetType : null;
        if(targetClass==null){
            log.error("未找到目标类:[{}]",targetType);
        }
        Map<String, JsonTransObject> jsonTransObjectMap=JsonTransObjectXmlUtil.getConfig();
        String jsontransobjectValue=jsontransobject.value();
        return JsonTransObjectUtil.setPropertyWithClass(targetClass,jsontransobjectValue,jsonTransObjectMap,requstBody,requestpathVariable,requestLine);
    }








    /**
     * 获取请求体中的参数
     * @param httpServletRequest
     * @return
     */
    public Map<String,Object> obtainRequestBody(HttpServletRequest httpServletRequest){
        Map<String,Object> requstBody = new HashMap<String, Object>();
        BufferedReader br;
        String line,wholeLine="";
        try {
            br=httpServletRequest.getReader();
            while((line = br .readLine())!=null){
                wholeLine+=line;
            }
            if(wholeLine.length()!=0){
                requstBody= JSON.parseObject(wholeLine,Map.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("解析请求体参数失败");
        }
        return requstBody;
    }

    /**
     * 获取请求行后面的参数
     * @param httpServletRequest
     * @return
     */
    public Map<String,Object> obtainRequestLine(HttpServletRequest httpServletRequest){
        Map<String,Object> requestLine=new HashMap<String, Object>();
        Map<String, String[]> params = httpServletRequest.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            requestLine.put(param.getKey(),param.getValue());
        }
        return requestLine;
    }

    /**
     * 获取requestLine中的入参
     * @param httpServletRequest
     * @return
     */
    public Map<String,Object> obtainPathVariable(HttpServletRequest httpServletRequest){
        Map pathVariables = (Map)httpServletRequest.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return pathVariables;
    }




}
