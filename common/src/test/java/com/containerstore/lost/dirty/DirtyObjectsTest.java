package com.containerstore.lost.dirty;

import com.containerstore.common.base.money.Money;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DirtyObjectsTest {

    @Test
    public void testOneLevel() {
        LevelOne levelOne = new LevelOne();

        levelOne.defaultLevelOne = 100;

        assertThat(levelOne.getDefaultLevelOne(), is(100));

        DirtyObjects.activateObject(levelOne);
        assertThat(levelOne.getLevel1DirtyObject().getNullMode(), is(true));
        assertThat(levelOne.getDefaultLevelOne(), is((Integer)null));

        DirtyObjects.deactivateObject(levelOne);
        assertThat(levelOne.getLevel1DirtyObject().getNullMode(), is(false));
        assertThat(levelOne.getDefaultLevelOne(), is(100));
    }

    @Test
    public void testTwoLevels() {
        LevelTwo testClass = new LevelTwo();

        testClass.defaultLevelOne = 100;
        testClass.defaultLevelTwo = "100";

        assertThat(testClass.getDefaultLevelOne(), is(100));
        assertThat(testClass.getDefaultLevelTwo(), is("100"));

        DirtyObjects.activateObject(testClass);
        assertThat(testClass.getLevel1DirtyObject().getNullMode(), is(true));
        assertThat(testClass.getLevel2DirtyObject().getNullMode(), is(true));
        assertThat(testClass.getDefaultLevelOne(), is((Integer)null));
        assertThat(testClass.getDefaultLevelTwo(), is((String)null));

        DirtyObjects.deactivateObject(testClass);
        assertThat(testClass.getLevel1DirtyObject().getNullMode(), is(false));
        assertThat(testClass.getLevel2DirtyObject().getNullMode(), is(false));
        assertThat(testClass.getDefaultLevelOne(), is(100));
        assertThat(testClass.getDefaultLevelTwo(), is("100"));
    }

    @Test
    public void testThreeLevels() {
        LevelThree testClass = new LevelThree();

        testClass.defaultLevelOne = 100;
        testClass.defaultLevelTwo = "100";
        testClass.defaultLevelThree = new Money("100");

        assertThat(testClass.getDefaultLevelOne(), is(100));
        assertThat(testClass.getDefaultLevelTwo(), is("100"));
        assertThat(testClass.getDefaultLevelThree(), is(new Money("100")));

        DirtyObjects.activateObject(testClass);
        assertThat(testClass.getLevel1DirtyObject().getNullMode(), is(true));
        assertThat(testClass.getLevel2DirtyObject().getNullMode(), is(true));
        assertThat(testClass.getLevel3DirtyObject(), is((DirtyObject) null));
        assertThat(testClass.getDefaultLevelOne(), is((Integer)null));
        assertThat(testClass.getDefaultLevelTwo(), is((String)null));
        assertThat(testClass.getDefaultLevelThree(), is(new Money("100")));

        DirtyObjects.deactivateObject(testClass);
        assertThat(testClass.getLevel1DirtyObject().getNullMode(), is(false));
        assertThat(testClass.getLevel2DirtyObject().getNullMode(), is(false));
        assertThat(testClass.getLevel3DirtyObject(), is((DirtyObject) null));
        assertThat(testClass.getDefaultLevelOne(), is(100));
        assertThat(testClass.getDefaultLevelTwo(), is("100"));
        assertThat(testClass.getDefaultLevelThree(), is(new Money("100")));
    }

    @Test
    public void testFourLevels() {
        LevelFour testClass = new LevelFour();

        testClass.defaultLevelOne = 100;
        testClass.defaultLevelTwo = "100";
        testClass.defaultLevelThree = new Money("100");
        testClass.defaultLevelFour = Double.valueOf("100");

        assertThat(testClass.getDefaultLevelOne(), is(100));
        assertThat(testClass.getDefaultLevelTwo(), is("100"));
        assertThat(testClass.getDefaultLevelThree(), is(new Money("100")));
        assertThat(testClass.getDefaultLevelFour(), is(Double.valueOf("100")));

        DirtyObjects.activateObject(testClass);
        assertThat(testClass.getLevel1DirtyObject().getNullMode(), is(true));
        assertThat(testClass.getLevel2DirtyObject().getNullMode(), is(true));
        assertThat(testClass.getLevel3DirtyObject(), is((DirtyObject) null));
        assertThat(testClass.getLevel4DirtyObject().getNullMode(), is(true));
        assertThat(testClass.getDefaultLevelOne(), is((Integer)null));
        assertThat(testClass.getDefaultLevelTwo(), is((String)null));
        assertThat(testClass.getDefaultLevelThree(), is(new Money("100")));
        assertThat(testClass.getDefaultLevelFour(), is((Double)null));

        DirtyObjects.deactivateObject(testClass);
        assertThat(testClass.getLevel1DirtyObject().getNullMode(), is(false));
        assertThat(testClass.getLevel2DirtyObject().getNullMode(), is(false));
        assertThat(testClass.getLevel3DirtyObject(), is((DirtyObject)null));
        assertThat(testClass.getLevel4DirtyObject().getNullMode(), is(false));
        assertThat(testClass.getDefaultLevelOne(), is(100));
        assertThat(testClass.getDefaultLevelTwo(), is("100"));
        assertThat(testClass.getDefaultLevelThree(), is(new Money("100")));
        assertThat(testClass.getDefaultLevelFour(), is(Double.valueOf("100")));
    }

    @Test
    public void testClassesWithoutDirtyObject() {
        // Expecting no exceptions

        int primitive = 0;

        DirtyObjects.activateObject(new Object());
        DirtyObjects.activateObject(Lists.newLinkedList());
        DirtyObjects.activateObject(new JdbcTemplate());
        DirtyObjects.activateObject(primitive);
    }

    @Test
    public void testSetDefaultsForHierarchy() {
        LevelFour testClass = new LevelFour();

        DirtyObjects.assignDefaultsToHierarchy(testClass);

        assertThat(testClass.defaultLevelFour, is(0d));
        assertThat(testClass.defaultLevelThree, is((Money)null));
        assertThat(testClass.defaultLevelTwo, is(""));
        assertThat(testClass.defaultLevelOne, is(0));

    }

    private static class LevelOne {
        private DirtyObject dirtyObject = new DirtyObject(this, LevelOne.class);

        protected Integer defaultLevelOne;

        public DirtyObject getLevel1DirtyObject() {
            return dirtyObject;
        }

        public Integer getDefaultLevelOne() {
            return dirtyObject.getValue("defaultLevelOne", defaultLevelOne);
        }
    }

    private static class LevelTwo extends LevelOne {
        private DirtyObject dirtyObject = new DirtyObject(this, LevelTwo.class);

        protected String defaultLevelTwo;

        public DirtyObject getLevel2DirtyObject() {
            return dirtyObject;
        }

        public String getDefaultLevelTwo() {
            return dirtyObject.getValue("defaultLevelTwo", defaultLevelTwo);
        }
    }

    private static class LevelThree extends LevelTwo {
        protected Money defaultLevelThree;

        public DirtyObject getLevel3DirtyObject() {
            return null;
        }

        public Money getDefaultLevelThree() {
            return defaultLevelThree;
        }
    }

    private static class LevelFour extends LevelThree {
        private DirtyObject dirtyObject = new DirtyObject(this, LevelFour.class);

        protected Double defaultLevelFour;

        public DirtyObject getLevel4DirtyObject() {
            return dirtyObject;
        }

        public Double getDefaultLevelFour() {
            return dirtyObject.getValue("defaultLevelFour", defaultLevelFour);
        }
    }

}
