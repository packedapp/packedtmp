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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InaccessibleObjectException;

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import app.packed.reflect.UncheckedIllegalAccessException;
import packed.internal.hook.OnHookMemberBuilder;
import packed.internal.reflect.MemberFinder;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.StringFormatter;

/**
 * A runtime model of an {@link ExtensionNode} implementation.
 */
final class ExtensionNodeModel {

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

    /** An type extractor to find the extension type the node belongs to. */
    private static final TypeVariableExtractor EXTENSION_NODE_TV_EXTRACTOR = TypeVariableExtractor.of(ExtensionNode.class);

    /** The extension the node belongs to. */
    public final ExtensionModel<?> extension;

    /**
     * Creates a new extension model.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionNodeModel(ExtensionModel<?> extension, Builder builder) {
        this.extension = requireNonNull(extension);
    }

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
    static class Builder {

        /** The builder for the corresponding extension model. */
        final ExtensionModel.Builder builder;

        Lookup lookup = MethodHandles.lookup();

        final OnHookMemberBuilder p;

        final Class<? extends ExtensionNode<?>> actualType;

        /**
         * @param actualType
         */
        Builder(ExtensionModel.Builder builder, Class<? extends ExtensionNode<?>> actualType) {
            p = new OnHookMemberBuilder(ExtensionNode.class, actualType, false);
            this.actualType = actualType;
            this.builder = builder;
            lookup = MethodHandles.lookup();
            try {
                builder.onHooks.lookup = MethodHandles.privateLookupIn(actualType, lookup);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the hook aggregate " + StringFormatter.format(actualType) + ", the module '"
                        + actualType.getModule().getName() + "' in which the class is located must be 'open' to 'app.packed.base'", e);
            }
        }

        ExtensionNodeModel build(ExtensionModel<?> extensionModel) {
            MemberFinder.findMethods(ExtensionNode.class, actualType, method -> {
                // Det her skal fikses. Basaltset er det fordi lok
                builder.onHooks.processMethod(method);
            });
            return new ExtensionNodeModel(extensionModel, this);
        }
    }
}
