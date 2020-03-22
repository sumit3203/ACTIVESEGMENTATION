package test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AnnotationIntrospector {

    public AnnotationIntrospector() {
        super();
    }

    public Annotation[] findClassAnnotation(Class<?> clazz) {
        return clazz.getAnnotations();
    }

    public Annotation[] findMethodAnnotation(Class<?> clazz, String methodName) {

        Annotation[] annotations = null;
        try {
            Class<?>[] params = null;
            Method method = clazz.getDeclaredMethod(methodName, params);
            if (method != null) {
                annotations = method.getAnnotations();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return annotations;
    }

    public Annotation[] findFieldAnnotation(Class<?> clazz, String fieldName) {
        Annotation[] annotations = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null) {
            	//System.out.println("nn");
                annotations = field.getAnnotations();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return annotations;
    }

    /**
     * @param args
     */
    /*
    public static void main(String[] args) {
        AnnotationIntrospector ai = new AnnotationIntrospector();
        Annotation[] annotations;
        Class<User> userClass = User.class;
        String methodDoStuff = "doStuff";
        String fieldId = "id";
        String fieldAddress = "address";

        // Find class annotations
        annotations = ai.findClassAnnotation(be.fery.annotation.User.class);
        System.out.println("Annotation on class '" + userClass.getName()
                + "' are:");
        showAnnotations(annotations);

        // Find method annotations
        annotations = ai.findMethodAnnotation(User.class, methodDoStuff);
        System.out.println("Annotation on method '" + methodDoStuff + "' are:");
        showAnnotations(annotations);

        // Find field annotations
        annotations = ai.findFieldAnnotation(User.class, fieldId);
        System.out.println("Annotation on field '" + fieldId + "' are:");
        showAnnotations(annotations);

        annotations = ai.findFieldAnnotation(User.class, fieldAddress);
        System.out.println("Annotation on field '" + fieldAddress + "' are:");
        showAnnotations(annotations);

    }
*/
    public static void showAnnotations(Annotation[] ann) {
        if (ann == null)
            return;
        for (Annotation a : ann) {
            System.out.println(a.toString());
        }
    }
}