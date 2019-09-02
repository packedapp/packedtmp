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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.inject.InstantiationMode;
import app.packed.inject.Provide;
import app.packed.util.FieldDescriptor;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.util.descriptor.InternalMemberDescriptor;

/** A descriptor for a member annotated with {@link Provide}. */
public final class AtProvides {

    /** An (optional) description from {@link Provide#description()}. */
    @Nullable
    public final String description;

    /** The instantiation mode from {@link Provide#instantionMode()}. */
    public final InstantiationMode instantionMode;

    /** Whether or not the member on which the annotation is present is a static member. */
    public final boolean isStaticMember;

    /** The key under which the provided service will be made available. */
    public final Key<?> key;

    /** The annotated member, either an {@link FieldDescriptor} or an {@link MethodDescriptor}. */
    public final Member member;

    /** The annotated value, is used for creating config sites. */
    public final Provide provides;

    /** The dependencies (parameters) of the member. */
    public final List<InternalDependencyDescriptor> dependencies;

    public final MethodHandle methodHandle;

    public AtProvides(MethodHandle mh, InternalMemberDescriptor member, Key<?> key, Provide provides, List<InternalDependencyDescriptor> dependencies) {
        this.methodHandle = requireNonNull(mh);
        this.dependencies = requireNonNull(dependencies);
        this.provides = requireNonNull(provides);
        this.description = provides.description().length() > 0 ? provides.description() : null;
        this.member = requireNonNull(member);
        this.instantionMode = provides.instantionMode();
        this.isStaticMember = Modifier.isStatic(member.getModifiers());
        this.key = requireNonNull(key);
    }
}
