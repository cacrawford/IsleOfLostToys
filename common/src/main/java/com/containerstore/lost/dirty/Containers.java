package com.containerstore.lost.dirty;

import java.util.Collection;
import java.util.Map;

public final class Containers {

    private Containers() {
        throw new UnsupportedOperationException();
    }

    public static boolean isContainer(Object obj) {
        if (obj == null) {
            return false;
        }

        return (obj instanceof Collection<?> || obj instanceof Map<?,?>);
    }

    public static <T> boolean isEmpty(T object) {
        if (isContainer(object)) {
            if (object instanceof Collection<?>) {
                return ((Collection) object).isEmpty();
            } else if (object instanceof Map<?,?>) {
                return ((Map) object).isEmpty();
            }
        }
        return true;
    }
}
