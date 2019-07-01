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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import app.packed.util.FieldDescriptor;

/**
 *
 */
// We could extend from an MethodHook
public interface AnnotatedFieldHook<T> {

    // TODO remove this method
    Lookup lookup();

    FieldDescriptor field();

    VarHandle newVarHandle();

    // <S> void onMethodReady(Class<S> key, BiConsumer<S, Runnable> consumer);

    // We throw access exception as a runtime exception, because there is no way the client (Extension).
    // Can do anything about access problems

    // Problemet med den er hvis vi faar AOP saa kan folk smide filtre ind foran.... Ogsaa paa statisk???

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
