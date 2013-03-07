package com.containerstore.lost.dirty;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * DirtyObject is used to keep track of the private and protected fields in a class to retain null state
 * and yet allow them to have defaulted values. Fields will be marked as "dirty" when they are changed
 * via setValue() or the class is scanned via markAllFields(). This class can be set to "null mode" where
 * all non-dirty values are returned as null.
 *
 * Notes:
 *          This class is intended to be declared as a private/protected field, initialized with the "this" pointer.
 *
 *          Static and public fields are ignored (static is obvious, but public is because access
 *          cannot be controlled and null state cannot be properly ensured).
 *
 *          To activate the dirty field functionality, getters and setters should be implemented using the "setValue"
 *          and "getValue" methods. Member fields with default values will be non-dirty by default (returning null in
 *          null mode), but this behavior can be overridden by an explicit call to markField() or by scanning
 *          all fields using the markAllFields() method.
 *
 *          Note that the "DirtyObjects" class can be used to activate/deactivate the null mode for
 *          any class that contains a DirtyObject member field from an external class.
 */
public class DirtyObject {
    private final static Logger LOG = Logger.getLogger(DirtyObject.class);
    private final Object theObject;
    private final Class asClass;
    private Map<String, Boolean> dirtyList = Maps.newHashMap();
    private List<String> ignoredFields = Lists.newArrayList();
    private boolean emptyContainersAsNull = false;
    private boolean nullMode = false;

    public DirtyObject(Object theObject, Class asClass) {
        this.theObject = theObject;
        this.asClass = asClass;
        buildFieldContainers();
    }

    /**
     * Walks through the list of all fields and sets a default value for every field with a null value
     */
    public void assignDefaults() {
        ObjectDefaults objectDefaults = new ObjectDefaults();

        for (String fieldName : dirtyList.keySet()) {
            Field field = getField(fieldName);
            if (isNull(getValue(fieldName, null))) {
                setValue(fieldName, null, objectDefaults.getDefaultValue(field.getType()));
            }
        }
    }

    /**
     *  Retrieves the value for the given field. If in null mode, will return null
     *  for all non-dirty fields, regardless of their value.
     *
     * @param fieldName - The name of the field to retrieve the value for
     * @param <T>       - The object class being returned
     * @return
     */
    public <T> T getValue(String fieldName, T defaultValue) {
        Field field = getField(fieldName);
        if (field != null) {
            if (nullMode && !isDirty(fieldName)) {
                return null;
            }
        } else {
            field = getIgnoredField(fieldName);
        }

        try {
            return (T)field.get(theObject);
        } catch (Exception e) {
            LOG.error("Unable to get value for field " + fieldName + " for class " + asClass);
        }
        return defaultValue;
    }

    /**
     * Returns whether the given field is marked as "dirty" or not.
     *
     * @param fieldName - The field name to be evaluated
     * @return
     */
    public boolean isDirty(String fieldName) {
        Field field = getField(fieldName);
        if (field != null) {
            return dirtyList.get(fieldName);
        }
        return false;
    }

    /**
     * Walks through the list of all fields and marks those that are non-null as dirty. Designed to be
     * used after object construction, to mark any non-null fields that were set.
     *
     */
    public void markAllFields() {
        List<Field> nonNullFields = Lists.newArrayList();
        for (String fieldName : dirtyList.keySet()) {
            Field field = getField(fieldName);
            if (field != null && !isNull(getValue(field.getName(), null))) {
                nonNullFields.add(field);
            }
        }

        for (Field field : nonNullFields) {
            markField(field, true);
        }
    }

    /**
     * Explicitly marks a field as dirty or non-dirty
     *
     * @param fieldName - The field name to mark
     * @param dirty     - The dirty flag
     */
    public void markField(String fieldName, boolean dirty) {
        Field field = getField(fieldName);
        if (field != null) {
            markField(field, dirty);
        }
    }

    /**
     * If true, empty containers are considered nulls for the purpose of marking as non-dirty.
     *
     * @param emptyContainersAsNull
     */
    public void setEmptyContainersAsNull(boolean emptyContainersAsNull) {
        this.emptyContainersAsNull = emptyContainersAsNull;
    }

