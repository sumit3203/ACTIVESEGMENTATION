package activeSegmentation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*
 * Library for annotations
 */
public class AnnotationManager {

   
    public static Annotation[] getClassAnnotations(Class<?> clazz) {
        return clazz.getAnnotations();
    }

    public static Annotation[] getMethodAnnotations(Class<?> clazz, String methodName) throws NoSuchMethodException {
        Annotation[] annotations = null;
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            if (method != null) {
                annotations = method.getAnnotations();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return annotations;
    }

    public static Annotation[] getFieldAnnotations(Class<?> clazz, String fieldName) throws NoSuchFieldException{
        Annotation[] annotations = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null) {
                annotations = field.getAnnotations();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return annotations;
    }

   
    public static void printAnnotations(Annotation[] ann) {
        if (ann == null) return;
        for (Annotation a : ann) {
            System.out.println(a.toString());
        }
    }
}