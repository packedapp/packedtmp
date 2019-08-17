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
package app.packed.extension;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.MethodDescriptor;

/** A hook representing a method annotated with a specific type. */
public interface AnnotatedMethodHook<T extends Annotation> {

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

    <E> HookApplicator<E> applicator(MethodOperator<E> operator);

    /**
     * Applies the specified method operator to the underlying static method.
     * 
     * @param <E>
     *            the type of result from applying the operator
     * @param operator
     *            the operator to apply
     * @return the result from applying the operator to the static method
     * @throws UnsupportedOperationException
     *             if the underlying method is not a static method
     */
    <E> E applyOnStaticMethod(MethodOperator<E> operator);

    Lookup lookup(); // TODO remove this method when possible...

    /**
     * Returns the annotated method.
     * 
     * @return the annotated method
     */
    MethodDescriptor method();

    /**
     * Returns a {@link MethodHandle} to the underlying method.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying method is an instance method.
     * 
     * @return a MethodHandle to the underlying method
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created
     * @see Lookup#unreflect(java.lang.reflect.Method)
     */
    MethodHandle methodHandle();
}

// checkNotOptional()
// Er taenkt til en optional componenter.... f.eks. kan man ikke registere @Provide metoder, men gerne @Inject metoder
// paa en optional component...
// Problemet med den er hvis vi faar AOP saa kan folk smide filtre ind foran.... Ogsaa paa statisk???
/// Vi kan vel bare wrappe MethodHandles....

// disableAOP()
// enableInjection()
