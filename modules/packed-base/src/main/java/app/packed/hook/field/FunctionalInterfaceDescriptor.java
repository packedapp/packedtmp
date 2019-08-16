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
package app.packed.hook.field;

import java.lang.invoke.MethodHandle;

/**
 *
 */
public final class FunctionalInterfaceDescriptor {

    /** A cache of functional interface descriptors. */
    static final ClassValue<FunctionalInterfaceDescriptor> DESCRIPTOR_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected FunctionalInterfaceDescriptor computeValue(Class<?> functionalInterface) {
            return new FunctionalInterfaceDescriptor(functionalInterface);
        }
    };

    private FunctionalInterfaceDescriptor(Class<?> clazz) {
        // Ahh.. for helvede...
        // Vi bliver noedt til at supportere gemme dem per extension....
        // eftersom en extension maaske har adgang til et hemmeligt interface...
        // Det er ivirkeligheden nok den der laver FieldOperatoren...

        // check interface

        // Maybe FieldOperator needs to take a MethodHandle.lookup object if none public SAM type...
    }

    public MethodHandle method() {
        throw new UnsupportedOperationException();
    }
}
