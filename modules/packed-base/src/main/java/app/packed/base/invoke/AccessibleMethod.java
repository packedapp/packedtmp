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
package app.packed.base.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.base.reflect.MethodDescriptor;

/**
 * A wrapper for a {@link Method} and a {@link Lookup} object.
 * 
 * @apiNote In the future, if the Java language permits, {@link AccessibleMethod} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface AccessibleMethod {

    /**
     * Returns a descriptor of the underlying method.
     * 
     * @return a descriptor of the underlying method
     */
    MethodDescriptor descriptor();

    /**
     * Returns a {@link MethodHandle} for the underlying method.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying method is an instance method.
     * 
     * @return a MethodHandle for the underlying method
     * @throws IllegalStateException
     *             if invoked outside of the intended scope.
     * @see Lookup#unreflect(java.lang.reflect.Method)
     */
    MethodHandle newMethodHandle();
}
//An OpenMethod often has a simple lifecycle
//isOpen() / isClosed ?

// Ideen er at man f.eks. ikke kan gemme den her og lave en MethodHandle efter Graal image build er koert.
//boolean isClosed(); // isValid, isActive

///**
// * Creates a new {@link Method} representing the underlying method.
// * <p>
// * The returned method does not have its {@link Method#setAccessible(boolean)} bit set.
// * 
// * @return a new method
// * @apiNote There are currently no support for making a method accessible using the {@link Lookup} API. Which is why we
// *          do not support creating methods that are {@link Method#setAccessible(boolean) accessible}.
// */
//Method newMethod();
///**
//* @param tag
//*            a non-native int less then 65536 (keep the rest of the bits for something else..
//* @return this method
//*/
//OpenMethod tagWith(int tag, Class<?>... sidecarTypes);

// Den her er jo ikke rigtig en constant pga #methodHandle
// Hvis vi har behov for en ConstructorSource paa et tidspunkt kan vi jo bare lave det...
// OpenMethod? Taenker OpenClass er nok noget vi ender med....

// Kan tage MethodDescriptor Method MethodHandle...
// Strengt taget kunne vi jo bare tage f.eks. Supplier<MethodHandle>.... istedet for denne klasse
// Men VarSource kraever jo en specific klasse... Saa vi kan lave de MethodHandle getters...

// Har vi integration med .inject???? F.eks. List<Dependency> som parametere...
// Her skal vi ogsaa resolver evt. dependencies fra Annotat

// MethodAccessorFactory

// Fra Lookup javadoc
// * A <em>lookup object</em> is a factory for creating method handles,
// * when the creation requires access checking.
// * Method handles do not perform
// * access checks when they are called, but rather when they are created.
// * Therefore, method handle access
// * restrictions must be enforced when a method handle is created.
// * The caller class against which those restrictions are enforced
// * is known as the {@linkplain #lookupClass() lookup class}.
