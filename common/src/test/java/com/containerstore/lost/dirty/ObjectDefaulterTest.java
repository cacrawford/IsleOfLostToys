package com.containerstore.lost.dirty;

import com.containerstore.common.base.money.Money;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ObjectDefaulterTest {

    @Test
    public void testAssignDefaults() {
        ObjectClass objectClass = new ObjectClass();

        ObjectDefaulter.setObjectDefaults(objectClass);

        assertThat(objectClass.theint, is(0));
        assertThat(objectClass.theInt, is(0));
        assertThat(objectClass.thelong, is(0L));
        assertThat(objectClass.theLong, is(0L));
        assertThat(objectClass.thebool, is(false));
        assertThat(objectClass.theBool, is(false));
        assertThat(objectClass.theString, is(""));
        assertThat(objectClass.theMoney, is(Money.ZERO));
        assertThat(objectClass.theBigDecimal, is(BigDecimal.ZERO));
        assertThat(objectClass.enumWithDefault, is(EnumWithDefault.Default));
        assertThat(objectClass.enumWithoutDefault, is(EnumWithoutDefault.First));
        assertThat(objectClass.objectWithDefault, not(is((ObjectWithDefault) null)));
        assertThat(objectClass.objectWithDefault.getCode(), is(0));
        assertThat(objectClass.objectWithoutDefault, is((ObjectWithoutDefault)null));
    }

    @Test
    public void testAssignDefaultsWithOverride() {
        ObjectClass objectClass = new ObjectClass();

        ObjectDefaulter.setObjectDefaults(objectClass, new TestObjectDefaults());

        assertThat(objectClass.theint, is(0));
        assertThat(objectClass.theInt, is(0));
        assertThat(objectClass.thelong, is(0L));
        assertThat(objectClass.theLong, is(0L));
        assertThat(objectClass.thebool, is(false));
        assertThat(objectClass.theBool, is(false));
        assertThat(objectClass.theMoney, is(Money.ZERO));
        assertThat(objectClass.theBigDecimal, is(BigDecimal.ZERO));
        assertThat(objectClass.enumWithDefault, is(EnumWithDefault.Default));
        assertThat(objectClass.enumWithoutDefault, is(EnumWithoutDefault.First));
        assertThat(objectClass.objectWithDefault, not(is((ObjectWithDefault) null)));
        assertThat(objectClass.objectWithDefault.getCode(), is(0));

        assertThat(objectClass.theString, is("New Default"));
        assertThat(objectClass.objectWithoutDefault, not(is((ObjectWithoutDefault) null)));
        assertThat(objectClass.objectWithoutDefault.getCode(), is(100));
    }

    @Test
    public void testAssignDefaultsClassHierarchy() {
        ObjectSubclass objectSubclass = new ObjectSubclass();

        ObjectDefaulter.setObjectDefaults(objectSubclass);

        assertThat(objectSubclass.theInt, is((Integer)null));
        assertThat(objectSubclass.thedouble, is(0d));
        assertThat(objectSubclass.theDouble, is(0d));

        ObjectDefaulter.setObjectDefaults(objectSubclass, ObjectClass.class);

        assertThat(objectSubclass.theInt, is(0));
        assertThat(objectSubclass.theDouble, is(0d));
    }

    private static class ObjectClass {
        public int theint;
        public Integer theInt;
        protected long thelong;
        protected Long theLong;
        private boolean thebool;
        private Boolean theBool;
        private String theString;
        private Money theMoney;
        private BigDecimal theBigDecimal;
        private EnumWithDefault enumWithDefault;
        private EnumWithoutDefault enumWithoutDefault;
        private ObjectWithDefault objectWithDefault;
        private ObjectWithoutDefault objectWithoutDefault;

        public ObjectClass() {
        }
    }

    private static class ObjectSubclass extends ObjectClass {
        private double thedouble;
        private Double theDouble;

        public ObjectSubclass() {
        }
    }

    private static enum EnumWithDefault {
        First,
        Second,
        Default;

        @Defaulted
        public static EnumWithDefault getDefault() {
            return Default;
        }
    }

    private static enum EnumWithoutDefault {
        First,
        Second,
        Third;
    }

    private static class ObjectWithDefault {

        private int code;
        public ObjectWithDefault(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Defaulted
        public static ObjectWithDefault getDefault() {
            return new ObjectWithDefault(0);
        }
    }

    private static class ObjectWithoutDefault {
        private int code;
        public ObjectWithoutDefault(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private static class TestObjectDefaults extends ObjectDefaults {

        TestObjectDefaults() {
            addSupportedObject(ObjectWithoutDefault.class);
        }

        protected ObjectWithoutDefault getObjectWithoutDefaultDefault(Class clazz) {
            return new ObjectWithoutDefault(100);
        }

        protected String getStringDefault(Class clazz) {
            return "New Default";
        }
    }
}
