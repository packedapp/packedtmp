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
package packed.internal.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.reflect.UncheckedIllegalAccessException;
import packed.internal.component.ComponentModel;
import packed.internal.inject.factoryhandle.ExecutableFactoryHandle;
import packed.internal.inject.factoryhandle.FactoryHandle;

/**
 * This class exists because we have to ways to access the members of a component. One with a {@link Lookup} object, and
 * one without.
 */
public interface ComponentLookup {

    default MethodHandle acquireMethodHandle(Class<?> componentType, Constructor<?> constructor) {
        throw new UnsupportedOperationException();
    }

    MethodHandle acquireMethodHandle(Class<?> componentType, Method method);

    default VarHandle acquireVarHandle(Class<?> componentType, Field field) {
        throw new UnsupportedOperationException();
    }

    // componentModel should probably check valid types.... Basically
    ComponentModel componentModelOf(Class<?> componentType);

    Lookup lookup();

    default <T> FactoryHandle<T> readable(FactoryHandle<T> factory) {
        // TODO needs to cached

        // TODO add field...
        if (factory instanceof ExecutableFactoryHandle) {
            ExecutableFactoryHandle<T> e = (ExecutableFactoryHandle<T>) factory;
            if (!e.hasMethodHandle()) {
                return e.withLookup(lookup());
            }
        }
        return factory;
    }
    // Maybe method for acquire

    default MethodHandle toMethodHandle(FactoryHandle<?> factory) {
        return readable(factory).toMethodHandle();
    }

    /**
     * @param method
     *            the method to unreflect
     * @return a method handle for the unreflected method
     */
    default MethodHandle unreflect(Method method) {
        if (!ComponentLookup.class.getModule().canRead(method.getDeclaringClass().getModule())) {
            ComponentLookup.class.getModule().addReads(method.getDeclaringClass().getModule());
        }
        try {
            Lookup l = MethodHandles.privateLookupIn(method.getDeclaringClass(), lookup());
            return l.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("stuff", e);
        }
    }

    default MethodHandle unreflectGetter(Field field) {
        try {
            Lookup l = MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup());
            return l.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }
    }

    default MethodHandle unreflectSetter(Field field) {
        try {
            Lookup l = MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup());
            return l.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }
    }

    default VarHandle unreflectVarhandle(Field field) {
        try {
            Lookup l = MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup());
            return l.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a VarHandle", e);
        }
    }
}
