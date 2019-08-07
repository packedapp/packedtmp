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
package app.packed.hook;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.BiConsumer;

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

    Lookup lookup(); // TODO remove this method when possible...

    /**
     * Returns the annotated method.
     * 
     * @return the annotated method
     */
    MethodDescriptor method();

    /**
     * Creates a new {@link MethodHandle} to the underlying method.
     * <p>
     * 
     * @return a new MethodHandle for the underlying method
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created
     * @see Lookup#unreflect(java.lang.reflect.Method)
     */
    MethodHandle newMethodHandle();

    <S> void onMethodReady(Class<S> key, BiConsumer<S, Runnable> consumer);

    default PreparedLambda<Runnable> newRunnable() {
        throw new UnsupportedOperationException();
    }

    // checkNotOptional()
    // Er taenkt til en optional componenter.... f.eks. kan man ikke registere @Provide metoder, men gerne @Inject metoder
    // paa en optional component...

}
// Problemet med den er hvis vi faar AOP saa kan folk smide filtre ind foran.... Ogsaa paa statisk???
/// Vi kan vel bare wrappe MethodHandles....

// Problemet er her den callback vi skal smide tilbage paa
//// Vi kan require en Service...

// disableAOP()
// enableInjection()
// foobar

// InternalService -> Class -> T
// Injector ->
// LifecycleManager ->
