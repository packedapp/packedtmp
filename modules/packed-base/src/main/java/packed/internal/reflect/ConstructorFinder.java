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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;
import packed.internal.util.TypeUtil;

/**
 *
 */
final class ConstructorFinder {

    /** The app.packed.base module. */
    private static final Module THIS_MODULE = ConstructorFinder.class.getModule();

    static MethodHandle extract(Class<?> onType, Class<?>... parameterTypes) {
        if (Modifier.isAbstract(onType.getModifiers())) {
            throw new IllegalArgumentException("The specified extension is an abstract class, type = " + StringFormatter.format(onType));
        } else if (TypeUtil.isInnerOrLocalClass(onType)) {
            throw new IllegalArgumentException("The specified type '" + StringFormatter.format(onType) + "' cannot be an inner or local class");
        }

        // First check that we have a constructor with specified parameters.
        // We could use Lookup.findSpecial, but we need to register the constructor if we are generating a native image.
        Constructor<?> constructor;
        try {
            constructor = onType.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The extension " + StringFormatter.format(onType) + " must have a no-argument constructor to be used");
        }

        // Check that the package the class is in, is open to app.packed.base
        if (!onType.getModule().isOpen(onType.getPackageName(), THIS_MODULE)) {
            String n = onType.getModule().getName();
            String m = ConstructorFinder.class.getModule().getName();
            String p = onType.getPackageName();
            throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(onType) + ", the extension's module '"
                    + onType.getModule().getName() + "' must be open to '" + m + "'. This can be done either via\n -> open module " + n + "\n -> opens " + p
                    + "\n -> opens " + p + " to " + m);
        }

        // Make sure we can read the module where the extension is located.
        if (!THIS_MODULE.canRead(onType.getModule())) {
            THIS_MODULE.addReads(onType.getModule());
        }

        Lookup lookup = MethodHandles.lookup();

        // Check to see, if we need to use a private lookup
        if (!Modifier.isPublic(onType.getModifiers()) || !Modifier.isPublic(constructor.getModifiers())) {
            try {
                lookup = MethodHandles.privateLookupIn(onType, lookup);
            } catch (IllegalAccessException e) {
                // This should never happen, because we have checked all preconditions
                // And we use our own lookup object which have Module access mode enabled.

                // Maybe something with unnamed modules...
                throw new UncheckedIllegalAccessException("This exception was not expected, please file a bug report with details", e);
            }
        }

        MethodHandle methodHandle;
        try {
            methodHandle = lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("This exception was not expected, please file a bug report with details", e);
        }
        NativeImage.registerConstructor(constructor);
        return methodHandle;
    }
}
