/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.nasa.worldwind.util;

import java.util.Arrays;

/**
 * SparseIntArrays map integers to integers.  Unlike a normal array of integers,
 * there can be gaps in the indices.  It is intended to be more memory efficient
 * than using a HashMap to map Integers to Integers, both because it avoids
 * auto-boxing keys and values and its data structure doesn't rely on an extra entry object
 * for each mapping.
 *
 * <p>Note that this container keeps its mappings in an array data structure,
 * using a binary search to find keys.  The implementation is not intended to be appropriate for
 * data structures
 * that may contain large numbers of items.  It is generally slower than a traditional
 * HashMap, since lookups require a binary search and adds and removes require inserting
 * and deleting entries in the array.  For containers holding up to hundreds of items,
 * the performance difference is not significant, less than 50%.</p>
 *
 * <p>It is possible to iterate over the items in this container using
 * {@link #keyAt(int)} and {@link #valueAt(int)}. Iterating over the keys using
 * <code>keyAt(int)</code> with ascending values of the index will return the
 * keys in ascending order, or the values corresponding to the keys in ascending
 * order in the case of <code>valueAt(int)</code>.</p>
 */
public class SparseIntArray implements Cloneable {
    private int[] mKeys;
    private int[] mValues;
    private int mSize;
    /**
     * Creates a new SparseIntArray containing no mappings.
     */
    public SparseIntArray() {
        this(10);
    }
    /**
     * Creates a new SparseIntArray containing no mappings that will not
     * require any additional memory allocation to store the specified
     * number of mappings.  If you supply an initial capacity of 0, the
     * sparse array will be initialized with a light-weight representation
     * not requiring any additional array allocations.
     */
    public SparseIntArray(int initialCapacity) {
        if (initialCapacity == 0) {
            mKeys = EmptyArray.INT;
            mValues = EmptyArray.INT;
        } else {
            mKeys = new int[initialCapacity];
            mValues = new int[mKeys.length];
        }
        mSize = 0;
    }
    @Override
    public SparseIntArray clone() {
        SparseIntArray clone = null;
        try {
            clone = (SparseIntArray) super.clone();
            clone.mKeys = mKeys.clone();
            clone.mValues = mValues.clone();
        } catch (CloneNotSupportedException cnse) {
            /* ignore */
        }
        return clone;
    }
    /**
     * Gets the int mapped from the specified key, or <code>0</code>
     * if no such mapping has been made.
     */
    public int get(int key) {
        return get(key, 0);
    }
    /**
     * Gets the int mapped from the specified key, or the specified value
     * if no such mapping has been made.
     */
    public int get(int key, int valueIfKeyNotFound) {
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        if (i < 0) {
            return valueIfKeyNotFound;
        } else {
            return mValues[i];
        }
    }
    /**
     * Removes the mapping from the specified key, if there was any.
     */
    public void delete(int key) {
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        if (i >= 0) {
            removeAt(i);
        }
    }
    /**
     * Removes the mapping at the given index.
     */
    public void removeAt(int index) {
        System.arraycopy(mKeys, index + 1, mKeys, index, mSize - (index + 1));
        System.arraycopy(mValues, index + 1, mValues, index, mSize - (index + 1));
        mSize--;
    }
    /**
     * Adds a mapping from the specified key to the specified value,
     * replacing the previous mapping from the specified key if there
     * was one.
     */
    public void put(int key, int value) {
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        if (i >= 0) {
            mValues[i] = value;
        } else {
            i = ~i;
            mKeys = GrowingArrayUtils.insert(mKeys, mSize, i, key);
            mValues = GrowingArrayUtils.insert(mValues, mSize, i, value);
            mSize++;
        }
    }
    /**
     * Returns the number of key-value mappings that this SparseIntArray
     * currently stores.
     */
    public int size() {
        return mSize;
    }
    /**
     * Given an index in the range <code>0...size()-1</code>, returns
     * the key from the <code>index</code>th key-value mapping that this
     * SparseIntArray stores.
     *
     * <p>The keys corresponding to indices in ascending order are guaranteed to
     * be in ascending order, e.g., <code>keyAt(0)</code> will return the
     * smallest key and <code>keyAt(size()-1)</code> will return the largest
     * key.</p>
     */
    public int keyAt(int index) {
        return mKeys[index];
    }
    /**
     * Given an index in the range <code>0...size()-1</code>, returns
     * the value from the <code>index</code>th key-value mapping that this
     * SparseIntArray stores.
     *
     * <p>The values corresponding to indices in ascending order are guaranteed
     * to be associated with keys in ascending order, e.g.,
     * <code>valueAt(0)</code> will return the value associated with the
     * smallest key and <code>valueAt(size()-1)</code> will return the value
     * associated with the largest key.</p>
     */
    public int valueAt(int index) {
        return mValues[index];
    }
    /**
     * Directly set the value at a particular index.
     * @hide
     */
    public void setValueAt(int index, int value) {
        mValues[index] = value;
    }
    /**
     * Returns the index for which {@link #keyAt} would return the
     * specified key, or a negative number if the specified
     * key is not mapped.
     */
    public int indexOfKey(int key) {
        return ContainerHelpers.binarySearch(mKeys, mSize, key);
    }
    /**
     * Returns an index for which {@link #valueAt} would return the
     * specified key, or a negative number if no keys map to the
     * specified value.
     * Beware that this is a linear search, unlike lookups by key,
     * and that multiple keys can map to the same value and this will
     * find only one of them.
     */
    public int indexOfValue(int value) {
        for (int i = 0; i < mSize; i++)
            if (mValues[i] == value)
                return i;
        return -1;
    }
    /**
     * Removes all key-value mappings from this SparseIntArray.
     */
    public void clear() {
        mSize = 0;
    }
    /**
     * Puts a key/value pair into the array, optimizing for the case where
     * the key is greater than all existing keys in the array.
     */
    public void append(int key, int value) {
        if (mSize != 0 && key <= mKeys[mSize - 1]) {
            put(key, value);
            return;
        }
        mKeys = GrowingArrayUtils.append(mKeys, mSize, key);
        mValues = GrowingArrayUtils.append(mValues, mSize, value);
        mSize++;
    }
    /**
     * Provides a copy of keys.
     *
     * @hide
     * */
    public int[] copyKeys() {
        if (size() == 0) {
            return null;
        }
        return Arrays.copyOf(mKeys, size());
    }
    /**
     * {@inheritDoc}
     *
     * <p>This implementation composes a string by iterating over its mappings.
     */
    @Override
    public String toString() {
        if (size() <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(mSize * 28);
        buffer.append('{');
        for (int i=0; i<mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = keyAt(i);
            buffer.append(key);
            buffer.append('=');
            int value = valueAt(i);
            buffer.append(value);
        }
        buffer.append('}');
        return buffer.toString();
    }
}