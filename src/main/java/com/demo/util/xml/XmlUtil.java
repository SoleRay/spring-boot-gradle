package com.demo.util.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.InputStream;

public class XmlUtil {

    public static XStream getInstance(){
        XStream xStream = new XStream(new DomDriver("UTF-8",new NoNameCoder())) {
            /**
             * 忽略xml中多余字段
             */
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @SuppressWarnings("rawtypes")
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        if (definedIn == Object.class) {
                            return false;
                        }
                        return super.shouldSerializeMember(definedIn, fieldName);
                    }
                };
            }
        };
        XStream.setupDefaultSecurity(xStream);
        xStream.setClassLoader(XmlUtil.class.getClassLoader());
        xStream.addPermission(AnyTypePermission.ANY);
        return xStream;
    }

    public static <T> String toXml(T t,Class<T> clazz){
        XStream stream = getInstance();
        stream.processAnnotations(clazz);
        return stream.toXML(t);
    }

    public static String toXml(InputStream inputStream){
        XStream stream = getInstance();
        return stream.toXML(inputStream);
    }

    public static <T> T fromXml(String xml, Class<T> clazz) {
        XStream stream = getInstance();
        stream.processAnnotations(clazz);
        Object object = stream.fromXML(xml);
        T cast = clazz.cast(object);
        return cast;
    }
}
