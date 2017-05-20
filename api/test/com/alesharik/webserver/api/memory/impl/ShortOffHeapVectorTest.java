package com.alesharik.webserver.api.memory.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ShortOffHeapVectorTest {
    private static final short FIRST_VALUE = 6234;
    private static final short SECOND_VALUE = 621;
    private static final short THIRD_VALUE = 6112;
    private static final short FOURTH_VALUE = 6234;
    private static final short DUDE = 987;
    private static final short NOT_EXISTS = 562;

    private static final ShortOffHeapVector array = new ShortOffHeapVector();
    private long address;

    @Before
    public void setUp() throws Exception {
        address = array.allocate();
        address = array.add(address, FIRST_VALUE);
        address = array.add(address, SECOND_VALUE);
        address = array.add(address, THIRD_VALUE);
        address = array.add(address, FOURTH_VALUE);
        address = array.add(address, DUDE); //i = 4
    }

    @After
    public void tearDown() throws Exception {
        array.free(address);
    }

    @Test
    public void instanceTest() throws Exception {
        ShortOffHeapVector v = ShortOffHeapVector.instance();
        long address = v.allocate();
        v.add(address, FIRST_VALUE);
        assertEquals(FIRST_VALUE, v.get(address, 0), 0);
        v.free(address);
    }

    @Test
    public void formCharArray() throws Exception {
        short[] arr = {FIRST_VALUE, SECOND_VALUE, THIRD_VALUE, FOURTH_VALUE};
        long addr = 0;
        try {
            addr = array.fromShortArray(arr);
            assertEquals(FIRST_VALUE, array.get(addr, 0), 0);
            assertEquals(SECOND_VALUE, array.get(addr, 1), 0);
            assertEquals(THIRD_VALUE, array.get(addr, 2), 0);
            assertEquals(FOURTH_VALUE, array.get(addr, 3), 0);
        } finally {
            array.free(addr);
        }
    }

    @Test
    public void toCharArray() throws Exception {
        short[] arr = array.toShortArray(address);
        assertEquals(arr[0], FIRST_VALUE, 0);
        assertEquals(SECOND_VALUE, arr[1], 0);
        assertEquals(THIRD_VALUE, arr[2], 0);
        assertEquals(FOURTH_VALUE, arr[3], 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getILessThanZero() throws Exception {
        array.get(address, -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getIMoreThanCount() throws Exception {
        array.get(address, 300);
    }

    @Test
    public void getNormal() throws Exception {
        assertEquals(FIRST_VALUE, array.get(address, 0), 0);
        assertEquals(SECOND_VALUE, array.get(address, 1), 0);
        assertEquals(THIRD_VALUE, array.get(address, 2), 0);
        assertEquals(FOURTH_VALUE, array.get(address, 3), 0);
    }

    @Test
    public void addTest() throws Exception {
        for(int i = 0; i < 200; i++) {
            address = array.add(address, DUDE);
        }
    }

    @Test
    public void containsTest() throws Exception {
        assertTrue(array.contains(address, FIRST_VALUE));
        assertTrue(array.contains(address, SECOND_VALUE));
        assertTrue(array.contains(address, THIRD_VALUE));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorTest() throws Exception {
        Iterator<Short> iter = array.iterator(address);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), FIRST_VALUE, 0);

        assertTrue(iter.hasNext());
        assertEquals(iter.next(), SECOND_VALUE, 0);

        assertTrue(iter.hasNext());
        assertEquals(iter.next(), THIRD_VALUE, 0);

        assertTrue(iter.hasNext());

        AtomicInteger counter = new AtomicInteger(0);
        iter.forEachRemaining(a -> counter.incrementAndGet());
        assertTrue(counter.get() > 0);

        assertNull(iter.next());

        iter.remove();
    }

    @Test
    public void forEachTest() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        array.forEach(address, aDouble -> counter.getAndIncrement());
        assertTrue(counter.get() > 3);
    }

    @Test
    public void indexOfExists() throws Exception {
        assertEquals(0, array.indexOf(address, FIRST_VALUE));
        assertEquals(1, array.indexOf(address, SECOND_VALUE));
        assertEquals(2, array.indexOf(address, THIRD_VALUE));
        assertEquals(0, array.indexOf(address, FOURTH_VALUE));
    }

    @Test
    public void indexOfNotExists() throws Exception {
        assertEquals(-1, array.indexOf(address, NOT_EXISTS));
    }

    @Test
    public void lastIndexOfExists() throws Exception {
        assertEquals(3, array.lastIndexOf(address, FOURTH_VALUE));
        assertEquals(1, array.lastIndexOf(address, SECOND_VALUE));
        assertEquals(2, array.lastIndexOf(address, THIRD_VALUE));
    }

    @Test
    public void lastIndexOfNotExists() throws Exception {
        assertEquals(-1, array.lastIndexOf(address, NOT_EXISTS));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void setLessThanZero() throws Exception {
        array.set(address, -1, DUDE);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void setMoreThanCount() throws Exception {
        array.set(address, 1000, DUDE);
    }

    @Test
    public void setNormal() throws Exception {
        array.set(address, 4, SECOND_VALUE);
        assertEquals(SECOND_VALUE, array.get(address, 4), 0);
    }

    @Test
    public void removeExists() throws Exception {
        assertTrue(array.remove(address, DUDE));
    }

    @Test
    public void removeNotExists() throws Exception {
        assertFalse(array.remove(address, NOT_EXISTS));
    }

    @Test
    public void getElementSizeTest() throws Exception {
        assertEquals(2L, array.getElementSize());
    }
}