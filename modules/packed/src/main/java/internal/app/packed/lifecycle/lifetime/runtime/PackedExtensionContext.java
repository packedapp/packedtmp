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
package internal.app.packed.lifecycle.lifetime.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import app.packed.context.ContextTemplate;
import app.packed.context.ContextualServiceProvider;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionContext;
import app.packed.extension.InternalExtensionException;
import internal.app.packed.ValueBased;
import internal.app.packed.util.LookupUtil;

/**
 * All strongly connected components relate to the same pod.
 */
@ContextualServiceProvider(extension = BaseExtension.class)
@ValueBased
public final class PackedExtensionContext implements ExtensionContext {

    /** A context template for {@link ExtensionContext}. */
    public static final ContextTemplate CONTEXT_TEMPLATE = ContextTemplate.of(ExtensionContext.class).withImplementation(PackedExtensionContext.class);

    /** A method handle for calling {@link #read(int)} at runtime. */
    public static final MethodHandle MH_CONSTANT_POOL_READER;

    static {
        MethodHandle m = LookupUtil.findVirtual(MethodHandles.lookup(), "read", Object.class, int.class);
        MethodType mt = m.type().changeParameterType(0, ExtensionContext.class);
        MH_CONSTANT_POOL_READER = m.asType(mt);
    }

    public static final ExtensionContext EMPTY = new PackedExtensionContext(0);

    final Object[] objects;

    private PackedExtensionContext(int size) {
        objects = new Object[size];
    }

    public static ExtensionContext create(int size) {
        if (size == 0) {
            return EMPTY;
        } else {
            return new PackedExtensionContext(size);
        }
    }

    public void print() {
        System.out.println("--");
        for (int i = 0; i < objects.length; i++) {
            System.out.println(i + " = " + objects[i]);
        }

        System.out.println("--");
    }

    public Object read(int index) {
        Object value = objects[index];
        if (value == null) {
            throw new InternalExtensionException("Bean with index " + index + " has not been initialized");
        }
        return value;
        // System.out.println("Reading index " + index + " value= " + value);
        // new Exception().printStackTrace();
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
}
