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
import packed.internal.container.extension.hook.OnHookMemberProcessor;
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

    /** If the extension has a corresponding extension node */
    private final ExtensionNodeModel node;

    final Class<? extends Extension> extensionType;

    final OnHookXModel onHoox;

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
        if (builder.node == null) {
            this.node = null;
        } else {
            this.node = builder.node.build(this);
        }
        this.onHoox = new OnHookXModel(builder.onHooks);
    }

    @Nullable
    public ExtensionNodeModel node() {
        return node;
    }

    public OnHookXModel model() {
        return onHoox;
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
        return (ExtensionModel<T>) CACHE.get(extensionType);
    }

    /** A builder for {@link ExtensionModel}. */
    static class Builder extends MemberProcessor {

        private ExtensionNodeModel.Builder node;

        private Class<? extends ExtensionNode<?>> nodeType;

        final OnHookMemberProcessor onHooks;

        private Builder(Class<? extends Extension> extensionType) {
            super(Extension.class, extensionType);
            onHooks = new OnHookMemberProcessor(Extension.class, extensionType, false);
            if (!Modifier.isFinal(extensionType.getModifiers())) {
                throw new IllegalArgumentException("The extension " + StringFormatter.format(extensionType) + " must be declared final");
            } else if (!Extension.class.isAssignableFrom(extensionType)) {
                throw new IllegalArgumentException(
                        "The specified type '" + StringFormatter.format(extensionType) + "' does not extend '" + StringFormatter.format(Extension.class) + "'");
            }
        }

        private ExtensionModel<?> build() {
            findMethods();
            if (nodeType != null) {
                node = new ExtensionNodeModel.Builder(this, nodeType);
            }
            return new ExtensionModel<>(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void processMethod(Method method) {
            onHooks.processMethod(method);
            if (method.getParameterCount() == 0 && method.getName().equals("onAdded")) {
                Class<?> nodeType = method.getReturnType();
                if (nodeType != ExtensionNode.class) {
                    this.nodeType = (Class<? extends ExtensionNode<?>>) nodeType;
                    // Vi vil gerne have den final... Saa brugere ikke returnere end anden type
                    if (!Modifier.isFinal(nodeType.getModifiers())) {
                        throw new InvalidDeclarationException(
                                "The extension node returned by onAdded(), must be declareda final, node type = " + StringFormatter.format(nodeType));
                    }
                }
            }
        }
    }
}
