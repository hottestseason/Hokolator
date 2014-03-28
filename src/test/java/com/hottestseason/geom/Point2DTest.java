package com.hottestseason.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class Point2DTest {
    @Test
    public void testHashCode() {
        assertEquals(new Point2D().hashCode(), new Point2D(0, 0).hashCode());
    }

    @Test
    public void testEquals() {
        assertEquals(new Point2D(), new Point2D(0, 0));
        assertNotEquals(new Point2D(), new Point2D(1, 0));
    }
}
