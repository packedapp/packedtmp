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
package internal.app.packed.util.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.UndeclaredThrowableException;

import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
abstract class Handlers {

    static MethodHandle constructor(MethodHandles.Lookup caller, Class<?> inClass, Class<?>... parameterTypes) {
        return LookupUtil.findConstructor(caller, inClass, parameterTypes);
    }
    static VarHandle field(MethodHandles.Lookup lookup, Class<?> recv, String name, Class<?> type) {
        return LookupUtil.findVarHandle(lookup, recv, name, type);
    }

    static MethodHandle method(MethodHandles.Lookup lookup, Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        return LookupUtil.findVirtual(lookup, refc, name, returnType, parameterTypes);
    }

    static UndeclaredThrowableException throwIt(Throwable throwable) {
        return ThrowableUtil.orUndeclared(throwable);
    }
}
