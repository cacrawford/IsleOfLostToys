package com.containerstore.lost.dirty;

import com.containerstore.common.base.money.Money;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Defaults.*;

/**
 * This class allows one to receive a default value for any class type. It supports non-null primitives,
 * primitive wrappers, enumerations, String, List, Map, Money, BigDecimal, and @Defaulted objects.
 *
 * If an object is not specifically defined, it returns null.
 *
 * If Enumerations are not @Defaulted, it returns the first enumeration value.
 *
 * This class may be extended to support defaults for new types, or to change defaults for existing types.
 * To extend, the subclass must call "addSupportedClass" with the desired class object, and then create
 * a function with the signature "getClassDefault(Class class)" that returns a class object.
 *
 * ObjectDefaulter is a helper class that allows defaults to be set for all fields of a desired class.
 */
public class ObjectDefaults {

    private List<Class> supportedObjects = Lists.<Class>newArrayList(
            Boolean.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Double.class,
            Float.class,
            String.class,
            List.class,
            Map.class,
            Money.class,
            BigDecimal.class
    );

    public ObjectDefaults() {
    }

    /**
     * Returns the default value defined for the given class type.
     *
     * @param clazz - class type
     * @param <T>   - class object type
     * @return      - default value
     */
    public <T> T getDefaultValue(Class<T> clazz) {
        if (clazz.isPrimitive()) {
            return getPrimitiveDefault(clazz);
        }

        Method supportedMethod = getMethod(clazz);
        if (supportedMethod != null) {
            try {
                return (T) supportedMethod.invoke(this, clazz);
            } catch (Exception e) {
                throw new IllegalStateException("Exception invoking method for class " + clazz.getCanonicalName()
                                                    + ": " + e.getMessage());
            }
        }

        if (clazz.isEnum()) {
            return getEnumDefault(clazz);
        }

        return getObjectDefault(clazz);
    }

    protected void addSupportedObject(Class clazz) {
        supportedObjects.add(clazz);
    }

    protected <T> T getBigDecimalDefault(Class<T> clazz) {
        return (T)BigDecimal.ZERO;
    }

    protected <T> Boolean getBooleanDefault(Class<T> clazz) {
        return Boolean.FALSE;
    }

    protected <T> Byte getByteDefault(Class<T> clazz) {
        return Byte.valueOf("0");
    }

    protected <T> Double getDoubleDefault(Class<T> clazz) {
        return Double.valueOf("0.0");
    }

    protected <T> T getEnumDefault(Class<T> clazz) {
        T value = getObjectDefault(clazz);
        if (value == null) {
            value = clazz.getEnumConstants()[0];
        }

        return value;
    }

    protected <T> Float getFloatDefault(Class<T> clazz) {
        return Float.valueOf("0.0");
    }

    protected <T> Integer getIntegerDefault(Class<T> clazz) {
        return Integer.valueOf("0");
    }

    protected <T> T getListDefault(Class<T> clazz) {
        return (T)Lists.newLinkedList();
    }

    protected <T> Long getLongDefault(Class<T> clazz) {
        return Long.valueOf("0");
    }

    protected <T> T getMapDefault(Class<T> clazz) {
        return (T)Maps.newLinkedHashMap();
    }

    protected <T> T getMoneyDefault(Class<T> clazz) {
        return (T)Money.ZERO;
    }

    protected <T> T getObjectDefault(Class<T> clazz) {
        // Determine if defaulted
        for (Method each : clazz.getMethods()) {
            each.setAccessible(true);
            if (each.getAnnotation(Defaulted.class) != null
                    && Modifier.isStatic(each.getModifiers())
                    && each.getParameterTypes().length == 0
                    && clazz.equals(each.getReturnType())) {

                try {
                    return (T)each.invoke(null);
                } catch (Exception e) {
                    throw new IllegalStateException("Excepting invoking enumeration class " + clazz.getName(), e);
                }
            }
        }

        // Else, nothing to do but return null
        return null;
    }

    protected <T> T getPrimitiveDefault(Class<T> clazz) {
        return defaultValue(clazz);
    }

    protected <T> Short getShortDefault(Class<T> clazz) {
        return Short.valueOf("0");
    }

    protected <T> String getStringDefault(Class<T> clazz) {
        return "";
    }

    private String getClassMethodName(Class clazz) {
        String name = clazz.getCanonicalName();
        int pos = name.lastIndexOf('.');
        if (pos != -1) {
            name = name.substring(pos+1);
        }
        return "get" + name + "Default";
    }

    private Method getMethod(Class clazz) {
        // Using reflection, we have to do our own polymorphism. If a method doesn't exist for the current class,
        // crawl up the hierarchy until we get to
        if (!supportedObjects.contains(clazz)) {
            return null;
        }

        Class thisClazz = getClass();
        String methodName = getClassMethodName(clazz);
        while(true) {
            try {
                Method method = thisClazz.getDeclaredMethod(methodName, Class.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                // Continue to check through superclasses
            }

            if (thisClazz.equals(ObjectDefaults.class)) {
                break;
            }
            thisClazz = thisClazz.getSuperclass();
        }

        throw new IllegalStateException("No method " + methodName + "(Class clazz) is defined");
    }
}
