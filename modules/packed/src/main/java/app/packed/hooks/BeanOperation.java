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
package app.packed.hooks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import app.packed.inject.FactoryType;

/**
 *
 */
// noget omkring Instance (requiresInstance) Altsaa det er lidt 
// noget omkring wrapping mode

// FactoryInvoker??? Fraekt hvis faa dem bundet sammen

// Kan laves fra et Field eller Method
// og kan invokere en metoder/constructor, lase/skrive/update et field
public abstract class BeanOperation {

    BeanOperation() {}

    /**
     * Specifies an action that is invoked whenever the methodhandle has been build by the runtime.
     * 
     * @param action
     *            the action
     */
    public void onReady(Consumer<MethodHandle> action) {}

    public final MethodType invocationType() {
        // For entity beans... Vi returnere vi
        // MethodType.methodType(void.class, BeanOperationInvocationContext.class, FooEntityBean.class);
        return MethodType.methodType(void.class, ContainerRealmContext.class);
    }

    FactoryType type() {
        // Hvad goer vi med annoteringer paa Field/Update???
        // Putter paa baade Variable og ReturnType???? Det vil jeg mene

        throw new UnsupportedOperationException();
    }
}
