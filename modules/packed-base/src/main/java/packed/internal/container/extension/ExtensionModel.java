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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.container.extension.hook.OnHookXModel;
import packed.internal.reflect.AbstractInstantiableModel;
import packed.internal.reflect.MemberProcessor;
import packed.internal.util.StringFormatter;

/**
 * A model of an Extension. Is mainly used for instantiating new extension instances.
 */
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension
public final class ExtensionModel<T extends Extension> extends AbstractInstantiableModel<T> {

    /** A cache of values. */
    private static final ClassValue<ExtensionModel<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionModel<? extends Extension> computeValue(Class<?> type) {
            return new Builder((Class<? extends Extension>) type).build();
        }
    };

    final ExtensionNodeModel node;

    private final Class<? extends Extension> extensionType;

    /**
     * Creates a new extension model.
     * 
     * @param builder
     *            the builder for this model
     */
    @SuppressWarnings("unchecked")
    private ExtensionModel(Builder builder) {
        super(builder.findNoParameterConstructor());
        this.extensionType = (Class<? extends Extension>) builder.actualType;
        this.node = builder.node;
    }

    @Nullable
    public ExtensionNodeModel node() {
        return node;
    }

    public OnHookXModel model() {
        return OnHookXModel.get(extensionType);
    }

    /**
     * Returns an extension model for the specified extension type.
     * 
     * @param <T>
     *            the type of extension to return a model for
     * @param extensionType
     *            the type of extension to return a model for
     * @return an extension model for the specified extension type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Extension> ExtensionModel<T> of(Class<T> extensionType) {
        // Time goes from around 1000 ns to 12 ns when we cache the method handle.
        // With LambdaMetafactory wrapped in a supplier we can get down to 6 ns
        return (ExtensionModel<T>) CACHE.get(extensionType);
    }

    /** A builder for {@link ExtensionModel}. */
    private static class Builder extends MemberProcessor {

        ExtensionNodeModel node;

        private Builder(Class<? extends Extension> extensionType) {
            super(Extension.class, extensionType);
            if (!Modifier.isFinal(extensionType.getModifiers())) {
                throw new IllegalArgumentException("The extension " + StringFormatter.format(extensionType) + " must be declared final");
            } else if (!Extension.class.isAssignableFrom(extensionType)) {
                throw new IllegalArgumentException(
                        "The specified type '" + StringFormatter.format(extensionType) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
            }
        }

        private ExtensionModel<?> build() {
            findMethods();
            return new ExtensionModel<>(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void processMethod(Method method) {
            if (method.getParameterCount() == 0 && method.getName().equals("onAdded")) {
                Class<?> nodeType = method.getReturnType();
                if (nodeType != ExtensionNode.class) {
                    if (!Modifier.isFinal(nodeType.getModifiers())) {
                        throw new InvalidDeclarationException(nodeType + " must be a final class");
                    }
                    node = new ExtensionNodeModel.Builder((Class<? extends ExtensionNode<?>>) nodeType).build();
                }
            }
        }
    }
}
