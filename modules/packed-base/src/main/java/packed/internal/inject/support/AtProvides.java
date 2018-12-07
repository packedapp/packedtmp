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
package packed.internal.inject.support;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.invokers.InvokableMember;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMemberDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/** A descriptor of the {@link Provides} annotation. */
public final class AtProvides {

    /** The binding mode from {@link Provides#bindingMode()}. */
    public final BindingMode bindingMode;

    /** Any dependencies (parameters) the annotated member has, is always empty for fields. */
    public final List<InternalDependency> dependencies;

    /** An (optional) description from {@link Provides#description()}. */
    @Nullable
    public final String description;

    /** The annotated member, either an {@link InternalFieldDescriptor} or an {@link InternalMethodDescriptor}. */
    public final InternalMemberDescriptor descriptor;

    /** The invokable member. */
    public final InvokableMember<?> invokable;

    /** whether or not the field or method on which the annotation is present is a static field or method. */
    public final boolean isStaticMember;

    /** The key under which the provided service will be made available. */
    public final Key<?> key;

    AtProvides(InvokableMember<?> invokable, InternalMemberDescriptor descriptor, Key<?> key, Provides provides, List<InternalDependency> dependencies) {
        this.invokable = requireNonNull(invokable);
        this.descriptor = requireNonNull(descriptor);
        this.key = requireNonNull(key);
        this.dependencies = requireNonNull(dependencies);
        this.description = provides.description().length() > 0 ? provides.description() : null;
        this.bindingMode = provides.bindingMode();
        this.isStaticMember = descriptor.isStatic();
    }
}
