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
import static packed.util.Formatter.formatShort;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import app.packed.inject.InjectionException;
import packed.util.ThrowableUtil;

/** A temporary class, that allows execution of methods */
public class Invokeable<V> implements Callable<V>, Runnable {

    /** The method that is being invoked. */
    private final Method method;

    /** The parameters to the method. */
    private final Object[] parameters;

    /** The instance that the method is invoked upon */
    private final Object instance;

    /**
     * @param method
     * @param parameters
     */
    public Invokeable(Object instance, Method method, Object[] parameters) {
        this.instance = requireNonNull(instance);
        this.method = requireNonNull(method);
        this.parameters = requireNonNull(parameters);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        // Be carefull when changing this. For example, DaemonAnnotatedMethodHandler dependss on the behavior
        return formatShort(method);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            method.invoke(instance, parameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // We try to unpack the exception to compact the stack trace
            Throwable cause = e.getCause();
            ThrowableUtil.rethrowErrorOrRuntimeException(cause);
            throw new RuntimeException("Failed to properly invoke method", cause);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public V call() throws Exception {
        try {
            return (V) method.invoke(instance, parameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // We try to unpack the exception to compact the stack trace
            Throwable cause = e.getCause();
            ThrowableUtil.rethrowErrorOrException(cause);
            throw new RuntimeException("Failed to properly invoke method", cause);
        }
    }

    /**
     * @param executable
     *            the constructor or static method used to instantiate the component
     * @param parameters
     *            the parameters to the method or constructor
     * @return the object that was created or returned
     */
    public static Object invoke(Object value, Executable executable, Object... parameters) {
        Throwable cause;
        Method method = (Method) executable;
        try {
            return method.invoke(value, parameters);
        } catch (ReflectiveOperationException e) {
            cause = e;
        }

        // method or constructor invocation failed
        if (cause instanceof InvocationTargetException) {
            cause = cause.getCause();
        }
        ThrowableUtil.rethrowErrorOrRuntimeException(cause);
        String s = executable instanceof Constructor ? "Constructor" : "Method";
        s = s + " for " + format(executable.getDeclaringClass()) + " failed";
        throw new InjectionException(s, cause);
    }
}
