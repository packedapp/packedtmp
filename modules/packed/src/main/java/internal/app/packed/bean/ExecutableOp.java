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
package internal.app.packed.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.operation.OperationType;

/** A factory that wraps a method or constructor. */
public final class ExecutableOp<T>  {

    /** A cache of factories used by {@link #factoryOf(Class)}. */
    public static final ClassValue<ExecutableOp<?>> DEFAULT_FACTORY = new ClassValue<>() {

        /** {@inheritDoc} */
        protected ExecutableOp<?> computeValue(Class<?> implementation) {
            Constructor<?> executable = ConstructorFinder.getConstructor(implementation, true, e -> new IllegalArgumentException(e));
            return new ExecutableOp<>(executable);
        }
    };

    /** A factory with an executable as a target. */
    public final Constructor<?> executable;

    public final OperationType operationType;

    public ExecutableOp(Constructor<?> constructor) {
        this.operationType = OperationType.ofExecutable(constructor);
        this.executable = constructor;
    }

    /** {@inheritDoc} */
    public MethodHandle toMethodHandle(Lookup lookup) {
        MethodHandle methodHandle;
        try {
            if (!Modifier.isPublic(executable.getModifiers())) {
                lookup = MethodHandles.lookup();
                lookup = MethodHandles.privateLookupIn(executable.getDeclaringClass(), lookup);
            }
            methodHandle = lookup.unreflectConstructor(executable);

        } catch (IllegalAccessException e) {
            String name = executable instanceof Constructor ? "constructor" : "method";
            throw new InaccessibleBeanMemberException("No access to the " + name + " " + executable + " with the specified lookup object", e);
        }

        MethodHandle mh = methodHandle;
        if (executable.isVarArgs()) {
            mh = mh.asFixedArity();
        }
        return mh;
    }

    @Override
    public String toString() {
        return executable.toString();
    }
}