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
package app.packed.sidecar;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;

/**
 * A special version of an invoker that takes an invocation template as argument.
 */

// Skal vi dropper templaten???
// Saa kan vi ogsaa bruge den for ting der tager en enkelt parameter??? Nej...

// Vi skal paa en eller anden maade supportere Invoker+TemplateInvoker som en getter.

// I version 2 kan vi have TemplateInvokerParametarizedReturn... men som udgangspunkt returnere vi Object...
// SpecializedTemplateInvoker() <- kan returnere et inline object...

//// Og dog... Altsaa det
//The gold standard must be JAX RS method
// Det er vel bare at injecte den i en FieldSidecar....

// Hvordan skal vi supportere inject af denne????
// Generalt supportere vi jo ikke generiske typer....
// Maaske skal vi have
// @Accessor(ignoreParentTypes = false)
public interface TemplateInvoker<T> {

    /**
     * Invokes the underlying member.
     * 
     * @param template
     *            the invocation template to use
     * @return stuff
     * @throws Throwable
     *             anything thrown by the underlying member propagates unchanged through the call
     */
    @Nullable
    Object invoke(T template) throws Throwable;

    // Taenkt paa at man kan tage Invoker paa nogle metoder...
    // F.eks, kan man maaske wrappe den i noget retry mechanism
    // Invoker.retryWith(RetryPolicy, Throwable on these Exceptions...)
    /**
     * @param template
     * @return the new invoker
     * 
     * @throws ClassCastException
     *             if the specified template is not an instance of T
     */
    Invoker toInvoker(T template);

    MethodHandle toMethodHandle();

    /**
     * @param <T>
     * @param lookup
     *            a lookup of object for the template type
     * @param templateType
     *            the template type
     * @param target
     *            the method handle target
     * @return stuff
     */
    static <T> TemplateInvoker<T> of(MethodHandles.Lookup lookup, Class<T> templateType, MethodHandle target) {
        throw new UnsupportedOperationException();
    }
}
// int invokeInt(T template) throws Throwable;
// The above wont work, because we could 1000s of inline types.
// Which would need their own....
// No we need specialized invokers. 