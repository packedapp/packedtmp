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
package app.packed.inject;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.util.InvalidDeclarationException;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.factory.InternalFactoryExecutable;
import packed.internal.util.TypeUtil;
import packed.internal.util.descriptor.InternalConstructorDescriptor;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/** This class is responsible for finding an injectable executable. */
class FactoryFindInjectable {

    static <T> InternalFactory<T> find(Class<T> implementation) {
        InternalExecutableDescriptor executable = findExecutable(implementation);
        return new InternalFactoryExecutable<>(TypeLiteral.of(implementation), executable, InternalDependency.fromExecutable(executable),
                executable.getParameterCount(), null);
    }

    static <T> InternalFactory<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        InternalExecutableDescriptor executable = findExecutable(implementation.getRawType());
        return new InternalFactoryExecutable<>(implementation, executable, InternalDependency.fromExecutable(executable), executable.getParameterCount(), null);
    }

    private static InternalExecutableDescriptor findExecutable(Class<?> type) {
        if (type.isArray()) {
            throw new IllegalArgumentException("The specified type (" + format(type) + ") is an array");
        } else if (type.isAnnotation()) {
            throw new IllegalArgumentException("The specified type (" + format(type) + ") is an annotation");
        }

        // Try to find a single static method annotated with @Inject
        Method method = null;
        for (Method m : type.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && JavaXInjectSupport.isInjectAnnotationPresent(m)) {
                if (method != null) {
                    throw new IllegalArgumentException("There are multiple static methods annotated with @Inject on " + format(type));
                }
                method = m;
            }
        }
        if (method != null) {
            // Det er jo i virkeligheden en Key vi laver her, burde havde det samme checkout..
            if (method.getReturnType() == void.class /* || returnType == Void.class */) {
                throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have a void return type."
                        + " (@Inject on static methods are used to indicate that the method is a factory for a specific type, not for injecting values");
            } else if (TypeUtil.isOptionalType(method.getReturnType())) {
                throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have an optional return type ("
                        + method.getReturnType().getSimpleName() + "). A valid instance needs to be provided by the method");
            }
            return InternalMethodDescriptor.of(method);
        }

        // Try to find a single static method annotated with @Inject
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return InternalConstructorDescriptor.of(constructors[0]);
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        int maxParameters = 0;
        for (Constructor<?> c : constructors) {
            maxParameters = Math.max(maxParameters, c.getParameterCount());
            if (JavaXInjectSupport.isInjectAnnotationPresent(c)) {
                if (constructor != null) {
                    throw new InvalidDeclarationException(
                            "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return InternalConstructorDescriptor.of(constructor);
        }

        // Try and find one constructor with maximum number of parameters.
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() == maxParameters) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple constructors having the maximum number of parameters (" + maxParameters + ") on class " + format(type));
                }
                constructor = c;
            }
        }
        return InternalConstructorDescriptor.of(constructor);
    }
}
