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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InaccessibleObjectException;

import app.packed.container.extension.OldExtensionNode;
import app.packed.reflect.UncheckedIllegalAccessException;
import packed.internal.hook.HookClassBuilder;
import packed.internal.reflect.MemberFinder;
import packed.internal.util.StringFormatter;

/**
 * A runtime model of an {@link OldExtensionNode} implementation.
 */
public final class ExtensionNodeModel {

    public final Class<? extends OldExtensionNode<?>> type;

    /**
     * Creates a new extension model.
     * 
     * @param builder
     *            the builder for this model
     */
    private ExtensionNodeModel(ExtensionModel<?> extension, Builder builder) {
        // this.extension = requireNonNull(extension);
        this.type = builder.type;
    }

    /** A builder for {@link ExtensionModel}. This builder is used by ExtensionModel. */
    static class Builder {

        /** The builder for the corresponding extension model. */
        final ExtensionModel.Builder builder;

        Lookup lookup = MethodHandles.lookup();

        final HookClassBuilder p;

        final Class<? extends OldExtensionNode<?>> type;

        /**
         * @param type
         */
        Builder(ExtensionModel.Builder builder, Class<? extends OldExtensionNode<?>> type) {
            p = new HookClassBuilder(type, false);
            this.type = type;
            this.builder = builder;
            lookup = MethodHandles.lookup();
            try {
                builder.hooks.lookup = MethodHandles.privateLookupIn(type, lookup);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the hook aggregate " + StringFormatter.format(type) + ", the module '"
                        + type.getModule().getName() + "' in which the class is located must be 'open' to 'app.packed.base'", e);
            }
        }

        ExtensionNodeModel build(ExtensionModel<?> extensionModel) {
            MemberFinder.findMethods(OldExtensionNode.class, type, method -> {
                builder.hooks.processMethod(method);
            });
            return new ExtensionNodeModel(extensionModel, this);
        }
    }
}
