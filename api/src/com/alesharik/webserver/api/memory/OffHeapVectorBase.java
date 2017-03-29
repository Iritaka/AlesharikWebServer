package com.alesharik.webserver.api.memory;

import one.nio.util.JavaInternals;
import sun.misc.Unsafe;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * ┌────────────┬─────────────┬───────────┬──────┐
 * │ BASE(long) │ COUNT(long) │ MAX(long) │ DATA │
 * └────────────┴─────────────┴───────────┴──────┘
 * This is base of OffHeapVector. It contains basic methods array methods for size, maximum size, etc
 *
 * @implNote you need to write your own <code>set()</code>, <code>get()</code>, etc methods
 */
@NotThreadSafe
public abstract class OffHeapVectorBase {
    protected static final int DEFAULT_INITIAL_COUNT = 16;
    /**
     * <code>newCapacity = oldCapacity + RESIZE_DELTA</code>
     */
    protected static final int RESIZE_DELTA = 20;

    protected static final Unsafe unsafe = JavaInternals.getUnsafe();

    /**
     * Size - <code>sizeof(long) == 8</code><br>
     * Hold <code>sizeof(T)</code>
     */
    protected static final long BASE_FIELD_SIZE = 8L;
    /**
     * Size - <code>sizeof(long) == 8</code><br>
     * Hold array size
     */
    protected static final long COUNT_FIELD_SIZE = 8L;
    /**
     * Size - <code>sizeof(long) == 8</code><br>
     * Hold array max size
     */
    protected static final long MAX_FIELD_SIZE = 8L;
    /**
     * Size - <code>((sizeof(long) == 8) * 3) == 24</code><br>
     * All meta information size
     */
    protected static final long META_SIZE = COUNT_FIELD_SIZE + BASE_FIELD_SIZE + MAX_FIELD_SIZE;

    /**
     * Initial size of all arrays after {@link #allocate()}
     */
    protected final long initialCount;

    public OffHeapVectorBase() {
        this(DEFAULT_INITIAL_COUNT);
    }

    /**
     * Created array initial size
     */
    public OffHeapVectorBase(long initialCount) {
        this.initialCount = initialCount;
    }

    /**
     * Allocate offHeap memory for array. YOU MUST DO {@link #free(long)} BEFORE CLEAN YOUR OBJECT BY GARBAGE COLLECTOR
     *
     * @return memory address
     */
    public long allocate() {
        long address = unsafe.allocateMemory(META_SIZE + (initialCount * getElementSize())); //malloc
        unsafe.putLong(address, getElementSize()); //put BASE(element size)
        unsafe.putLong(address + BASE_FIELD_SIZE, 0L); //put COUNT(array size) == 0
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, initialCount); //put MAX(max size) == initialCount
        return address;
    }

    /**
     * Delete memory region
     *
     * @param address array pointer (array memory block address)
     */
    public void free(long address) {
        unsafe.freeMemory(address);
    }

    /**
     * Return array size
     *
     * @param address array pointer (array memory block address)
     */
    public long size(long address) {
        return unsafe.getLong(address + BASE_FIELD_SIZE);
    }

    public boolean isEmpty(long address) {
        return size(address) == 0;
    }

    /**
     * Remove element with specific index
     *
     * @param address array pointer (array memory block address)
     * @param i       element index
     */
    public void remove(long address, long i) {
        long count = size(address);
        if(i < 0) {
            throw new IllegalArgumentException();
        }
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        unsafe.copyMemory(address + META_SIZE + getElementSize() * (i + 1), address + META_SIZE + getElementSize() * i, getElementSize() * (count - i));
        decrementSize(address);
    }

    /**
     * Free all unused memory
     *
     * @param address array pointer (array memory block address)
     * @return new address
     */
    public long shrink(long address) {
        long size = size(address);
        if(getMax(address) > size) {
            resize(address, META_SIZE + getElementSize() * size);
        }
        return address;
    }

    /**
     * Resize array on elementCount
     *
     * @param address      array pointer (array memory block address)
     * @param elementCount array element count. May be less than maximum
     * @return new array pointer
     */
    protected final long resize(long address, long elementCount) {
        address = unsafe.reallocateMemory(address, META_SIZE + elementCount * getElementSize());
        setMax(address, elementCount);
        return address;
    }

    /**
     * Return maximum array size
     *
     * @param address array pointer (memory block address)
     */
    protected final long getMax(long address) {
        return unsafe.getLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE);
    }

    /**
     * Set maximum array size
     *
     * @param address array pointer (array memory block address)
     * @param max     new maximum array size
     */
    protected final void setMax(long address, long max) {
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, max);
    }

    /**
     * Increment array size
     *
     * @param address array pointer (array memory block address)
     */
    protected final void incrementSize(long address) {
        unsafe.putLong(address + BASE_FIELD_SIZE, size(address) + 1);
    }

    /**
     * Decrement array size
     *
     * @param address array pointer (array memory block address)
     */
    protected final void decrementSize(long address) {
        unsafe.putLong(address + BASE_FIELD_SIZE, size(address) - 1);
    }

    protected final long checkBounds(long address, long next) {
        long max = getMax(address);
        if(next >= max) {
            address = resize(address, max + RESIZE_DELTA);
        }
        return address;
    }

    protected void checkIndexBounds(long address, long i) {
        long count = size(address);
        if(i < 0) {
            throw new IllegalArgumentException();
        }
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Return element size in bytes
     */
    protected abstract long getElementSize();
}