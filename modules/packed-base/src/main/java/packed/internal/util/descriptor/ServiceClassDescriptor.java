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
package packed.internal.util.descriptor;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import app.packed.inject.Inject;
import packed.internal.util.descriptor.fields.FieldInvokerAtInject;
import packed.internal.util.descriptor.methods.MethodInvokerAtInject;

/**
 * A service class descriptor contains information about injectable fields and methods.
 */
public class ServiceClassDescriptor<T> {

    /** The class this descriptor is created from. */
    private final Class<T> clazz;

    /** All fields annotated with {@link Inject}. */
    private final Collection<FieldInvokerAtInject> injectableFields;

    /** All methods annotated with {@link Inject}. */
    private final Collection<MethodInvokerAtInject> injectableMethods;

    /** The simple name of the class as returned by {@link Class#getSimpleName()}. (Quite a slow operation) */
    private final String simpleName;

    protected ServiceClassDescriptor(Class<T> clazz, MethodHandles.Lookup lookup) {
        this.clazz = clazz;
        this.simpleName = clazz.getSimpleName();
        this.injectableFields = FieldInvokerAtInject.findInjectableFields(clazz, lookup);
        this.injectableMethods = MethodInvokerAtInject.findInjectableMethods(clazz, lookup);
    }

    public boolean hasInjectableFields() {
        return !injectableFields.isEmpty();
    }

    public boolean hasInjectableMethods() {
        return !injectableMethods.isEmpty();
    }

    /**
     * Returns the simple name of the class.
     *
     * @return the simpleName of the class
     * @see Class#getSimpleName()
     */
    public final String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns the type that is mirrored
     *
     * @return the type that is mirrored
     */
    public final Class<T> getType() {
        return clazz;
    }

    /**
     * Returns all injectable fields on this type.
     * 
     * @return all injectable fields on this type
     */
    public final Collection<FieldInvokerAtInject> injectableFields() {
        return injectableFields;
    }

    /**
     * Returns all injectable methods on this type.
     * 
     * @return all injectable methods on this type
     */
    public final Collection<MethodInvokerAtInject> injectableMethods() {
        return injectableMethods;
    }

    /**
     * Returns a service class descriptor for the specified lookup and type
     * 
     * @param <T>
     *            the type of element the service class descriptor holds
     * @param lookup
     *            the lookup
     * @param type
     *            the type
     * @return a service class descriptor for the specified lookup and type
     */
    public static <T> ServiceClassDescriptor<T> from(MethodHandles.Lookup lookup, Class<T> type) {
        return LookupDescriptorAccessor.get(lookup).getServiceDescriptor(type);
    }
}
