package de.chkal.togglz.core.util;

public class ClassUtils {

    public static <T> T createInstance(String classname, Class<T> interfaceClazz) {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null) {
            classLoader = interfaceClazz.getClassLoader();
        }

        try {

            Class<?> clazz = Class.forName(classname, true, classLoader);
            return (T) clazz.newInstance();

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unknown class: " + classname);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not create an instance of class: " + classname, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not create an instance of class: " + classname, e);
        }

    }

}
