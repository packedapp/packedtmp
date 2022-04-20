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
package app.packed.bean.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.inject.FactoryType;

/**
 *
 */
// noget omkring Instance (requiresInstance) Altsaa det er lidt 
// noget omkring wrapping mode

// FactoryInvoker??? Fraekt hvis faa dem bundet sammen

// Kan laves fra et Field eller Method
// og kan invokere en metoder/constructor, lase/skrive/update et field
public non-sealed interface InjectableOperationHandle extends OperationHandle {

    // Ideen er lidt at hvis vi har forskel

    default void filter(Function<MethodHandle, MethodHandle> filter) {
        // MethodType of the returned function must be identical
    }

    MethodType invocationType();

    int packIndex();

    // Used if we have for example seperate
    void packKey(Key<? extends OperationPack> key);

    // The path of the extension bean used when creating the operation
    NamespacePath packPath();

    // Hvad goer vi med annoteringer paa Field/Update???
    // Putter paa baade Variable og ReturnType???? Det vil jeg mene

    FactoryType type();
}
//
///**
//* Specifies an action that is invoked whenever the methodhandle has been build by the runtime.
//* 
//* @param action
//*            the action
//*/
//// onWired??? Hmm ikke ved wirelets
//void deliverTo(Consumer<MethodHandle> action);
