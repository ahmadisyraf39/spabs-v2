package com.ahmadisyraf39.spabs_v2.autotests.sanity;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SanityTest {

    @Test
    public void onePlusOne_equalsTwo() {
        Assert.assertEquals(1 + 1, 2);
    }

}
