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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import app.packed.reflect.ConstructorExtractor;
import app.packed.util.InvalidDeclarationException;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

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
     * @param extensionType
     *            the extension type
     */
    private ExtensionModel(Class<? extends Extension> extensionType) {
        if (!Modifier.isFinal(extensionType.getModifiers())) {
            throw new IllegalArgumentException("Extension of type " + extensionType + " must be declared final");
        } else if (!Extension.class.isAssignableFrom(extensionType)) {
            throw new IllegalArgumentException(
                    "The specified type '" + StringFormatter.format(extensionType) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
        }
        this.extensionType = extensionType;
        this.constructor = ConstructorExtractor.extract(extensionType);
        Method m = null;
        try {
            m = extensionType.getDeclaredMethod("onAdded");
        } catch (NoSuchMethodException ignore) {}

        if (m != null) {
            Class<?> nodeType = m.getReturnType();
            if (nodeType != ExtensionNode.class) {
                if (!Modifier.isFinal(nodeType.getModifiers())) {
                    throw new InvalidDeclarationException(nodeType + " must be a final class");
                }
                System.out.println("YES");
            }
        }
    }

    /**
     * Creates a new extension of the specified type.
     * 
     * @param <T>
     *            the type of extension
     * @param extensionType
     *            the type of extension
     * @return a new instance of the extension
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> T newInstance(Class<T> extensionType) {
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
