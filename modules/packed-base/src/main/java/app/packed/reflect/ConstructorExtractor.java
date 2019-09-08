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
package app.packed.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;
import packed.internal.util.TypeUtil;

/**
 *
 */
public final class ConstructorExtractor {
    private static final Module THIS_MODULE = ConstructorExtractor.class.getModule();

    public static MethodHandle extract(Class<?> type) {
        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("The specified extension is an abstract class, type = " + StringFormatter.format(type));
        } else if (TypeUtil.isInnerOrLocalClass(type)) {
            throw new IllegalArgumentException("The specified type '" + StringFormatter.format(type) + "' cannot be an inner or local class");
        }

        Constructor<?> constructor;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The extension " + StringFormatter.format(type) + " must have a no-argument constructor to be used");
        }

        // Check that the package the extension is located in, is open to app.packed.base
        if (!type.getModule().isOpen(type.getPackageName(), THIS_MODULE)) {
            String n = type.getModule().getName();
            String m = ConstructorExtractor.class.getModule().getName();
            String p = type.getPackageName();
            throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(type) + ", the extension's module '"
                    + type.getModule().getName() + "' must be open to '" + m + "'. This can be done either via\n -> open module " + n + "\n -> opens " + p
                    + "\n -> opens " + p + " to " + m);
        }

        // Make sure we can read the module where the extension is located.
        if (!THIS_MODULE.canRead(type.getModule())) {
            THIS_MODULE.addReads(type.getModule());
        }

        Lookup lookup = MethodHandles.lookup();

        // See if need to use a private lookup
        if (!Modifier.isPublic(type.getModifiers()) || !Modifier.isPublic(constructor.getModifiers())) {
            try {
                lookup = MethodHandles.privateLookupIn(type, lookup);
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
