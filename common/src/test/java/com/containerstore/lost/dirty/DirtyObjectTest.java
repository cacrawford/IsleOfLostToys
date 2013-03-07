package com.containerstore.lost.dirty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DirtyObjectTest {
    private final static String NULL_STRING = null;
    private final static Integer NULL_INT = null;
    private final static LocalDateTime NULL_LOCALDATETIME = null;
    private final static Boolean NULL_BOOL = null;


    @Test
    public void testFieldNullActivation() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                                                         null);

        assertThat(tester.dirtyObject.isDirty("publicInt"), is(false));
        assertThat(tester.dirtyObject.isDirty("privateInt"), is(false));
        assertThat(tester.dirtyObject.isDirty("protectedInt"), is(false));
        assertThat(tester.dirtyObject.isDirty("dateTime"), is(false));
        assertThat(tester.dirtyObject.isDirty("littleB"), is(false));
        assertThat(tester.dirtyObject.isDirty("bigB"), is(false));
        assertThat(tester.dirtyObject.isDirty("defaultedString"), is(false));
        assertThat(tester.dirtyObject.isDirty("nullString"), is(false));

        DirtyObjects.activateObject(tester);
        assertThat(tester.getPublicInt(), is(10));
        assertThat(tester.getPrivateInt(), is(NULL_INT));
        assertThat(tester.getProtectedInt(), is(NULL_INT));
        assertThat(tester.getDateTime(), is(NULL_LOCALDATETIME));
        assertThat(tester.isLittleB(), is(true));
        assertThat(tester.getBigB(), is(NULL_BOOL));
        assertThat(tester.getDefaultedString(), is(NULL_STRING));
        assertThat(tester.getNullString(), is(NULL_STRING));

        DirtyObjects.deactivateObject(tester);
        assertThat(tester.getPublicInt(), is(10));
        assertThat(tester.getPrivateInt(), is(10));
        assertThat(tester.getProtectedInt(), is(10));
        assertThat(tester.getDateTime(), is(LocalDateTime.parse("2013-03-07")));
        assertThat(tester.isLittleB(), is(true));
        assertThat(tester.getBigB(), is(true));
        assertThat(tester.getDefaultedString(), is("defaultValue"));
        assertThat(tester.getNullString(), is(NULL_STRING));
    }

    @Test
    public void testIgnoredValues() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                                                         null);

        tester.setPublicInt(100);
        assertThat(tester.publicInt, is(100));
        assertThat(tester.getPublicInt(), is(100));
        assertThat(tester.dirtyObject.isDirty("publicInt"), is(false));

        tester.setLittleB(false);
        assertThat(tester.littleB, is(false));
        assertThat(tester.isLittleB(), is(false));
        assertThat(tester.dirtyObject.isDirty("littleB"), is(false));
    }

    @Test
    public void testValues() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                                                         "new value");

        assertThat(tester.getDefaultedString(), is("new value"));
        assertThat(tester.dirtyObject.isDirty("defaultedString"), is(true));

        tester.setPrivateInt(100);
        assertThat(tester.privateInt, is(100));
        assertThat(tester.getPrivateInt(), is(100));
        assertThat(tester.dirtyObject.isDirty("privateInt"), is(true));
        DirtyObjects.activateObject(tester);
        tester.dirtyObject.markField("privateInt", false);
        assertThat(tester.getPrivateInt(), is(NULL_INT));
        DirtyObjects.deactivateObject(tester);

        tester.setProtectedInt(100);
        assertThat(tester.protectedInt, is(100));
        assertThat(tester.getProtectedInt(), is(100));
        assertThat(tester.dirtyObject.isDirty("protectedInt"), is(true));
        DirtyObjects.activateObject(tester);
        tester.dirtyObject.markField("protectedInt", false);
        assertThat(tester.getProtectedInt(), is(NULL_INT));
        DirtyObjects.deactivateObject(tester);

        LocalDateTime testDateTime = LocalDateTime.parse("2020-05-05");
        tester.setDateTime(testDateTime);
        assertThat(tester.dateTime, is(testDateTime));
        assertThat(tester.getDateTime(), is(testDateTime));
        assertThat(tester.dirtyObject.isDirty("dateTime"), is(true));
        DirtyObjects.activateObject(tester);
        tester.dirtyObject.markField("dateTime", false);
        assertThat(tester.getDateTime(), is(NULL_LOCALDATETIME));
        DirtyObjects.deactivateObject(tester);

        tester.setBigB(false);
        assertThat(tester.bigB, is(false));
        assertThat(tester.getBigB(), is(false));
        assertThat(tester.dirtyObject.isDirty("bigB"), is(true));
        DirtyObjects.activateObject(tester);
        tester.dirtyObject.markField("bigB", false);
        assertThat(tester.getBigB(), is(NULL_BOOL));
        DirtyObjects.deactivateObject(tester);
    }

    @Test
    public void testMarkObjectsAfterConstructions() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                "string value");

        tester.dirtyObject.markAllFields();

        assertThat(tester.dirtyObject.isDirty("publicInt"), is(false));
        assertThat(tester.dirtyObject.isDirty("privateInt"), is(true));
        assertThat(tester.dirtyObject.isDirty("protectedInt"), is(true));
        assertThat(tester.dirtyObject.isDirty("dateTime"), is(true));
        assertThat(tester.dirtyObject.isDirty("littleB"), is(false));
        assertThat(tester.dirtyObject.isDirty("bigB"), is(true));
        assertThat(tester.dirtyObject.isDirty("defaultedString"), is(true));
        assertThat(tester.dirtyObject.isDirty("nullString"), is(false));
    }

    @Test
    public void testContainersEmptyAsNull() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                "string value");

        assertThat(tester.dirtyObject.isDirty("list"), is(false));
        assertThat(tester.dirtyObject.isDirty("map"), is(false));

        tester.dirtyObject.setEmptyContainersAsNull(true);

        tester.dirtyObject.markAllFields();

        assertThat(tester.dirtyObject.isDirty("list"), is(false));
        assertThat(tester.dirtyObject.isDirty("map"), is(false));


    }

    @Test
    public void testContainersEmptyNotNull() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                "string value");

        assertThat(tester.dirtyObject.isDirty("list"), is(false));
        assertThat(tester.dirtyObject.isDirty("map"), is(false));

        tester.dirtyObject.setEmptyContainersAsNull(false);

        tester.dirtyObject.markAllFields();

        assertThat(tester.dirtyObject.isDirty("list"), is(true));
        assertThat(tester.dirtyObject.isDirty("map"), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFieldNameIsDirty() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                "string value");

        tester.dirtyObject.isDirty("invalid name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFieldNameGetName() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                "string value");

        tester.dirtyObject.getValue("invalid name", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFieldNameSetValue() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                "string value");

        tester.dirtyObject.setValue("invalid name", 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFieldNameGetValue() {
        DirtyFieldsTester tester = new DirtyFieldsTester(10, 10, 10, LocalDateTime.parse("2013-03-07"), true, true,
                "string value");

        tester.dirtyObject.getValue("invalid name", null);
    }

    @Test
    public void testSetDefaults() {
        DefaultsTester tester = new DefaultsTester();

        tester.dirtyObject.assignDefaults();

        assertThat(tester.publicInt, is(NULL_INT));
        assertThat(tester.privateInt, is(0));
        assertThat(tester.protectedInt, is(0));
        assertThat(tester.dateTime, is(NULL_LOCALDATETIME));
        assertThat(tester.littleB, is(false));
        assertThat(tester.bigB, is(false));
        assertThat(tester.defaultedString, is("defaultValue"));
        assertThat(tester.nullString, is(""));
        assertThat(tester.list.size(), is(0));
        assertThat(tester.map.size(), is(0));
    }

    private static class DirtyFieldsTester {
        DirtyObject dirtyObject = new DirtyObject(this, DirtyFieldsTester.class);

        public Integer publicInt;
        private Integer privateInt;
        protected Integer protectedInt;

        private LocalDateTime dateTime;
        private boolean littleB;
        private Boolean bigB;

        private String defaultedString = "defaultValue";
        private String nullString;

        private List<String> list = Lists.newArrayList();
        private Map<String,String> map = Maps.newHashMap();

        public DirtyFieldsTester(Integer publicInt, Integer privateInt, Integer protectedInt,
                                 LocalDateTime dateTime, boolean littleB, Boolean bigB,
                                 String defaultedString) {

            this.publicInt = publicInt;
            this.privateInt = privateInt;
            this.protectedInt = protectedInt;
            this.dateTime = dateTime;
            this.littleB = littleB;
            this.bigB = bigB;

            dirtyObject.setValue("defaultedString", defaultedString, "defaultValue");
        }

        public Integer getPublicInt() {
            return dirtyObject.getValue("publicInt", publicInt);
        }

        public void setPublicInt(Integer publicInt) {
            dirtyObject.setValue("publicInt", publicInt);
        }

        public Integer getPrivateInt() {
            return dirtyObject.getValue("privateInt", privateInt);
        }

        public void setPrivateInt(Integer privateInt) {
            dirtyObject.setValue("privateInt", privateInt);
        }

        public Integer getProtectedInt() {
            return dirtyObject.getValue("protectedInt", protectedInt);
        }

        public void setProtectedInt(Integer protectedInt) {
            dirtyObject.setValue("protectedInt", protectedInt);
        }

        public LocalDateTime getDateTime() {
            return dirtyObject.getValue("dateTime", dateTime);
        }

        public void setDateTime(LocalDateTime dateTime) {
            dirtyObject.setValue("dateTime", dateTime);
        }

        public boolean isLittleB() {
            return littleB;
        }

        public void setLittleB(boolean littleB) {
            dirtyObject.setValue("littleB", littleB);
        }

        public Boolean getBigB() {
            return dirtyObject.getValue("bigB", bigB);
        }

        public void setBigB(Boolean bigB) {
            dirtyObject.setValue("bigB", bigB);
        }

        public String getDefaultedString() {
            return dirtyObject.getValue("defaultedString", defaultedString);
        }

        public void setDefaultedString(String defaultedString) {
            this.defaultedString = defaultedString;
        }

        public String getNullString() {
            return dirtyObject.getValue("nullString", nullString);
        }

        public void setNullString(String nullString) {
            dirtyObject.setValue("nullString", nullString);
        }
    }

    private static class DefaultsTester {
        private DirtyObject dirtyObject = new DirtyObject(this, DefaultsTester.class);

        public Integer publicInt;
        private Integer privateInt;
        protected Integer protectedInt;
        private LocalDateTime dateTime;
        private boolean littleB;
        private Boolean bigB;
        private String defaultedString = "defaultValue";
        private String nullString;
        private List<String> list;
        private Map<String,String> map;
    }
}
