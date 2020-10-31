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
package packed.internal.component.source;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.inject.Factory;
import packed.internal.classscan.OpenClass;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * This class exists because we have two ways to access the members of a component instance. One with a {@link Lookup}
 * object, and one using whatever power a module descriptor has given us.
 */
public abstract class RealmLookup {

    /** Calls package-private method Factory.toMethodHandle(Lookup). */
    private static final MethodHandle FACTORY_TO_METHOD_HANDLE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Factory.class, "toMethodHandle",
            MethodHandle.class, Lookup.class);

    /** A cache of component class descriptors. */
    private final ClassValue<SourceModel> components = new ClassValue<>() {

        @Override
        protected SourceModel computeValue(Class<?> type) {
            return SourceModel.newInstance(realm(), RealmLookup.this.newClassProcessor(type, true));
        }
    };

    final SourceModel modelOf(Class<?> componentType) {
        return components.get(componentType);
    }

    abstract Lookup lookup();

    abstract RealmModel realm();

    final OpenClass newClassProcessor(Class<?> clazz, boolean registerNatives) {
        return new OpenClass(lookup(), clazz, registerNatives);
    }

    public final MethodHandle toMethodHandle(Factory<?> factory) {
        try {
            return (MethodHandle) FACTORY_TO_METHOD_HANDLE.invoke(factory, lookup());
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
}
