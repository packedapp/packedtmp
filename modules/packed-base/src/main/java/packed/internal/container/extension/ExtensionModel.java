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
package packed.internal.container.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.container.extension.Extension;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.NativeImage;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TypeUtil;

/**
 * A cache of {@link Extension} implementations. Is mainly used for instantiating new instances of extensions.
 */
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension
final class ExtensionModel<T> {

    /** A cache of values. */
    private static final ClassValue<ExtensionModel<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected ExtensionModel<?> computeValue(Class<?> type) {
            return new ExtensionModel(type);
        }
    };

    /** The method handle used to create a new instance of the extension. */
    private final MethodHandle constructor;

    /** The type of extension. */
    private final Class<? extends Extension> extensionType;

    /**
     * Creates a new extension model.
     * 
     * @param type
     *            the extension type
     */
    private ExtensionModel(Class<? extends Extension> type) {
        this.extensionType = requireNonNull(type);

        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("The specified extension is an abstract class, type = " + StringFormatter.format(type));
        } else if (!Extension.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "The specified type '" + StringFormatter.format(type) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
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
        if (!type.getModule().isOpen(type.getPackageName(), ExtensionModel.class.getModule())) {
            String n = type.getModule().getName();
            String m = ExtensionModel.class.getModule().getName();
            String p = type.getPackageName();
            throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(type) + ", the extension's module '"
                    + type.getModule().getName() + "' must be open to '" + m + "'. This can be done either via\n -> open module " + n + "\n -> opens " + p
                    + "\n -> opens " + p + " to " + m);
        }

        // Make sure we can read the module where the extension is located.
        if (!getClass().getModule().canRead(type.getModule())) {
            getClass().getModule().addReads(type.getModule());
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

        try {
            this.constructor = lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("This exception was not expected, please file a bug report with details", e);
        }

        NativeImage.registerConstructor(constructor);
    }

    /**
     * Creates a new extension of the specified type.
     * 
     * @param <T>
     *            the type of extension
     * @param extensionType
     *            the type of extension
     * @param pcc
     *            the configuration of the container the extension is being added to
     * @return a new instance of the extension
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> T newInstance(Class<T> extensionType, PackedContainerConfiguration pcc) {
        // Time goes from around 1000 ns to 12 ns when we cache the method handle.
        // With LambdaMetafactory wrapped in a supplier we can get down to 6 ns
        ExtensionModel<T> model = (ExtensionModel<T>) CACHE.get(extensionType);
        try {
            return (T) model.constructor.invoke();
        } catch (Throwable t) {
            ThrowableUtil.rethrowErrorOrRuntimeException(t);
            throw new UndeclaredThrowableException(t, "Could not instantiate extension '" + StringFormatter.format(model.extensionType) + "'");
        }
    }
}
