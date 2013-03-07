package com.containerstore.lost.dirty;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;

/**
 * This utility class is used to activate/deactivate the null state any class with a DirtyObject member
 * field.
 **/
public class DirtyObjects {
    private static final Logger LOG = Logger.getLogger(DirtyObjects.class);


    private DirtyObjects() {
        throw new UnsupportedOperationException();
    }

    public static void activateObject(Object theObject) {
        setNullStatus(theObject, true);
    }

    public static void deactivateObject(Object theObject) {
        setNullStatus(theObject, false);
    }

    public static void assignDefaultsToHierarchy(Object theObject) {
        // Crawl up the object hierarchy so we activate all superclass DirtyObject members as well.
        Class clazz = theObject.getClass();
        while(true) {
            DirtyObject dirtyObject = lookup(theObject, clazz);
            if (dirtyObject != null) {
                dirtyObject.assignDefaults();
            }

            clazz = clazz.getSuperclass();
            if (clazz == null || clazz.equals(Object.class)) {
                break;
            }
        }
    }

    private static void setNullStatus(Object theObject, boolean status) {
        // Crawl up the object hierarchy so we activate all superclass DirtyObject members as well.
        Class clazz = theObject.getClass();
        while(true) {
            DirtyObject dirtyObject = lookup(theObject, clazz);
            if (dirtyObject != null) {
                dirtyObject.setNullMode(status);
            }

            clazz = clazz.getSuperclass();
            if (clazz == null || clazz.equals(Object.class)) {
                break;
            }
        }
    }

    private static DirtyObject lookup(Object theObject, Class asClass) {
        Field[] fields = asClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(DirtyObject.class)) {
                try {
                    field.setAccessible(true);
                    return (DirtyObject)field.get(theObject);
                } catch (IllegalAccessException e) {
                    LOG.warn("Exception getting DirtyObject object");
                    return null;
                }
            }
        }
        return null;
    }
}
