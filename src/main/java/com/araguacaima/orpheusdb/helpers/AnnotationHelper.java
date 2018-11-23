package com.araguacaima.orpheusdb.helpers;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class AnnotationHelper {

    public static <T extends Annotation> T getAnnotation(Class type, Class<T> annotationClass) {
        String annotationName = annotationClass.getName();
        while (type != null) {
            for (Annotation annotation : type.getDeclaredAnnotations()) {
                String name = annotation.annotationType().toString().replaceAll("interface ", StringUtils.EMPTY);
                if (name.equals(annotationName)) {
                    return (T) annotation;
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }
}