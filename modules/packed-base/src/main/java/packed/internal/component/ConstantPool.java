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
package packed.internal.component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.inject.ServiceLocator;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 * All strongly connected components relate to the same pod.
 */
// Passive System -> 1 NodeStore
// Active System -> 1 NodeStore per guest
// Long term, this might just be an Object[] array. But for now its a class, in case we need stuff that isn't stored in the array. 

// Is this a constant pool???
public final /* primitive*/ class ConstantPool {

    /** A method handle for calling {@link #getSingletonInstance(int)} at runtime. */
    static final MethodHandle MH_GET_SINGLETON_INSTANCE = LookupUtil.lookupVirtual(MethodHandles.lookup(), "getSingletonInstance", Object.class, int.class);

    public final Object[] table;

    public ConstantPool(int i) {
        table = new Object[i];
    }

    public Object getSingletonInstance(int index) {
//        Object value = store[index];
        // System.out.println("Reading index " + index + " value= " + value);
        // new Exception().printStackTrace();
        return table[index];
    }

    public void print() {
        System.out.println("--");
        for (int i = 0; i < table.length; i++) {
            System.out.println(i + " = " + table[i]);
        }

        System.out.println("--");
    }

    public boolean isSet(int index) {
        return table[index] != null;
    }

    // Don't know
    PackedApplicationRuntime container() {
        return (PackedApplicationRuntime) table[0];
    }

    ServiceLocator serviceRegistry(PackedComponent node) {
        return (ServiceLocator) table[node.modifiers().hasRuntime() ? 1 : 0];
    }

    public void store(int index, Object instance) {
        if (table[index] != null) {
            throw new IllegalStateException();
        }
        table[index] = instance;
        // new Exception().printStackTrace();
    }

    public static MethodHandle readSingletonAs(int index, Class<?> type) {
        MethodHandle mh = MethodHandleUtil.bind(MH_GET_SINGLETON_INSTANCE, 1, index);
        return MethodHandleUtil.castReturnType(mh, type);
    }
}
