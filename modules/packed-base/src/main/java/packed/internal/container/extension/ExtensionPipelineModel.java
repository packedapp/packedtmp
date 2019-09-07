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

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionWireletPipeline;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;
import packed.internal.util.TypeUtil;

/**
 *
 */
public class ExtensionPipelineModel {
    /** A cache of values. */
    static final ClassValue<ExtensionPipelineModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected ExtensionPipelineModel computeValue(Class<?> type) {
            return new ExtensionPipelineModel((Class<? extends ExtensionWireletPipeline<?>>) type);
        }
    };

    /** The method handle used to create a new instance of the extension. */
    final MethodHandle constructor;

    /** The type of extension. */
    final Class<? extends ExtensionWireletPipeline<?>> extensionPipelineType;

    final Class<? extends Extension> extensionType;

    /**
     * Creates a new extension model.
     * 
     * @param type
     *            the extension type
     */
    @SuppressWarnings("unchecked")
    private ExtensionPipelineModel(Class<? extends ExtensionWireletPipeline<?>> type) {
        this.extensionPipelineType = requireNonNull(type);

        // We wrapper det her i en constructor finder...
        // Indtil videre har vi Extension, ExtensionPipeline, GroupBuilder

        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("The specified extension is an abstract class, type = " + StringFormatter.format(type));
        } else if (!Extension.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    "The specified type '" + StringFormatter.format(type) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
        } else if (TypeUtil.isInnerOrLocalClass(type)) {
            throw new IllegalArgumentException("The specified type '" + StringFormatter.format(type) + "' cannot be an inner or local class");
        }

        // We have a small hack where we allow PackedContainerConfiguration to be injected.
        // This only works for modules where PackedContainerConfiguration is exported to

        Constructor<?> constructor;
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length != 1) {
            throw new IllegalStateException();
        }
        constructor = constructors[0];
        // this.needsPackedContainerConfiguration = needsPackedContainerConfiguration;

        extensionType = (Class<? extends Extension>) constructor.getParameterTypes()[0];

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

}
