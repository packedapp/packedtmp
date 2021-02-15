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

import app.packed.base.Nullable;

/**
 * A special version of an invoker that takes an invocation template as argument.
 */
// Folk kan lave deres egen via et functional interface
public interface MethodAccessor1<T, R> {

    /**
     * Invokes the underlying method handle discards any result.
     * 
     * @throws Throwable
     */
    void call(@Nullable T argument) throws Throwable;
    
    /**
     * Invokes the underlying member.
     * 
     * @param argument
     *            the invocation template to use
     * @return stuff
     * @throws Throwable
     *             anything thrown by the underlying member propagates unchanged through the call
     */
    R invoke(@Nullable T argument) throws Throwable;

    @Nullable
    R invokeNullable(@Nullable T argument) throws Throwable;
}

///**
//* @param argument
//*            the template
//* @return the new invoker
//* 
//* @throws ClassCastException
//*             if the specified template is not an instance of T
//*/
//Invoker<?> toInvoker(T argument);

///**
//* @param <T>
//*            the template type
//* @param lookup
//*            a lookup of object for the template type
//* @param templateType
//*            the template type
//* @param target
//*            the method handle target
//* @return stuff
//*/
//static <R, T> Invoker1<R, T> of(MethodHandles.Lookup lookup, Class<T> templateType, MethodHandle target) {
//  throw new UnsupportedOperationException();
//}
