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
package packed.internal.service.build.service;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.List;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.HookGroupBuilder;
import app.packed.hook.OnHook;
import app.packed.reflect.FieldDescriptor;
import app.packed.reflect.MethodDescriptor;
import app.packed.service.Provide;
import app.packed.service.ServiceDependency;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import packed.internal.util.ErrorMessageBuilder;

/** Information about fields and methods annotated with {@link Provide}. */
public final class AtProvidesGroup {

    /** Whether or not there are any non-static providing fields or methods. */
    final boolean hasInstanceMembers;

    /** An immutable map of all providing members. */
    final List<AtProvides> members;

    /**
     * Creates a new group.
     * 
     * @param builder
     *            the builder to create the group for
     */
    private AtProvidesGroup(Builder builder) {
        this.members = builder.members == null ? List.of() : List.copyOf(builder.members.values());
        this.hasInstanceMembers = builder.hasInstanceMembers;
    }

    /** A builder for {@link AtProvidesGroup}. */
    public final static class Builder implements HookGroupBuilder<AtProvidesGroup> {

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

        /**
         * Invoked by the runtime whenever it encounters a field annotated with {@link Provide}.
         * 
         * @param fieldHook
         *            the field hook
         */
        @OnHook
        void onFieldProvide(AnnotatedFieldHook<Provide> fieldHook) {
            FieldDescriptor field = fieldHook.field();

            // Generation of Key, I think we might want to do something that produces a good error message.
            // Maybe just hard code it in key.
            tryAdd0(fieldHook.getter(), field, Key.fromField(field.unsafeField()), fieldHook.annotation(), List.of());
        }

        /**
         * Invoked by the runtime whenever it encounters a method annotated with {@link Provide}.
         * 
         * @param methodHook
         *            the method hook
         */
        @OnHook
        void onMethodProvide(AnnotatedMethodHook<Provide> methodHook) {
            MethodDescriptor method = methodHook.method();

            tryAdd0(methodHook.methodHandle(), method, Key.fromMethodReturnType(method.newMethod()), methodHook.annotation(),
                    ServiceDependency.fromExecutable(method));
        }

        private AtProvides tryAdd0(MethodHandle mh, Member descriptor, Key<?> key, Provide provides, List<ServiceDependency> dependencies) {
            AtProvides ap = new AtProvides(mh, descriptor, key, provides, dependencies);
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
