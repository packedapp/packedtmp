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

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import packed.internal.container.extension.hook.OnHookMemberProcessor;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 *
 */
public final class ExtensionNodeModel {

    /** An extractor to find the extension the node is build upon. */
    private static final TypeVariableExtractor EXTENSION_NODE_TV_EXTRACTOR = TypeVariableExtractor.of(ExtensionNode.class);

    /** A cache of values. */
    private static final ClassValue<ExtensionNodeModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionNodeModel computeValue(Class<?> type) {
            Class<? extends Extension> extensionType = (Class<? extends Extension>) EXTENSION_NODE_TV_EXTRACTOR.extract(type);
            return ExtensionModel.of(extensionType).node();
        }
    };

    /**
     * Creates a new extension model.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionNodeModel(ExtensionModel<?> extensionModel, Builder builder) {}

    /**
     * Returns an extension node model for the specified extension node type.
     * 
     * @param nodeType
     *            the type of extension to return a model for
     * @return an extension model for the specified extension type
     */
    public static ExtensionNodeModel of(Class<? extends ExtensionNode<?>> nodeType) {
        return CACHE.get(nodeType);
    }

    /** A builder for {@link ExtensionModel}. This builder is used by ExtensionModel. */
    static class Builder extends OnHookMemberProcessor {

        final ExtensionModel.Builder builder;

        /**
         * @param actualType
         */
        Builder(ExtensionModel.Builder builder, Class<? extends ExtensionNode<?>> actualType) {
            super(ExtensionNode.class, actualType, false);
            this.builder = builder;
        }

        ExtensionNodeModel build(ExtensionModel<?> extensionModel) {
            findMethods();
            return new ExtensionNodeModel(extensionModel, this);
        }

        @Override
        public void processMethod(Method method) {
            builder.onHooks.processMethod(method);
        }
    }
}
