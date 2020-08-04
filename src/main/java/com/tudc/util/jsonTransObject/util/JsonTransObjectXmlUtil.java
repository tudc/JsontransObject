package com.tudc.util.jsonTransObject.util;

import com.tudc.util.jsonTransObject.model.entity.JsonTransObject;
import com.tudc.util.jsonTransObject.model.entity.JsonTransObjectProperty;
import com.tudc.util.jsonTransObject.model.enumpackage.JsonTransObjectEnum;
import lombok.extern.java.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

/**
 * @author
 * @create 2020-07-29 14:19
 * @contact
 **/
@Log
public class JsonTransObjectXmlUtil {
    private static Long lastModifyTime;
    private static String defaultPath="jsontransobject";
    private static List<File> needLodFileList;
    private static Map<String,JsonTransObject> jsonTransObjectList=new HashMap<>();
    static {
        try {
            loadConfig();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.info("加载配置文件失败");
        }
    }

    public static Map<String,JsonTransObject> getConfig(){
        return jsonTransObjectList;
    }

    /**
     * 加载配置
     * @throws FileNotFoundException
     */

    public static void loadConfig() throws FileNotFoundException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path=url.getPath();
        path+=defaultPath;
        loopDirs(path);
        loadFileList();
    }

    /**
     * 遍历指定路径，加载所有的文件
     * @param path 配置文件路径
     * @throws FileNotFoundException
     */
    private static void loopDirs(String path) throws FileNotFoundException {
        File file = ResourceUtils.getFile(path);
        if(file.exists()){
            File[] files=file.listFiles();
            for(File fileIten: files){
                if (fileIten.isDirectory()) {
                    //如果是路径，则往下遍历
                    loopDirs(fileIten.getAbsolutePath());
                }else{
                    //如果是文件，则加载到文件集合里面去
                    if(needLodFileList==null){
                        needLodFileList=new ArrayList<>();
                    }
                    needLodFileList.add(fileIten);
                }
            }
        }
    }

    /**
     * 加载所有的文件
     */
    private static void loadFileList() {
        if (needLodFileList == null || needLodFileList.size() == 0) {
            log.info("无需要加载的 jsonTransObject 配置文件");
            return;
        }
        needLodFileList.forEach(file -> {
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(file);
                Element rootNode = document.getRootElement();
                Iterator<Element> beanList = rootNode.elementIterator("bean");
                while (beanList.hasNext()) {
                    Element bean = beanList.next();
                    jsonTransObjectList.put(bean.attributeValue("id"),loadBean(bean));
                }
                log.info("加载配置文件完毕");
            } catch (DocumentException e) {
                e.printStackTrace();
                log.info("读取jsonTransObject的配置文件出错：" + file.getAbsolutePath());
            }
        });
    }

    /**
     * 加载单个配置信息
     * @param bean 配置信息体
     * @return
     */
    private static JsonTransObject loadBean(Element bean){
            JsonTransObject jsonTransObject = new JsonTransObject();
            jsonTransObject.setId(bean.attributeValue("id"));

            Iterator<Element> propertyList = bean.elementIterator("property");
            List<JsonTransObjectProperty> jsonTransObjectPropertyList = new ArrayList<>();
            while (propertyList.hasNext()) {
                Element property = propertyList.next();
                jsonTransObjectPropertyList.add(loadProperty(property));
        }
            jsonTransObject.setPropertyList(jsonTransObjectPropertyList);
            return jsonTransObject;
    }

    /**
     * 加载单个属性配置
     * @param property 属性
     * @return
     */
    private static JsonTransObjectProperty loadProperty(Element property){
        JsonTransObjectProperty jsonTransObjectProperty = new JsonTransObjectProperty();
        jsonTransObjectProperty.setValue(property.attributeValue("value"));
        jsonTransObjectProperty.setSetterMethod(property.attributeValue("setterMethod"));
        jsonTransObjectProperty.setGetterMethod(property.attributeValue("getterMethod"));
        jsonTransObjectProperty.setFromWhere(JsonTransObjectEnum.FROM_WHERE.getFromWhere(property.attributeValue("fromWhere")));
        jsonTransObjectProperty.setType(property.attributeValue("type"));
        jsonTransObjectProperty.setRegex(property.attributeValue("regex"));
        jsonTransObjectProperty.setIsRequired(Boolean.valueOf(property.attributeValue("isRequire")));
        jsonTransObjectProperty.setHasChild(Boolean.valueOf(property.attributeValue("hasChild")));
        if(Boolean.valueOf(property.attributeValue("hasChild"))){
            Iterator<Element> childPropertyList = property.elementIterator("property");
            List<JsonTransObjectProperty> jsonTransObjectPropertyList = new ArrayList<>();
            while (childPropertyList.hasNext()){
                jsonTransObjectPropertyList.add(loadProperty(childPropertyList.next()));
            }
            jsonTransObjectProperty.setChildJsonTransObjectProperty(jsonTransObjectPropertyList);
        }
        return jsonTransObjectProperty;
    }
}
