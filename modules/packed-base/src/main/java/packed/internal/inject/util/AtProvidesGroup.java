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
package packed.internal.inject.util;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.HookAggregateBuilder;
import app.packed.inject.Provide;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import packed.internal.invoke.ExecutableFunctionHandle;
import packed.internal.invoke.FieldFunctionHandle;
import packed.internal.invoke.InvokableMember;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMemberDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * Information about fields and methods annotated with {@link Provide}, typically on a single class. Used for both
 * services, components, import and export stages.
 */
public final class AtProvidesGroup {

    /** Whether or not there are any non-static providing fields or methods. */
    public final boolean hasInstanceMembers;

    /** An immutable map of all providing members. */
    // Not sure we need a map, A list should be fine
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

    /** A builder for {@link AtProvidesGroup}. */
    public final static class Builder implements HookAggregateBuilder<AtProvidesGroup> {

        /** Whether or not there are any non-static providing fields or methods. */
        private boolean hasInstanceMembers;

        /** A set of all keys for every provided service. */
        private final HashMap<Key<?>, AtProvides> members = new HashMap<>();

        /**
         * Creates a new group from this builder.
         * 
         * @return the new group
         */
        @Override
        public AtProvidesGroup build() {
            return new AtProvidesGroup(this);
        }

        void onFieldProvide(AnnotatedFieldHook<Provide> amh) {
            InternalFieldDescriptor field = InternalFieldDescriptor.of(amh.field());

            // Generation of Key, I think we might want to do something that produces a good error message.
            FieldFunctionHandle<?> handle = new FieldFunctionHandle<>(field.getTypeLiteral(), field, amh.varHandle(), null);

            tryAdd0(amh.getter(), handle, field, Key.fromField(field.unsafeField()), amh.annotation(), List.of());
        }

        void onMethodProvide(AnnotatedMethodHook<Provide> amh) {
            InternalMethodDescriptor method = (InternalMethodDescriptor) amh.method();

            ExecutableFunctionHandle<?> handle = new ExecutableFunctionHandle<>(method.returnTypeLiteral(), method, amh.methodHandle(), null);

            tryAdd0(amh.methodHandle(), handle, method, Key.fromMethodReturnType(method.newMethod()), amh.annotation(),
                    InternalDependencyDescriptor.fromExecutable(method));
        }

        private AtProvides tryAdd0(MethodHandle mh, InvokableMember<?> im, InternalMemberDescriptor descriptor, Key<?> key, Provide provides,
                List<InternalDependencyDescriptor> dependencies) {
            AtProvides ap = new AtProvides(mh, descriptor, im, key, provides, dependencies);
            hasInstanceMembers |= !ap.isStaticMember;

            // Check this
            // if (instantionMode != InstantiationMode.PROTOTYPE && hasDependencyOnInjectionSite) {
            // throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
            // }
            if (members.putIfAbsent(key, ap) != null) {
                throw new InvalidDeclarationException(ErrorMessageBuilder.of(descriptor.getDeclaringClass())
                        .cannot("have multiple members providing services with the same key (" + key.toStringSimple() + ").")
                        .toResolve("either remove @Provides on one of the members, or use a unique qualifier for each of the members"));
            }
            return ap;
        }
    }
}
