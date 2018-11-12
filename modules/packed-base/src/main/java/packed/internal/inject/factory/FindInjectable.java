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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.inject.Inject;
import app.packed.inject.TypeLiteral;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.TypeUtil;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalConstructorDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * This class is responsible for binding a single constructor or method from an implementation.
 */
public class FindInjectable {

    public static <T> InternalFactory<T> find(Class<T> implementation) {
        InternalExecutableDescriptor executable = findMethod(implementation);
        if (executable == null) {
            executable = findConstructor(implementation);// moc.constructors().findInjectable();
        }
        return new InternalFactoryExecutable<>(TypeLiteral.of(implementation), executable, InternalDependency.fromExecutable(executable),
                executable.getParameterCount(), null);
    }

    @SuppressWarnings("unchecked")
    public static <T> InternalFactory<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return (InternalFactory<T>) find(implementation.getRawType());
    }

    @SuppressWarnings("unchecked")
    static <T> InternalConstructorDescriptor<T> findConstructor(Class<T> clazz) {
        int maxParameters = 0;
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        InternalConstructorDescriptor<T>[] constructors = new InternalConstructorDescriptor[declaredConstructors.length];
        for (int i = 0; i < declaredConstructors.length; i++) {
            constructors[i] = (InternalConstructorDescriptor<T>) InternalConstructorDescriptor.of(declaredConstructors[i]);
            maxParameters = Math.max(maxParameters, constructors[i].getParameterCount());
        }
        // See if we only have one constructor, in which case we keep it for later
        if (constructors.length == 1) {
            // one = constructors[0];
        }

        // Look for a single constructor annotated with @Inject
        InternalConstructorDescriptor<T> injectable = null;
        for (InternalConstructorDescriptor<T> cm : constructors) {
            if (JavaXInjectSupport.isInjectAnnotationPresent(cm)) {
                if (injectable != null) {
                    throw new IllegalArgumentException("Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class "
                            + format(constructors[0].getDeclaringClass()));
                }
                injectable = cm;
            }
        }

        // Look for a single constructor with the maximum number of parameters
        if (injectable == null) {
            for (InternalConstructorDescriptor<T> cm : constructors) {
                if (cm.getParameterCount() == maxParameters) {
                    if (injectable != null) {
                        throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                                + ". And multiple constructors having the maximum number of parameters (" + maxParameters + ") on class "
                                + format(constructors[0].getDeclaringClass()));
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

    static InternalMethodDescriptor findMethod(Class<?> type) {
        if (type.isArray()) {
            throw new IllegalArgumentException("The specified type (" + format(type) + ") is an array");
        } else if (type.isAnnotation()) {
            throw new IllegalArgumentException("The specified type (" + format(type) + ") is an annotation");
        }
        Method method = null;
        for (Method m : type.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && JavaXInjectSupport.isInjectAnnotationPresent(m)) {
                if (method != null) {
                    throw new IllegalArgumentException("There are multiple static methods annotated with @Inject on " + format(type));
                }
                method = m;
            }
        }
        if (method == null) {
            return null;
        }

        if (method.getReturnType() == void.class /* || returnType == Void.class */) {
            throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have a void return type."
                    + " (@Inject on static methods are used to indicate that the method is a factory for a specific type, not for injecting values");
        } else if (TypeUtil.isOptionalType(method.getReturnType())) {
            throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have an optional return type ("
                    + method.getReturnType().getSimpleName() + "). A valid instance needs to be provided by the method");
        }
        return InternalMethodDescriptor.of(method);
    }
}
