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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import packed.internal.container.extension.hook.PackedMethodOperator;

/**
 *
 */
// Foerst og fremmest typen... //maaske om den skal vaere async??
// Styring af exceptions???? Version 2
/// Men ja, mener helt at det er noget man goer i operatoren...
/// Alt hvad der ikke skal tilpasses en runtime...
public interface MethodOperator<T> {

    // Hmm vil vi give et lookup object til et interface?
    T applyStatic(Lookup lookup, Method method);

    T invoke(MethodHandle mh);

    static <T> MethodOperator<Object> invokeOnce() {
        return new PackedMethodOperator.InvokeOnce<>();
    }

    static MethodOperator<Runnable> runnable() {
        return new PackedMethodOperator.RunnableInternalMethodOperation();
    }
}