    /**
     * Sets the value for the given field name. This call ensures that if a null field is
     * assigned a non-null value, the dirty flag is set.
     *
     * @param fieldName - The name of the field to set the value for
     * @param value     - The value to be set
     * @param <T>       - The class type of value
     */
    public <T> void setValue(String fieldName, T value) {
        setValue(fieldName, value, null);
    }

    /**
     * Sets the value for the given field name. This call ensures that if a null field is
     * assigned a non-null value, the dirty flag is set.
     *
     * @param fieldName     - The name of the field to set the value for
     * @param value         - The value to assign
     * @param defaultValue  - The default value to assign if value is null
     * @param <T>           - The class type of value
     */
    public <T> void setValue(String fieldName, T value, T defaultValue) {
        Field field = getField(fieldName);
        if (field == null) {
            maybeSetIgnoredFieldValue(fieldName, value);
            return;
        }

        if (!dirtyList.get(fieldName) && !isNull(value)) {
            markField(field, true);
        }

        maybeSetFieldValue(field, value == null ? defaultValue : value);
    }

    @VisibleForTesting
    boolean getNullMode() {
        return nullMode;
    }

    /**
     * When called with true, sets mode for class to return null values when a field is not dirty.
     *
     * @param nulLMode  - turns null mode on (true) or off (false)
     */
    public void setNullMode(boolean nulLMode) {
        this.nullMode = nulLMode;
    }

    private void buildFieldContainers() {
        Field[] fields = asClass.getDeclaredFields();

        for (Field field : fields) {
            if (ignoreField(field)) {
                ignoredFields.add(field.getName());
            } else {
                dirtyList.put(field.getName(), false);
            }
        }
    }

    private Field getField(String fieldName) {
        try {
            if(dirtyList.containsKey(fieldName)) {
                Field field = asClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            }

            if (ignoredFields.contains(fieldName)) {
                return null;
            }

            throw new NoSuchFieldException("Field " + fieldName + " unsupported for class "
                    + asClass);
        } catch(NoSuchFieldException e) {
            throw new IllegalArgumentException("Field " + fieldName + " unsupported for class "
                                                + asClass, e);
        }
    }

    private Field getIgnoredField(String fieldName) {
        if (ignoredFields.contains(fieldName)) {
            try {
                Field field = asClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                LOG.error("Unexpected exception getting ignored field name " + fieldName + " for class "
                            + asClass);
            }
        }

        return null;
    }

    private boolean ignoreField(Field field) {
        return (Modifier.isStatic(field.getModifiers())
                || Modifier.isPublic(field.getModifiers())
                || Modifier.isFinal(field.getModifiers())
                || field.getType().isPrimitive()
                || isThisClass(field));
    }

    private boolean isEmptyContainer(Object obj) {
        if (Containers.isContainer(obj)) {
            return Containers.isEmpty(obj);
        }
        return false;
    }

    private <T> boolean isNull(T value) {
        if (emptyContainersAsNull && isEmptyContainer(value)) {
            return true;
        }

        return value == null;
    }

    private boolean isThisClass(Field field) {
        return field.getType().equals(getClass());
    }

    private void markField(Field field, boolean dirty) {
        dirtyList.remove(field.getName());
        dirtyList.put(field.getName(), dirty);
    }

    private <T> void maybeSetFieldValue(Field field, T value) {
        try {
            if (dirtyList.containsKey(field.getName())) {
                field.set(theObject, value);
            }
        } catch (IllegalAccessException e) {
            LOG.error("Unable to set field value " + field.getName() + " for class " + asClass);
        }
    }

    private <T> void maybeSetIgnoredFieldValue(String fieldName, T value) {
        Field field = getIgnoredField(fieldName);
        if (field != null && !isThisClass(field)) {
            try {
                field.set(theObject, value);
            } catch (IllegalAccessException e) {
                LOG.error("Unable to set ignored field value " + field.getName()
                                + " for class " + asClass);
            }
        }
    }
}
