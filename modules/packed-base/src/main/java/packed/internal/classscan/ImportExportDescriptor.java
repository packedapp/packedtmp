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

import app.packed.inject.Provide;
import packed.internal.annotations.AtProvidesGroup;

/**
 *
 */
public class ImportExportDescriptor {

    /** A group of all members annotated with {@link Provide}. */
    public final AtProvidesGroup provides;

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
    ImportExportDescriptor(Class<?> clazz, MethodHandles.Lookup lookup, MemberScanner scanner) {
        // Do we need to store lookup??? I think yes. And then collect all annotated Fields in a list
        // Or do we validate everything up front?????? With Hooks and stufff...

        // We then run through each of them
        // Or maybe just throw it in an invoker?? The classes you register, are normally there for a reason.
        // Meaning the annotations are probablye

        this.type = clazz;
        this.provides = scanner.provides.get();
    }

    /**
     * Returns a service class descriptor for the specified lookup and type
     * 
     * @param lookup
     *            the lookup
     * @param type
     *            the type
     * @return a service class descriptor for the specified lookup and type
     */
    public static ImportExportDescriptor from(MethodHandles.Lookup lookup, Class<?> type) {
        return DescriptorFactory.get(lookup).getImportExportStage(type);
    }
}
