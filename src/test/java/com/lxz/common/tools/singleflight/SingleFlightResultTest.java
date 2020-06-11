package com.lxz.common.tools.singleflight;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SingleFlightResultTest {

    private SingleFlightResult<String> systemUnderTest;

    @Before
    public void setUp() {

    }

    @Test
    public void testToString1() {
        systemUnderTest = new SingleFlightResult<>("haha", null, false);
        String str = systemUnderTest.toString();
        assertEquals("SingleFlightResult{result=haha, exception=null, shared=false}", str);
    }

    @Test
    public void testToString2() {
        systemUnderTest = new SingleFlightResult<>("hehe", new RuntimeException(), true);
        String str = systemUnderTest.toString();
        assertEquals("SingleFlightResult{result=hehe, exception=java.lang.RuntimeException, shared=true}", str);
    }
}
