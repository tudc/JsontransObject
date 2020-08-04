package com.tudc.util.jsonTransObject.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tudc.util.jsonTransObject.annotation.Jsontransobject;
import com.tudc.util.jsonTransObject.model.entity.JsonTransObject;
import com.tudc.util.jsonTransObject.model.entity.JsonTransObjectProperty;
import com.tudc.util.jsonTransObject.model.enumpackage.JsonTransObjectEnum;
import lombok.extern.log4j.Log4j2;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author
 * @create 2020-07-28 16:33
 * @contact
 **/
@Log4j2
public class MyHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public boolean supportsParameter(MethodParameter methodParameter) {
        //如果这个是被我的注解注释，就支持
        return methodParameter.hasMethodAnnotation(Jsontransobject.class);
    }

    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
        NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest httpServletRequest=nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        Map<String,Object> requstBody=obtainRequestBody(httpServletRequest);
        Map<String,Object> requestpathVariable=obtainPathVariable(httpServletRequest);
        Map<String,Object> requestLine=obtainRequestLine(httpServletRequest);
        Object object=formObject(requstBody,requestpathVariable,requestLine,methodParameter,httpServletRequest);
        return object;
    }

    /**
     * 将请求中的参数设置到 目标对象中
     * @param requstBody 请求体中的参数
     * @param requestpathVariable 请求行中的参数
     * @param requestLine uri中的参数
     * @param methodParameter
     * @param <T>
     * @return 目标对象实体
     */
    private<T> Object formObject(Map<String, Object> requstBody,Map<String,Object> requestpathVariable,Map<String,Object> requestLine ,MethodParameter methodParameter,HttpServletRequest request)
        throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException, ParseException {
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
        Map<String,JsonTransObject> jsonTransObjectMap=JsonTransObjectXmlUtil.getConfig();
        T t=targetClass.newInstance();
        Class clazz=t.getClass();
        Map<String,Method> map=obtainMethodFromClass(clazz);
        if(jsonTransObjectMap.containsKey(jsontransobject.value())){
            JsonTransObject jsonTransObjectConf=jsonTransObjectMap.get(jsontransobject.value());
            List<JsonTransObjectProperty> propertyList=jsonTransObjectConf.getPropertyList();
            for(JsonTransObjectProperty property:propertyList){
                setProperty(requstBody,requestpathVariable,requestLine,map.get(property.getSetterMethod()),t,property);
            }
        }else{
            log.warn("未找到当前jsonTransObject【{}】配置的信息信息",jsontransobject.value());
        }

        return t;
    }

    /**
     * 设置属性
     * @param requstBody 来着body体的参数
     * @param requestpathVariable 来着uri中的参数
     * @param requestLine 来之请求行的参数
     * @param parentMethod
     * @param parent 实体
     * @param property 属性配置信息
     * @param <T>
     * @param <E>
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private <T,E> void setProperty(Map<String, Object> requstBody,Map<String,Object> requestpathVariable,Map<String,Object> requestLine ,Method parentMethod,T parent,JsonTransObjectProperty property)
        throws InvocationTargetException, IllegalAccessException, InstantiationException, ParseException {

        if(JsonTransObjectEnum.FROM_WHERE.PATH_VARIABLE.equals(property.getFromWhere())){
            //检查当前参数是否可选
            if(requestpathVariable.get(property.getValue())==null){
                if(property.getIsRequired()){
                    log.error("属性【{}】为必选参数",property.getValue());
                    throw new RuntimeException("属性【"+property.getValue()+"】为必选参数");
                }
                return;
            }
            //parentMethod.invoke(parent,requestpathVariable.get(property.getValue()));
            setPropertyByType(parentMethod,requestpathVariable.get(property.getValue()),parent,property.getType());
        }else if(JsonTransObjectEnum.FROM_WHERE.REQUEST_LINE.equals(property.getFromWhere())){
            //检查当前参数是否可选
            if(requestLine.get(property.getValue())==null){
                if(property.getIsRequired()){
                    log.error("属性【{}】为必选参数",property.getValue());
                    throw new RuntimeException("属性【"+property.getValue()+"】为必选参数");
                }
                return;
            }
            //parentMethod.invoke(parent,);
            setPropertyByType(parentMethod,requestLine.get(property.getValue()),parent,property.getType());
        }else{
            //检查当前参数是否可选
            if(requstBody.get(property.getValue())==null){
                if(property.getIsRequired()){
                    log.error("属性【{}】为必选参数",property.getValue());
                    throw new RuntimeException("属性【"+property.getValue()+"】为必选参数");
                }
                return;
            }
            //如果当前属性为对象属性，有下级属性
            if(property.getHasChild()){

                Class<?>[] childs=parentMethod.getParameterTypes();
                Class child=childs[0];
                if(Collection.class.isAssignableFrom(child)){
                    Type typeArgument= obtainActualTypeArgument(parentMethod);
                    Class typeArgumentClass=(Class) typeArgument;
                    Map<String,Method> map=obtainMethodFromClass(typeArgumentClass);
                    List<JsonTransObjectProperty> childJsonTransObjectPropertyList=property.getChildJsonTransObjectProperty();
                    JSONArray childRequestBody=obtainChildParam(requstBody,property);
                    List<Map> mapChildRequestBody=childRequestBody.toJavaList(Map.class);
                    List list=new ArrayList<>();
                    for(Map mapRequestBody:mapChildRequestBody){
                        E instance= (E) typeArgumentClass.newInstance();
                        for(JsonTransObjectProperty jsonTransObjectProperty:childJsonTransObjectPropertyList){
                            setProperty(mapRequestBody,requestpathVariable,requestLine,map.get(jsonTransObjectProperty.getSetterMethod()),instance,jsonTransObjectProperty);
                        }
                        list.add(instance);
                    }
                    setPropertyByType(parentMethod,list,parent,property.getType());
                }else{
                    JSONArray childRequestBody=obtainChildParam(requstBody,property);
                    List<Map> mapChildRequestBody= childRequestBody.toJavaList(Map.class);
                    if(mapChildRequestBody.size()==0){
                        log.warn("无数据");
                        return;
                    }
                    E instance= (E) child.newInstance();

                    Map<String,Method> map=obtainMethodFromClass(child);
                    List<JsonTransObjectProperty> childJsonTransObjectPropertyList=property.getChildJsonTransObjectProperty();
                    for(JsonTransObjectProperty jsonTransObjectProperty:childJsonTransObjectPropertyList){
                        setProperty(mapChildRequestBody.get(0),requestpathVariable,requestLine,map.get(jsonTransObjectProperty.getSetterMethod()),instance,jsonTransObjectProperty);
                    }
                    //如果有子级配置，则解析子级
                    //parentMethod.invoke(parent,instance);
                    setPropertyByType(parentMethod,instance,parent,property.getType());
                }

            }else {
                //默认参数来自请求体
                //parentMethod.invoke(parent, requstBody.get(property.getValue()));
                setPropertyByType(parentMethod,requstBody.get(property.getValue()),parent,property.getType());
            }
        }
    }

    /**
     * 获取该方法入参的泛型类型
     * 此方法默认为一个泛型参数，并且只有一个泛型
     * @param method 方法
     * @return 泛型类型
     */
    private Type obtainActualTypeArgument(Method method){
        log.info("当前对象是集合类");
        Type[] types=method.getGenericParameterTypes();
        Type reType=null;
        for(Type type:types){
            //只有带泛型的参数才是这种Type，所以得判断一下
            if(type instanceof ParameterizedType){
                ParameterizedType parameterizedType= (ParameterizedType) type;
                Type[] actualTypeArguments=parameterizedType.getActualTypeArguments();
                if(actualTypeArguments.length>0){
                    reType=actualTypeArguments[0];
                }
            }
        }
        return reType;
    }

    private<T> void setPropertyByType(Method method,Object value,T t,String type)
        throws InvocationTargetException, IllegalAccessException, ParseException {
        switch (type){
            case "string":
                method.invoke(t,value);
                break;
            case "int":
                method.invoke(t,value);
                break;
            case "date":
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                method.invoke(t,sf.parse((String) value));
                break;
            case "jsonObject":
                method.invoke(t,value);
                break;
            case "jsonArray":
                method.invoke(t,value);
                break;
            default:
                method.invoke(t,value);
        }

    }

    /**
     * 获取子级的入参
     * @param requstBody 请求体中的参数
     * @param property 当前级别的属性配置
     * @return 参数Map
     */
    private JSONArray obtainChildParam(Map<String, Object> requstBody,JsonTransObjectProperty property){
        JSONArray object=new JSONArray();
        if(StringUtils.isEmpty(property.getFromWhere())||JsonTransObjectEnum.FROM_WHERE.REQUEST_BODY.equals(property.getFromWhere())){
            //默认参数来自请求体(应该是只能来自requestBody)
            Object value=requstBody.get(property.getValue());
            if(value instanceof JSONObject){
                JSONObject jsonObject= (JSONObject) requstBody.get(property.getValue());
                object.add(jsonObject);
            }else if(value instanceof JSONArray){
                object= (JSONArray) requstBody.get(property.getValue());
            }else{
                log.error("未知得入参数据类型");
            }
        }else{
            log.warn("请检查JsonTransObject的属性value【{}】的【fromWhere】配置",property.getValue());
        }
        return object;
    }

    /**
     * 从class中获取方法的集合
     * @param clazz
     * @return
     */
    private Map<String,Method> obtainMethodFromClass(Class clazz){
        Method[] methods=clazz.getMethods();
        Map<String,Method> map=new HashMap<>();
        for (Method method:methods){
            map.put(method.getName(),method);
        }
        return map;
    }

    /**
     * 获取请求体中的参数
     * @param httpServletRequest
     * @return
     */
    private Map<String,Object> obtainRequestBody(HttpServletRequest httpServletRequest){
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
    private Map<String,Object> obtainRequestLine(HttpServletRequest httpServletRequest){
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
    private Map<String,Object> obtainPathVariable(HttpServletRequest httpServletRequest){
        Map pathVariables = (Map)httpServletRequest.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return pathVariables;
    }

    /**
     * 获取cookies
     * @param httpServletRequest
     * @return
     */
    private Cookie[] obtainCookies(HttpServletRequest httpServletRequest){
        Cookie[] cookies=httpServletRequest.getCookies();
        if(cookies!=null&&cookies.length!=0){
            log.info("当前cookie:"+cookies.toString());
            return cookies;
        }
        return null;
    }
}
