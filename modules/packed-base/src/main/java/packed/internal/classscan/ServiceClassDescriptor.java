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
package packed.internal.classscan;

import java.lang.invoke.MethodHandles;

import app.packed.inject.Inject;
import app.packed.inject.Provides;
import packed.internal.annotations.AtInjectGroup;
import packed.internal.annotations.AtProvidesGroup;

/**
 * A service class descriptor contains information about injectable fields and methods.
 */
// Qualifier, default key...
public class ServiceClassDescriptor {

    /** A group of all members annotated with {@link Inject}. */
    public final AtInjectGroup inject;

    /** A group of all members annotated with {@link Provides}. */
    public final AtProvidesGroup provides;

    /** The simple name of the class as returned by {@link Class#getSimpleName()}. (Quite a slow operation) */
    public final String simpleName;

    /** The class this descriptor is created from. */
    public final Class<?> type;

    /**
     * Creates a new descriptor.
     * 
     * @param clazz
     *            the class to create a descriptor for
     * @param lookup
     *            the lookup object used to access fields and methods
     * @param scanner
     *            a member scanner
     */
    ServiceClassDescriptor(Class<?> clazz, MethodHandles.Lookup lookup, MemberScanner scanner) {
        // Do we need to store lookup??? I think yes. And then collect all annotated Fields in a list
        // Or do we validate everything up front?????? With Hooks and stufff...

        // We then run through each of them
        // Or maybe just throw it in an invoker?? The classes you register, are normally there for a reason.
        // Meaning the annotations are probablye

        this.type = clazz;
        this.simpleName = clazz.getSimpleName();
        this.provides = scanner.provides.build();
        this.inject = scanner.inject.build();
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
    public static ServiceClassDescriptor from(MethodHandles.Lookup lookup, Class<?> type) {
        return LookupDescriptorAccessor.get(lookup).getServiceDescriptor(type);
    }
}
