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
package packed.internal.annotations;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ExtensionHookProcessor;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;
import app.packed.inject.InjectionExtension;
import app.packed.inject.Provide;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMemberDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * Information about fields and methods annotated with {@link Provide}, typically on a single class. Used for both
 * services, components, import and export stages.
 */
public final class AtProvidesGroup implements BiConsumer<ComponentConfiguration, InjectionExtension> {

    /** An empty provides group. */
    private static final AtProvidesGroup EMPTY = new AtProvidesGroup(new Builder());

    /** Whether or not there are any non-static providing fields or methods. */
    public final boolean hasInstanceMembers;

    /** An immutable map of all providing members. */
    public final Map<Key<?>, AtProvides> members;

    /**
     * Creates a new provides group
     * 
     * @param builder
     *            the builder to create the group for
     */
    private AtProvidesGroup(Builder builder) {
        this.members = builder.members == null ? Map.of() : Map.copyOf(builder.members);
        this.hasInstanceMembers = builder.hasInstanceMembers;
    }

    /** {@inheritDoc} */
    @Override
    public void accept(ComponentConfiguration cc, InjectionExtension e) {
        e.set(cc, this);
    }

    /** A builder for an {@link AtProvidesGroup}. */
    public final static class Builder extends ExtensionHookProcessor<InjectionExtension> {

        /** Whether or not there are any non-static providing fields or methods. */
        private boolean hasInstanceMembers;

        /** A set of all keys for every provided service. */
        @Nullable
        private HashMap<Key<?>, AtProvides> members;

        /**
         * Creates a new group from this builder.
         * 
         * @return the new group
         */
        @Override
        public AtProvidesGroup onBuild() {
            if (members == null) {
                throw new Error();
            }
            return members == null ? EMPTY : new AtProvidesGroup(this);
        }

        @OnHook
        public void onProvide(AnnotatedMethodHook<Provide> amh) {
            InternalMethodDescriptor descriptor = (InternalMethodDescriptor) amh.method();
            tryAdd0(amh.lookup(), descriptor, Key.fromMethodReturnType(descriptor.newMethod()), amh.annotation(),
                    InternalDependencyDescriptor.fromExecutable(descriptor));
        }

        @OnHook
        public void onProvide(AnnotatedFieldHook<Provide> amh) {
            tryAdd0(amh.lookup(), InternalFieldDescriptor.of(amh.field()), Key.fromField(amh.field().newField()), amh.annotation(), List.of());
        }

        private AtProvides tryAdd0(Lookup lookup, InternalMemberDescriptor descriptor, Key<?> key, Provide provides,
                List<InternalDependencyDescriptor> dependencies) {
            AtProvides ap = new AtProvides(lookup, descriptor, key, provides, dependencies);
            hasInstanceMembers |= !ap.isStaticMember;

            // Check this
            // if (instantionMode != InstantiationMode.PROTOTYPE && hasDependencyOnInjectionSite) {
            // throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
            // }

            if (members == null) {
                members = new HashMap<>();
            }
            if (members.putIfAbsent(key, ap) != null) {
                throw new InvalidDeclarationException(ErrorMessageBuilder.of(descriptor.getDeclaringClass())
                        .cannot("have multiple members providing services with the same key (" + key.toStringSimple() + ").")
                        .toResolve("either remove @Provides on one of the members, or use a unique qualifier for each of the members"));
            }
            return ap;
        }
    }
}
