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
package app.packed.container;

import java.lang.invoke.MethodHandle;
import java.util.function.BiConsumer;

import app.packed.util.MethodDescriptor;

/**
 *
 */
// We could extend from an MethodHook
public interface AnnotatedMethodHook<T> {

    MethodDescriptor method();

    // We throw access exception as a runtime exception, because there is no way the client (Extension).
    // Can do anything about access problems

    // Problemet med den er hvis vi faar AOP saa kan folk smide filtre ind foran.... Ogsaa paa statisk???
    MethodHandle create();

    <S> void onMethodReady(Class<S> key, BiConsumer<S, Runnable> consumer);

    // Problemet er her den callback vi skal smide tilbage paa
    //// Vi kan require en Service...

    // disableAOP()
    // enableInjection()
    // foobar

    // InternalService -> Class -> T
    // Injector ->
    // LifecycleManager ->

    // Mest taenkt for at give andre adgang til det.... Vi kan jo strengt taget.
    // Bare have nogle properties
}
