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
package app.packed.container.extension;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.reflect.MethodDescriptor;

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
     * Applies the specified operator to the underlying method.
     * 
     * @param <E>
     *            the type of result from applying the operator
     * @param operator
     *            the operator to apply
     * @return the result from applying the operator to the static method
     * @throws UnsupportedOperationException
     *             if the underlying method is not a static method
     * @throws UncheckedIllegalAccessException
     *             if access checking failed while applying the operator
     */
    <E> E applyStatic(MethodOperator<E> operator);

    /**
     * Returns a descriptor for the underlying method.
     * 
     * @return a descriptor for the underlying method
     */
    MethodDescriptor method();

    /**
     * Returns a {@link MethodHandle} for the underlying method.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying method is an instance method.
     * 
     * @return a MethodHandle to the underlying method
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
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
