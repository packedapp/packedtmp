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

import app.packed.base.Nullable;

/**
 *
 */
public interface Invoker {

    /**
     * Invokes the underlying executable.
     * 
     * @return the result
     * @throws Throwable
     *             anything thrown by the underlying executable propagates unchanged through the method handle call
     */
    @Nullable
    Object invoke() throws Throwable;

    /**
     * Returns a new parameter-less method handle. The return type of the method handle will be the exact return type of the
     * underlying executable.
     * 
     * @return the method handle
     */
    MethodHandle toMethodHandle();
}
//<T> so we can specialize paa et tidspunkt... Eller faa SpecializedInvoker paa et senere tidspunkt
//Invoker extends SpecializedInvoker<Object>
//Maaske laver vi det til en inline klasse...
//Er jo saaden set en special version af InvocationTemplate der ikke tager nogle parameter.
interface SpecializedInvoker<R> {

    /**
     * Invokes the underlying executable.
     * 
     * @return the result
     * @throws Throwable
     *             anything thrown by the underlying executable propagates unchanged through the method handle call
     */
    @Nullable
    R invoke() throws Throwable;

    /**
     * Returns a new parameter-less method handle. The return type of the method handle will be the exact return type of the
     * underlying executable.
     * 
     * @return the method handle
     */
    MethodHandle toMethodHandle();
}
interface SpecializedTemplateInvoker<R> {
        
}