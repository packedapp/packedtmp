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
package packed.internal.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.InternalExtensionException;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;
import packed.internal.util.types.TypeUtil;

/**
 * A utility class for finder method handles for constructors.
 * <p>
 * Is currently only used in connections with extensions. So we always throws {@link InternalExtensionException}.
 */
public final class ConstructorFinder {

    /**
     * Finds a constructor (method handle).
     * 
     * @param cp
     *            the type to find the constructor or
     * @param parameterTypes
     *            the parameter types the constructor must take
     * @return a method handle
     */
    public static <T extends Throwable> MethodHandle find(ClassProcessor cp, UncheckedThrowableFactory<T> tf, Class<?>... parameterTypes) throws T {
        Class<?> onType = cp.clazz();
        if (Modifier.isAbstract(onType.getModifiers())) {
            throw tf.newThrowable("'" + StringFormatter.format(onType) + "' cannot be an abstract class");
        } else if (TypeUtil.isInnerOrLocalClass(onType)) {
            throw tf.newThrowable("'" + StringFormatter.format(onType) + "' cannot be an inner or local class");
        }

        // First check that we have a constructor with specified parameters.
        // We could use Lookup.findSpecial, but we need to register the constructor if we are generating a native image.
        Constructor<?> constructor = null;
        try {
            constructor = onType.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            if (Extension.class.isAssignableFrom(onType)) {
                // Hack
                try {
                    constructor = onType.getDeclaredConstructor(ExtensionContext.class);
                } catch (NoSuchMethodException ignore) {} // Already on failure path
            }
            if (constructor == null) {
                if (parameterTypes.length == 0) {
                    throw tf.newThrowable("'" + StringFormatter.format(onType) + "' must have a no-argument constructor");
                } else {
                    throw tf.newThrowable("'" + StringFormatter.format(onType) + "' must have a constructor taking ["
                            + Stream.of(parameterTypes).map(p -> p.getName()).collect(Collectors.joining(",")) + "]");
                }
            }
        }

        return cp.unreflectConstructor(constructor, tf);
    }

    public static <T extends Throwable> MethodHandle findExactlyOne(ClassProcessor cp, UncheckedThrowableFactory<T> tf, MethodType... parameterTypes) throws T {
        throw new UnsupportedOperationException();
    }

    public static <T> T invoke(ClassProcessor cp, Class<?>... parameterTypes) {
        MethodHandle mh = ConstructorFinder.find(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY, parameterTypes);
        try {
            return (T) mh.invoke();
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }
}
