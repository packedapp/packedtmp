/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.lifecycle.runtime;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.extension.BaseExtension;
import app.packed.extension.InternalExtensionException;
import internal.app.packed.ValueBased;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.lifecycle.lifetime.LifetimeStore;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;

/**
 * All strongly connected components relate to the same pod.
 */
@AutoService(introspector = PackedExtensionContextBeanIntrospector.class)
@ValueBased
public final class PackedExtensionContext implements ExtensionContext {

    public static final ExtensionContext EMPTY = new PackedExtensionContext(null, 0);

    final Object[] objects;

    final LifetimeStore store;
    private PackedExtensionContext(LifetimeStore store, int size) {
        objects = new Object[size];
        this.store=store;

    }

    public static ExtensionContext create(LifetimeStore store, int size) {
        if (size == 0) {
            return EMPTY;
        } else {
            return new PackedExtensionContext(store, size);
        }
    }

    public void print() {
        IO.println("--");
        for (int i = 0; i < objects.length; i++) {
            IO.println(i + " = " + objects[i]);
        }

        IO.println("--");
    }

    public Object read(int index) {
        Object value = objects[index];
        if (value == null) {
            System.out.println(store.entries.get(index));
            throw new InternalExtensionException("Bean with index " + index + " has not been initialized");
        }
        return value;
        // IO.println("Reading index " + index + " value= " + value);
        // new Exception().printStackTrace();
    }

    public void storeObject(LifetimeStoreIndex index, Object instance) {
        int ind = index.index;
        if (objects[ind] != null) {
            throw new IllegalStateException();
        }
        objects[ind] = instance;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ConstantPool [size = " + objects.length + "]";
    }
}

final class PackedExtensionContextBeanIntrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onAutoService(Key<?> key, OnAutoService service) {
        if (key.rawType() == ExtensionContext.class) {
            if (beanOwner().isUserland()) {
                service.binder().failWith(ExtensionContext.class.getSimpleName() + " can only be injected into bean that owned by an extension");
            }
            service.binder().bindContext(ExtensionContext.class);
        } else {
            super.onAutoService(key, service);
        }
    }
}
