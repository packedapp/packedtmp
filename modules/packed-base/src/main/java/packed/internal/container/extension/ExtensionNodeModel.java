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

import app.packed.container.extension.ExtensionNode;
import packed.internal.reflect.MemberProcessor;

/**
 *
 */
public class ExtensionNodeModel {

    /** A cache of values. */
    private static final ClassValue<ExtensionNodeModel> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionNodeModel computeValue(Class<?> type) {
            return new Builder((Class<? extends ExtensionNode<?>>) type).build();
        }
    };

    /**
     * Creates a new extension model.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionNodeModel(Builder builder) {}

    /**
     * Returns an extension model for the specified extension type.
     * 
     * @param nodeType
     *            the type of extension to return a model for
     * @return an extension model for the specified extension type
     */
    public static ExtensionNodeModel of(Class<? extends ExtensionNode<?>> nodeType) {
        return CACHE.get(nodeType);
    }

    /** A builder for {@link ExtensionModel}. */
    private static class Builder extends MemberProcessor {

        /**
         * @param actualType
         */
        private Builder(Class<? extends ExtensionNode<?>> actualType) {
            super(ExtensionNode.class, actualType);
        }

        private ExtensionNodeModel build() {
            return new ExtensionNodeModel(this);
        }
    }
}
