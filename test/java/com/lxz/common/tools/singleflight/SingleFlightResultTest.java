package test.java.com.lxz.common.tools.singleflight;

import main.java.com.lxz.common.tools.singleflight.SingleFlightResult;
import org.junit.Assert;
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
        systemUnderTest = new SingleFlightResult<>("haha", false);
        String str = systemUnderTest.toString();
        assertEquals("SingleFlightResult{result=haha, shared=false}", str);
    }

    @Test
    public void testToString2() {
        systemUnderTest = new SingleFlightResult<>("hehe", true);
        String str = systemUnderTest.toString();
        assertEquals("SingleFlightResult{result=hehe, shared=true}", str);
    }
}
