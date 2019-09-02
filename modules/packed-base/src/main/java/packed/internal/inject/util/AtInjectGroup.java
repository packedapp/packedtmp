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

import java.util.ArrayList;
import java.util.List;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.HookAggregateBuilder;
import app.packed.inject.Inject;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 *
 */
public final class AtInjectGroup {

    /** An immutable map of all providing members. */
    public final List<AtInject> members;

    /**
     * Creates a new provides group
     * 
     * @param builder
     *            the builder to create the group for
     */
    private AtInjectGroup(Builder builder) {
        this.members = builder.members == null ? List.of() : List.copyOf(builder.members);
    }

    /** A builder for {@link AtInjectGroup}. */
    public final static class Builder implements HookAggregateBuilder<AtInjectGroup> {

        /** A set of all keys for every provided service. */
        private final ArrayList<AtInject> members = new ArrayList<>();

        /**
         * Creates a new group from this builder.
         * 
         * @return the new group
         */
        @Override
        public AtInjectGroup build() {
            return new AtInjectGroup(this);
        }

        void onFieldInject(AnnotatedFieldHook<Inject> fieldHook) {
            InternalFieldDescriptor field = InternalFieldDescriptor.of(fieldHook.field());
            members.add(new AtInject(fieldHook.setter(), field, List.of(InternalDependencyDescriptor.fromField(field))));
        }

        void onMethodProvide(AnnotatedMethodHook<Inject> methodHook) {
            InternalMethodDescriptor method = InternalMethodDescriptor.of(methodHook.method());
            List<InternalDependencyDescriptor> dependencies = InternalDependencyDescriptor.fromExecutable(method);
            // TestNotStatic... Hmm kan ikke kalde hook.checkNotStatic mere...
            members.add(new AtInject(methodHook.methodHandle(), method, dependencies));
        }
    }

}
