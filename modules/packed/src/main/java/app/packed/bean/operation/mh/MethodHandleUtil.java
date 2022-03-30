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
package app.packed.bean.operation.mh;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 *
 */
public class MethodHandleUtil {

    // Also
    // https://github.com/jbosboom/bytecodelib/blob/master/src/edu/mit/streamjit/util/bytecode/methodhandles/Combinators.java
    
    public static MethodHandle aroundInterceptorWithTmp(MethodHandle methodHandle, MethodHandle before, MethodHandle after) {
        MethodHandle mh = beforeInterceptor(methodHandle, before);
        return afterInterceptor(mh, after);
    }

    public static MethodHandle aroundInterceptor(MethodHandle methodHandle, MethodHandle before, MethodHandle after) {
        MethodHandle mh = beforeInterceptor(methodHandle, before);
        return afterInterceptor(mh, after);
    }

    public static MethodHandle beforeInterceptor(MethodHandle methodHandle, MethodHandle before) {
        return MethodHandles.foldArguments(methodHandle, before);
    }

    public static MethodHandle afterInterceptor(MethodHandle methodHandle, MethodHandle after) {
        Class<?> rType = methodHandle.type().returnType();
        MethodHandle mh = invokeAndReturnArg(after, rType);
        MethodHandle xx = MethodHandles.dropArguments(mh, mh.type().parameterCount(), rType);
        MethodHandle m1 = MethodHandles.foldArguments(xx, methodHandle);
        return m1;
    }

    public static MethodHandle invokeAndReturnArg(MethodHandle combiner, Class<?> c) {
        MethodHandle tmp = MethodHandles.dropArguments(combiner, 0, c);
        MethodHandle dropArguments = MethodHandles.dropArguments(MethodHandles.identity(c), 1, combiner.type().parameterArray());
        return MethodHandles.foldArguments(dropArguments, tmp);
    }

    // Before Alone
    //// Arguments are resolved to services
    //// before must have void return type

    // After alone
    //// Arguments are resolved to services

    // after(boolean useResult) -> Result is than first arg, remaining are resolved as services

    // around(before, after, useResult)
    // if before returns value, must be after useResult
    // remaining classes from before and after are resolved as services

}
