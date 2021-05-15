/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.invoke.constantpool;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 * All strongly connected components relate to the same pod.
 */
// Long term, this might just be an Object[] array. But for now its a class, in case we need stuff that isn't stored in the array. 

// ApplicationHeap???
public final /* primitive */ class ConstantPool {

    /** A method handle for calling {@link #read(int)} at runtime. */
    private static final MethodHandle MH_CONSTANT_POOL_READER = LookupUtil.lookupVirtual(MethodHandles.lookup(), "read", Object.class, int.class);

    private final Object[] objects;

    ConstantPool(int size) {
        objects = new Object[size];
    }

    public void print() {
        System.out.println("--");
        for (int i = 0; i < objects.length; i++) {
            System.out.println(i + " = " + objects[i]);
        }

        System.out.println("--");
    }

    public Object read(int index) {
//        Object value = store[index];
        // System.out.println("Reading index " + index + " value= " + value);
        // new Exception().printStackTrace();
        return objects[index];
    }

    public void storeObject(int index, Object instance) {
        if (objects[index] != null) {
            throw new IllegalStateException();
        }
        objects[index] = instance;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ConstantPool [size = " + objects.length + "]";
    }

    /**
     * @param index
     *            the index of the object to read
     * @param getAs
     *            the type of object to read
     * @return a method handle that will an object of the specified type for the specified index
     */
    public static MethodHandle indexedReader(int index, Class<?> getAs) {
        MethodHandle mh = MethodHandleUtil.bind(MH_CONSTANT_POOL_READER, 1, index);
        return MethodHandleUtil.castReturnType(mh, getAs);
    }
}
