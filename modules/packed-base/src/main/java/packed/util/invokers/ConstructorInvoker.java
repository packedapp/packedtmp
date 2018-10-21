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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import app.packed.inject.Inject;
import app.packed.inject.InjectionException;
import packed.inject.JavaXInjectSupport;
import packed.util.ThrowableUtil;
import packed.util.descriptor.InternalConstructorDescriptor;

/**
 * A field that can be read and written
 */
public class ConstructorInvoker<T> extends ExecutableInvoker {
    private final InternalConstructorDescriptor<T> descriptor;

    private final Constructor<T> constructor;

    ConstructorInvoker(Constructor<T> constructor) {
        this.descriptor = InternalConstructorDescriptor.of(constructor);
        this.constructor = constructor;
    }

    @Override
    public InternalConstructorDescriptor<T> descriptor() {
        return descriptor;
    }

    /** {@inheritDoc} */
    @Override
    public Object instantiate(Object... parameters) {
        if (!Modifier.isPublic(constructor.getModifiers()) && !constructor.canAccess(null)) {
            constructor.trySetAccessible();
        }
        try {
            return constructor.newInstance(parameters);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e;
            if (cause instanceof InvocationTargetException) {
                cause = e.getCause();
            }
            ThrowableUtil.rethrowErrorOrRuntimeException(cause);
            throw new InjectionException("Constructor for " + format(constructor.getDeclaringClass()) + " failed", cause);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ConstructorInvoker<T> find(Class<T> clazz) {
        int maxParameters = 0;
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        ConstructorInvoker<T>[] constructors = new ConstructorInvoker[declaredConstructors.length];
        for (int i = 0; i < declaredConstructors.length; i++) {
            constructors[i] = (ConstructorInvoker<T>) ConstructorInvoker.of(declaredConstructors[i]);
            maxParameters = Math.max(maxParameters, constructors[i].descriptor().getParameterCount());
        }
        // See if we only have one constructor, in which case we keep it for later
        if (constructors.length == 1) {
            // one = constructors[0];
        }

        // Look for a single constructor annotated with @Inject
        ConstructorInvoker<T> injectable = null;
        for (ConstructorInvoker<T> cm : constructors) {
            if (JavaXInjectSupport.isInjectAnnotationPresent(cm.descriptor())) {
                if (injectable != null) {
                    throw new IllegalArgumentException("Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class "
                            + format(constructors[0].descriptor().getDeclaringClass()));
                }
                injectable = cm;
            }
        }

        // Look for a single constructor with the maximum number of parameters
        if (injectable == null) {
            for (ConstructorInvoker<T> cm : constructors) {
                if (cm.descriptor().getParameterCount() == maxParameters) {
                    if (injectable != null) {
                        throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                                + ". And multiple constructors having the maximum number of parameters (" + maxParameters + ") on class "
                                + format(constructors[0].descriptor().getDeclaringClass()));
                    }
                    injectable = cm;
                }
            }
        }
        if (injectable == null) {
            throw new IllegalArgumentException("Did not find anything");
        }
        return injectable;
    }

    public static <T> ConstructorInvoker<T> of(Constructor<T> constructor) {
        return new ConstructorInvoker<>(constructor);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalAccessException
     */
    @Override
    public MethodHandle unreflect(Lookup lookup) throws IllegalAccessException {
        requireNonNull(lookup, "lookup is null");
        return lookup.unreflectConstructor(constructor);
    }
}
