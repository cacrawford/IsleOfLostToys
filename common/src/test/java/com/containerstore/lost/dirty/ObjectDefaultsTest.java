package com.containerstore.lost.dirty;

import com.containerstore.common.base.money.Money;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ObjectDefaultsTest {

    @Test
    public void testPrimitivesAndWrappers() {
        ObjectDefaults objectDefaults = new ObjectDefaults();

        assertThat(objectDefaults.getDefaultValue(int.class), is(0));
        assertThat(objectDefaults.getDefaultValue(Integer.class), is(0));
        assertThat(objectDefaults.getDefaultValue(long.class), is(0L));
        assertThat(objectDefaults.getDefaultValue(Long.class), is(0L));
        assertThat(objectDefaults.getDefaultValue(double.class), is(0d));
        assertThat(objectDefaults.getDefaultValue(Double.class), is(0d));
        assertThat(objectDefaults.getDefaultValue(float.class), is(0f));
        assertThat(objectDefaults.getDefaultValue(Float.class), is(0f));
        assertThat(objectDefaults.getDefaultValue(boolean.class), is(false));
        assertThat(objectDefaults.getDefaultValue(Boolean.class), is(false));
        assertThat(objectDefaults.getDefaultValue(byte.class), is(Byte.valueOf("0")));
        assertThat(objectDefaults.getDefaultValue(Byte.class), is(Byte.valueOf("0")));
        assertThat(objectDefaults.getDefaultValue(short.class), is(Short.valueOf("0")));
        assertThat(objectDefaults.getDefaultValue(Short.class), is(Short.valueOf("0")));
    }

    @Test
    public void testMappedObjects() {
        ObjectDefaults objectDefaults = new ObjectDefaults();

        assertThat(objectDefaults.getDefaultValue(String.class), is(""));
        assertThat(objectDefaults.getDefaultValue(List.class).size(), is(0));
        assertThat(objectDefaults.getDefaultValue(Map.class).size(), is(0));
        assertThat(objectDefaults.getDefaultValue(Money.class), is(Money.ZERO));
        assertThat(objectDefaults.getDefaultValue(BigDecimal.class), is(BigDecimal.ZERO));
    }

    @Test
    public void testEnum() {
        ObjectDefaults objectDefaults = new ObjectDefaults();

        assertThat(objectDefaults.getDefaultValue(NoDefault.class), is(NoDefault.First));
        assertThat(objectDefaults.getDefaultValue(HasDefault.class), is(HasDefault.Third));
    }

    @Test
    public void testExtensibility() {
        NewObjectDefaults newObjectDefaults = new NewObjectDefaults();

        assertThat(newObjectDefaults.getDefaultValue(StringBuilder.class), not(is((StringBuilder)null)));
        assertThat(newObjectDefaults.getDefaultValue(String.class), is("New Default"));
    }

    private static enum NoDefault {
        First,
        Second,
        Third;
    }

    private static enum HasDefault {
        First,
        Second,
        Third;

        @Defaulted
        public static HasDefault getDefault() {
            return Third;
        }
    }

    private static class NewObjectDefaults extends ObjectDefaults {

        public NewObjectDefaults() {
            addSupportedObject(StringBuilder.class);
        }

        protected <T> StringBuilder getStringBuilderDefault(Class<T> clazz) {
            return new StringBuilder();
        }

        @Override
        protected String getStringDefault(Class clazz) {
            return "New Default";
        }
    }
}
