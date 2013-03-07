package com.containerstore.lost.dirty;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility class for setting the default values of all non-static and non-final member variables.
 *
 * Great care should be taken using these classes, as they will overwrite any current values for
 * all non-static/non-final fields.
 */
public final class ObjectDefaulter {
    public final static Logger LOG = Logger.getLogger(ObjectDefaulter.class);

    private ObjectDefaulter() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets default values for all non-static/non-final/non-inherited member fields of the given object using the
     * ObjectDefaults base class.
     *
     * @param object - Object to set
     */
    public static void setObjectDefaults(Object object) {
        setObjectDefaults(object, new ObjectDefaults());
    }

    /**
     * Sets default values for all non-static/non-final/non-inherited member fields of the given object using the
     * base ObjectDefaults object and as the referenced class. This can be used to assign default values
     * to a superclass
     *
     * @param object
     * @param asClass
     */
    public static void setObjectDefaults(Object object, Class asClass) {
        setObjectDefaults(object, new ObjectDefaults(), asClass);
    }

    /**
     * Sets default values for all non-static/non-final/non-inherited member fields of the given object using the
     * given ObjectDefaults-derived object.
     *
     * @param object - Object to set
     * @param defaults - the ObjectDefaults object to use.
     */
    public static void setObjectDefaults(Object object, ObjectDefaults defaults) {
        setObjectDefaults(object, defaults, object.getClass());
    }

    /**
     * Sets default values for all non-static/non-final/non-inherited member fields of the given object using the
     * given ObjectDefaults-derived object and as the referenced class. This can be used to assign default values
     * to a superclass
     *
     * @param object
     * @param defaults
     * @param asClass
     */
    public static void setObjectDefaults(Object object, ObjectDefaults defaults, Class asClass) {
        if (!asClass.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException("Object type " + object.getClass().getCanonicalName()
                    + " is not instance of class: " + asClass.getCanonicalName());
        }

        Field[] fields = asClass.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers()) &&
                    !Modifier.isFinal(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    field.set(object, defaults.getDefaultValue(field.getType()));
                } catch (IllegalAccessException e) {
                    LOG.warn("Unable to set default value for field " + field.getName() + " for class "
                            + field.getType().getCanonicalName());
                }
            }
        }
    }
}
