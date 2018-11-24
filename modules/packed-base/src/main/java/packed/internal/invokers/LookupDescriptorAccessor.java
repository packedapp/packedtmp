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
package packed.internal.invokers;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.factory.InternalFactoryExecutable;

final class LookupAccessFactory {

    // private static volatile X[] INFOS = new X[10];
    //
    // VarHandle[] fieldHandles;
    //
    // MethodHandle[] methodHandles;
    //
    // public static LookupAccessor get(MethodHandles.Lookup l) {
    // X x = INFOS[l.lookupModes()];
    //
    // return x.get(l.lookupClass());
    // }
    //
    // static class X extends ClassValue<LookupAccessor> {
    //
    // /** {@inheritDoc} */
    // @Override
    // protected LookupAccessor computeValue(Class<?> type) {
    // return null;
    // }
    // }
}

/**
 * 
 */
public final class LookupDescriptorAccessor {

    /** The default public accessor object. */
    public static final LookupDescriptorAccessor PUBLIC = new LookupDescriptorAccessor(MethodHandles.publicLookup());

    /** A cache of service class descriptors. */
    private final ClassValue<ComponentClassDescriptor<?>> componentClassCache = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected ComponentClassDescriptor<?> computeValue(Class<?> type) {
            return new ComponentClassDescriptor(type, lookup, null);
        }
    };

    /** The lookup object. */
    private final MethodHandles.Lookup lookup;

    /** A cache of service class descriptors. */
    private final ClassValue<ServiceClassDescriptor<?>> serviceClassCache = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected ServiceClassDescriptor<?> computeValue(Class<?> type) {
            return new ServiceClassDescriptor(type, lookup);
        }
    };

    private LookupDescriptorAccessor(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup);
    }

    @SuppressWarnings("unchecked")
    public <T> ComponentClassDescriptor<T> getComponentDescriptor(Class<T> implementation) {
        return (ComponentClassDescriptor<T>) componentClassCache.get(requireNonNull(implementation, "implementation is null"));
    }

    /**
     * Returns a service class descriptor for the specified implementation type.
     * 
     * @param <T>
     *            the type of the descriptor
     * @param implementation
     *            the implementation type to return a descriptor for
     * @return a service descriptor for the specified implementation type
     */
    @SuppressWarnings("unchecked")
    public <T> ServiceClassDescriptor<T> getServiceDescriptor(Class<T> implementation) {
        return (ServiceClassDescriptor<T>) serviceClassCache.get(requireNonNull(implementation, "implementation is null"));
    }

    public <T> InternalFactory<T> readable(InternalFactory<T> factory) {
        if (factory instanceof InternalFactoryExecutable) {
            InternalFactoryExecutable<T> e = (InternalFactoryExecutable<T>) factory;
            if (!e.hasMethodHandle()) {
                return e.withLookup(lookup);
            }
        }
        return factory;
    }
    // install as component class
    // install as component instance
    // install as mixin class
    // install as mixin instance
    // install as service class
    // install as service instance
    // newInstance()

    public static LookupDescriptorAccessor get(MethodHandles.Lookup lookup) {
        return new LookupDescriptorAccessor(lookup);// TODO cache this object,
    }

    // Naar vi laver factoriet ved vi ikke hvordan det skal bruges....
    // Det er heller ikke fordi vi kender noget til Lookup objektet...

    // Saa vi skal have en special struktur til det. Der er uafhaendig af hele invokerings cirkuset....

    // InternalFactory, kan heller ikke vide noget om det, med mindre vi laver en withLookup();

    // Den ved kun noget om Lookup, meeeen
}
