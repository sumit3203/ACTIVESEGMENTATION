package activeSegmentation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*
 * Library for annotations
 */
public class AnnotationManager {

   
	/**
	 * 
	 * @param clazz
	 * @return
	 */
    public static Annotation[] getClassAnnotations(Class<?> clazz) {
        return clazz.getAnnotations();
    }

    /**
     * 
     * @param clazz
     * @param methodName
     * @return
     * @throws NoSuchMethodException
     */
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

    /**
     * 
     * @param clazz
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     */
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

   /**
    * 
    * @param ann
    */
    public static void printAnnotations(Annotation[] ann) {
        if (ann == null) return;
        for (Annotation a : ann) {
            System.out.println(a.toString());
        }
    }
}