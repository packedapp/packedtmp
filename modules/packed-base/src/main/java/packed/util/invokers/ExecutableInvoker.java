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

import static packed.util.Formatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import packed.inject.JavaXInjectSupport;
import packed.util.descriptor.AbstractExecutableDescriptor;

/**
 *
 */
public abstract class ExecutableInvoker {

    public abstract MethodHandle unreflect(MethodHandles.Lookup lookup) throws IllegalAccessException;

    public abstract AbstractExecutableDescriptor descriptor();

    /**
     * Invokes the constructor or static method.
     *
     * @param parameters
     *            the parameters to the method or constructor
     * @return the object that was created or returned
     */
    public abstract Object instantiate(Object... parameters);

    public static ExecutableInvoker getDefaultFactoryFindStaticMethod(Class<?> type) {
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
        } else if (JavaXInjectSupport.isOptionalType(method.getReturnType())) {
            throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have an optional return type ("
                    + method.getReturnType().getSimpleName() + "). A valid instance needs to be provided by the method");
        }
        return MethodInvoker.of(method);
    }
    // /**
    // * @param method
    // * @return
    // */
    // public static ExecutableInvoker of(Method method) {
    // requireNonNull(executable, "executable is null");
    // return executable instanceof Method ? InternalMethodDescriptor.of((Method) executable) :
    // InternalConstructorDescriptor.of((Constructor<?>) executable);
    //
    // }
}
