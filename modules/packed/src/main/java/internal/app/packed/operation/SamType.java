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
package internal.app.packed.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.operation.OperationType;

/**
 *
 */
public record SamType(Class<?> functionInterface, Method saMethod, MethodHandle methodHandle, OperationType type) {

    public static SamType of(Class<?> functionInterface) {
        if (!functionInterface.isInterface()) {
            throw new IllegalArgumentException(functionInterface + " is not an interface");
        }
        Method samMethod = null;
        for (Method m : functionInterface.getMethods()) {
            if (!m.isDefault() && !Modifier.isStatic(m.getModifiers())) {
                if (samMethod != null) {
                    throw new IllegalArgumentException(
                            functionInterface + " is not a proper functional interface, as there are multiple non-default and non-static methods, [" + samMethod
                                    + ", " + m + "]");
                }
                samMethod = m;
            }
        }
        if (samMethod == null) {
            throw new IllegalArgumentException(functionInterface + " is not a proper functional interface, because there are no non-default instance methods");
        }

        // For now we require that the Single Abstract Method must be on a public available class
        MethodHandle mh;
        try {
            mh = MethodHandles.publicLookup().unreflect(samMethod);
        } catch (IllegalAccessException e) {
            throw new Error(samMethod + " must be accessible via MethodHandles.publicLookup()", e);
        }

        OperationType ot = OperationType.fromExecutable(samMethod);
        return new SamType(functionInterface, samMethod, mh, ot);
    }
}
