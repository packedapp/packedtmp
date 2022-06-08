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
package app.packed.operation.mirror;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.operation.OperationPack;

/**
 *
 */


public interface InjectableOperationHandle {

    // Ideen er lidt at hvis vi har forskel

    default void filter(Function<MethodHandle, MethodHandle> filter) {
        // MethodType of the returned function must be identical
    }
    
    InjectableOperationHandle specializeMirror(Supplier<? extends OperationMirror> supplier);

    int pack();

    int pack(Key<OperationPack> key);

    // Hvad goer vi med annoteringer paa Field/Update???
    // Putter paa baade Variable og ReturnType???? Det vil jeg mene
}
//
///**
//* Specifies an action that is invoked whenever the methodhandle has been build by the runtime.
//* 
//* @param action
//*            the action
//*/
//// onWired??? Hmm ikke ved wirelets
// void deliverTo(Consumer<MethodHandle> action);
