package com.tudc.util.jsonTransObject.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tudc.util.jsonTransObject.model.enumpackage.SensitiveStrategy;
import com.tudc.util.jsonTransObject.annotation.Sensitive;
import com.tudc.util.jsonTransObject.model.entity.JsonTransObject;
import com.tudc.util.jsonTransObject.model.entity.JsonTransObjectProperty;
import com.tudc.util.jsonTransObject.model.enumpackage.JsonTransObjectEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@Log4j2 public class JsonTransObjectUtil {

    /**
     * 复制对象
     * @param jsontransobjectValue 规则ID
     * @param t 数据源对象
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T parseObject(String jsontransobjectValue, T t) throws Exception {
        Class clazz = t.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Map<String, JsonTransObject> jsonTransObjectMap = JsonTransObjectXmlUtil.getConfig();
        if (jsonTransObjectMap.containsKey(jsontransobjectValue)) {
            Map<String, Method> map = obtainMethodFromClass(clazz);
            JsonTransObject jsonTransObjectConf = jsonTransObjectMap.get(jsontransobjectValue);
            List<JsonTransObjectProperty> propertyList = jsonTransObjectConf.getPropertyList();
            return copyChildObj(propertyList, fields, map, t);
        } else {
            log.warn("未找到当前jsonTransObject【{}】配置的信息信息", jsontransobjectValue);
            return null;
        }
    }

    /**
     * 复制指定类型的目标对象
     * @param propertyList 复制时，使用的规则
     * @param fields 指定类的定义属性集合
     * @param map 指定类的方法集合
     * @param t 数据源对象
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private static <T> T copyChildObj(List<JsonTransObjectProperty> propertyList, Field[] fields, Map<String, Method> map, T t)
        throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        T targetObj = (T) t.getClass().newInstance();
        Map<String, JsonTransObjectProperty> propertyMap = new HashMap<>();
        propertyList.forEach(item -> propertyMap.put(item.getValue(), item));
        for (Field field : fields) {
            copyField(propertyMap,field,map,t,targetObj);
        }
        return targetObj;
    }

    /**
     * 复制属性
     * @param propertyMap 校验规则中的属性集合
     * @param field 属性
     * @param map 当前对象的方法集合
     * @param t 数据源对象
     * @param targetObj 需要复制到的目标对象
     * @param <T>
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     */
    private static  <T> void copyField(Map<String, JsonTransObjectProperty> propertyMap,Field field, Map<String, Method> map,T t,T targetObj)
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (Optional.ofNullable(propertyMap.get(field.getName())).isPresent()) {
            JsonTransObjectProperty property = propertyMap.get(field.getName());
            Method getMethod = map.get(property.getGetterMethod());
            String setMethodName = new StringBuilder(getMethod.getName()).replace(0, 1, "s").toString();
            Method setMethod = map.get(setMethodName);
            Object o = getMethod.invoke(t);
            if (isJavaClass(o.getClass())) {
                //如果是集合类
                if (o instanceof Collection) {
                    copyCollectionField(field,o,property,setMethod,targetObj);
                } else {
                    //检查是否需要进行脱敏
                    Sensitive sensitive = field.getAnnotation(Sensitive.class);
                    if (sensitive != null && Optional.ofNullable(sensitive.value()).isPresent()&&o instanceof String) {
                        o=desensitization((String)o,sensitive.value());
                    }
                    setMethod.invoke(targetObj, o);
                }
            } else {
                //用户定义的类型
                Class childClass = o.getClass();
                Field[] childFields = childClass.getDeclaredFields();
                Map<String, Method> childMap = obtainMethodFromClass(childClass);
                List<JsonTransObjectProperty> childPropertyList = property.getChildJsonTransObjectProperty();
                //将子级对象的值赋给当前级别的对象
                setMethod.invoke(targetObj, copyChildObj(childPropertyList, childFields, childMap, o));
            }
        }
    }

    /**
     * 复制集合类型的对象
     * @param field 当前的属性
     * @param o 当前属性的值
     * @param property 当前属性对应的校验规则
     * @param setMethod 当前属性的set方法
     * @param targetObj 属性所属的对象
     * @param <T>
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    private static<T> void copyCollectionField(Field field,Object o,JsonTransObjectProperty property,Method setMethod,T targetObj)
        throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Type type = field.getGenericType();
        Class actualTypeArgument = (Class) ((ParameterizedTypeImpl) type).getActualTypeArguments()[0];
        Class collection = o.getClass();
        T e = (T) collection.newInstance();
        List<JsonTransObjectProperty> childPropertyList = property.getChildJsonTransObjectProperty();
        Field[] childFields = actualTypeArgument.getDeclaredFields();
        Map<String, Method> childMap = obtainMethodFromClass(actualTypeArgument);
        Method me = collection.getDeclaredMethod("add", Object.class);
        Iterator it = ((Collection) o).iterator();
        while (it.hasNext()) {
            me.invoke(e, copyChildObj(childPropertyList, childFields, childMap, it.next()));
        }
        setMethod.invoke(targetObj, e);
    }
    /**
     * 判断是否是java本身的类型
     *
     * @param clz
     * @return
     */
    private static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

    /**
     * 脱敏
     * @param original
     * @param strategy
     * @return
     */
    private static String desensitization(String original, SensitiveStrategy strategy) {
        return strategy.getDesensitizer().apply(original);
    }

    /**
     * 通过class 设置属性
     *
     * @param targetClass          目标类型
     * @param jsontransobjectValue 配置的jsontransobject的ID
     * @param jsonTransObjectMap   所有配置的jsontransobject
     * @param requstBody           来自请求体的参数
     * @param requestpathVariable  来自请求路径的参数
     * @param requestLine          来自请求行的参数
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T setPropertyWithClass(Class<T> targetClass, String jsontransobjectValue,
        Map<String, JsonTransObject> jsonTransObjectMap, Map<String, Object> requstBody, Map<String, Object> requestpathVariable,
        Map<String, Object> requestLine) throws Exception {
        if (jsonTransObjectMap.containsKey(jsontransobjectValue)) {
            JsonTransObject jsonTransObjectConf = jsonTransObjectMap.get(jsontransobjectValue);
            List<JsonTransObjectProperty> propertyList = jsonTransObjectConf.getPropertyList();
            T t = targetClass.newInstance();
            Class clazz = t.getClass();
            Map<String, Method> map = obtainMethodFromClass(clazz);
            for (JsonTransObjectProperty property : propertyList) {
                setProperty(requstBody, requestpathVariable, requestLine, map.get(property.getSetterMethod()), t, property);
            }
            return t;
        } else {
            log.warn("未找到当前jsonTransObject【{}】配置的信息信息", jsontransobjectValue);
        }
        return null;
    }

    /**
     * 设置属性
     *
     * @param requstBody          来着body体的参数
     * @param requestpathVariable 来着uri中的参数
     * @param requestLine         来之请求行的参数
     * @param parentMethod
     * @param parent              实体
     * @param property            属性配置信息
     * @param <T>
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static <T> void setProperty(Map<String, Object> requstBody, Map<String, Object> requestpathVariable,
        Map<String, Object> requestLine, Method parentMethod, T parent, JsonTransObjectProperty property) throws Exception {

        if (JsonTransObjectEnum.FROM_WHERE.PATH_VARIABLE.equals(property.getFromWhere())) {
            //校验正则
            regex(property.getValue(), String.valueOf(requestpathVariable.get(property.getValue())), property.getRegex());
            //检查当前参数是否可选
            if (requestpathVariable.get(property.getValue()) == null) {
                if (property.getIsRequired()) {
                    log.error("属性【{}】为必选参数", property.getValue());
                    throw new RuntimeException("属性【" + property.getValue() + "】为必选参数");
                }
                return;
            }
            setPropertyByType(parentMethod, requestpathVariable.get(property.getValue()), parent, property.getType());
        } else if (JsonTransObjectEnum.FROM_WHERE.REQUEST_LINE.equals(property.getFromWhere())) {
            //校验正则
            regex(property.getValue(), String.valueOf(requestLine.get(property.getValue())), property.getRegex());
            //检查当前参数是否可选
            if (requestLine.get(property.getValue()) == null) {
                if (property.getIsRequired()) {
                    log.error("属性【{}】为必选参数", property.getValue());
                    throw new RuntimeException("属性【" + property.getValue() + "】为必选参数");
                }
                return;
            }
            setPropertyByType(parentMethod, requestLine.get(property.getValue()), parent, property.getType());
        } else {
            //检查当前参数是否可选
            if (requstBody.get(property.getValue()) == null) {
                if (property.getIsRequired()) {
                    log.error("属性【{}】为必选参数", property.getValue());
                    throw new RuntimeException("属性【" + property.getValue() + "】为必选参数");
                }
                return;
            }
            //如果当前属性为自定义类，有下级属性
            if (property.getHasChild()) {

                Class<?>[] childs = parentMethod.getParameterTypes();
                Class child = childs[0];
                if (Collection.class.isAssignableFrom(child)) {
                    handleChild(parentMethod,property,requstBody,requestpathVariable,requestLine,parent);
                } else {
                    handleArray(parentMethod,property,requstBody,requestpathVariable,requestLine,parent,child);
                }
            } else {
                //校验正则
                regex(property.getValue(), String.valueOf(requstBody.get(property.getValue())), property.getRegex());
                //默认参数来自请求体
                setPropertyByType(parentMethod, requstBody.get(property.getValue()), parent, property.getType());
            }
        }
    }

    /**
     * 处理自定义类型属性
     * @param parentMethod
     * @param property
     * @param requstBody
     * @param requestpathVariable
     * @param requestLine
     * @param parent
     * @param <T>
     * @throws Exception
     */
    private static <T> void handleChild(Method parentMethod,JsonTransObjectProperty property,Map<String, Object> requstBody, Map<String, Object> requestpathVariable,
        Map<String, Object> requestLine,T parent) throws Exception {
        Type typeArgument = obtainActualTypeArgument(parentMethod);
        Class typeArgumentClass = (Class) typeArgument;
        Map<String, Method> map = obtainMethodFromClass(typeArgumentClass);
        List<JsonTransObjectProperty> childJsonTransObjectPropertyList = property.getChildJsonTransObjectProperty();
        JSONArray childRequestBody = obtainChildParam(requstBody, property);
        List<Map> mapChildRequestBody = childRequestBody.toJavaList(Map.class);
        List list = new ArrayList<>();
        for (Map mapRequestBody : mapChildRequestBody) {
            T instance = (T) typeArgumentClass.newInstance();
            for (JsonTransObjectProperty jsonTransObjectProperty : childJsonTransObjectPropertyList) {
                setProperty(mapRequestBody, requestpathVariable, requestLine,
                    map.get(jsonTransObjectProperty.getSetterMethod()), instance, jsonTransObjectProperty);
            }
            list.add(instance);
        }
        setPropertyByType(parentMethod, list, parent, property.getType());
    }

    /**
     * 处理数组类型的属性
     * @param parentMethod
     * @param property
     * @param requstBody
     * @param requestpathVariable
     * @param requestLine
     * @param parent
     * @param child 数组元素的类型
     * @param <T>
     * @throws Exception
     */
    private static <T> void handleArray(Method parentMethod,JsonTransObjectProperty property,Map<String, Object> requstBody, Map<String, Object> requestpathVariable,
        Map<String, Object> requestLine,T parent,Class child) throws Exception {
        JSONArray childRequestBody = obtainChildParam(requstBody, property);
        List<Map> mapChildRequestBody = childRequestBody.toJavaList(Map.class);
        if (mapChildRequestBody.size() == 0) {
            log.warn("无数据");
            return;
        }
        T instance = (T) child.newInstance();
        Map<String, Method> map = obtainMethodFromClass(child);
        List<JsonTransObjectProperty> childJsonTransObjectPropertyList = property.getChildJsonTransObjectProperty();
        for (JsonTransObjectProperty jsonTransObjectProperty : childJsonTransObjectPropertyList) {
            setProperty(mapChildRequestBody.get(0), requestpathVariable, requestLine,
                map.get(jsonTransObjectProperty.getSetterMethod()), instance, jsonTransObjectProperty);
        }
        //如果有子级配置，则解析子级
        setPropertyByType(parentMethod, instance, parent, property.getType());
    }

    /**
     * 根据实际的类型，进行值的设置
     *
     * @param method 方法
     * @param value  将要设置的值
     * @param t      目标对象
     * @param type   注解文件中的属性的类型
     * @param <T>
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    private static <T> void setPropertyByType(Method method, Object value, T t, String type)
        throws InvocationTargetException, IllegalAccessException, ParseException {
        switch (type) {
            case "string":
                method.invoke(t, value);
                break;
            case "int":
                method.invoke(t, value);
                break;
            case "date":
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                method.invoke(t, sf.parse((String) value));
                break;
            case "jsonObject":
                method.invoke(t, value);
                break;
            case "jsonArray":
                method.invoke(t, value);
                break;
            default:
                method.invoke(t, value);
        }

    }

    /**
     * 从class中获取方法的集合
     *
     * @param clazz
     * @return
     */
    private static Map<String, Method> obtainMethodFromClass(Class clazz) {
        Method[] methods = clazz.getMethods();
        Map<String, Method> map = new HashMap<>();
        for (Method method : methods) {
            map.put(method.getName(), method);
        }
        return map;
    }

    private static void regex(String colunum, String value, String regex) throws Exception {
        if (StringUtils.isEmpty(regex)) {
            return;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher mathcer = pattern.matcher(value);
        if (!mathcer.matches()) {
            log.error("正则校验失败，value【{}】，regex:【{}】", value, regex);
            throw new Exception("数据【" + colunum + "】格式错误失败");
        }

    }

    /**
     * 获取cookies
     *
     * @param httpServletRequest
     * @return
     */
    public Cookie[] obtainCookies(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null && cookies.length != 0) {
            log.info("当前cookie:" + cookies.toString());
            return cookies;
        }
        return null;
    }

    /**
     * 获取子级的入参
     *
     * @param requstBody 请求体中的参数
     * @param property   当前级别的属性配置
     * @return 参数Map
     */
    private static JSONArray obtainChildParam(Map<String, Object> requstBody, JsonTransObjectProperty property) {
        JSONArray object = new JSONArray();
        if (StringUtils.isEmpty(property.getFromWhere()) || JsonTransObjectEnum.FROM_WHERE.REQUEST_BODY.equals(property.getFromWhere())) {
            //默认参数来自请求体(应该是只能来自requestBody)
            Object value = requstBody.get(property.getValue());
            if (value instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) requstBody.get(property.getValue());
                object.add(jsonObject);
            } else if (value instanceof JSONArray) {
                object = (JSONArray) requstBody.get(property.getValue());
            } else {
                log.error("未知得入参数据类型");
            }
        } else {
            log.warn("请检查JsonTransObject的属性value【{}】的【fromWhere】配置", property.getValue());
        }
        return object;
    }

    /**
     * 获取该方法入参的泛型类型
     * 此方法默认为一个泛型参数，并且只有一个泛型
     *
     * @param method 方法
     * @return 泛型类型
     */
    private static Type obtainActualTypeArgument(Method method) {
        log.info("当前对象是集合类");
        Type[] types = method.getGenericParameterTypes();
        Type reType = null;
        for (Type type : types) {
            //只有带泛型的参数才是这种Type，所以得判断一下
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    reType = actualTypeArguments[0];
                }
            }
        }
        return reType;
    }

}
