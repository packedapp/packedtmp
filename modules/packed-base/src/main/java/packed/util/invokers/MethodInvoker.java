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
package packed.util.invokers;

import static java.util.Objects.requireNonNull;
import static packed.util.Formatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.inject.InjectionException;
import packed.util.ThrowableUtil;
import packed.util.descriptor.InternalMethodDescriptor;

/**
 * A field that can be read and written
 */
public class MethodInvoker extends ExecutableInvoker {
    private final InternalMethodDescriptor descriptor;

    private final Method method;

    MethodInvoker(Method method) {
        this.descriptor = InternalMethodDescriptor.of(method);
        this.method = method;
    }

    @Override
    public InternalMethodDescriptor descriptor() {
        return descriptor;
    }

    /** {@inheritDoc} */
    @Override
    public Object instantiate(Object... parameters) {
        if (!Modifier.isPublic(method.getModifiers()) && !method.canAccess(null)) {
            method.trySetAccessible();
        }
        try {
            return method.invoke(null, parameters);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e;
            if (cause instanceof InvocationTargetException) {
                cause = e.getCause();
            }
            ThrowableUtil.rethrowErrorOrRuntimeException(cause);
            throw new InjectionException(method.getName() + "() method for " + format(method.getDeclaringClass()) + " failed", cause);
        }
    }

    public Object invoke(Object instance, Object... parameters) {
        if (!method.canAccess(instance)) {
            method.trySetAccessible();
        }

        return Invokeable.invoke(instance, method, parameters);
    }

    public Object invokeRaw(Object instance, Object... parameters) throws IllegalAccessException, InvocationTargetException {
        if (!Modifier.isPublic(method.getModifiers()) && !method.canAccess(instance)) {
            method.trySetAccessible();
        }
        return method.invoke(instance, parameters);
    }

    public static MethodInvoker of(Method method) {
        return new MethodInvoker(method);
    }

    @Override
    public MethodHandle unreflect(Lookup lookup) throws IllegalAccessException {
        requireNonNull(lookup, "lookup is null");
        return lookup.unreflect(method);
    }
}
