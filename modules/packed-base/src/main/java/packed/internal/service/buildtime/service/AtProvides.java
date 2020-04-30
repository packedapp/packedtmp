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
package packed.internal.service.buildtime.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.reflect.FieldDescriptor;
import app.packed.base.reflect.MemberDescriptor;
import app.packed.base.reflect.MethodDescriptor;
import app.packed.service.Provide;
import app.packed.service.ServiceMode;
import packed.internal.inject.ServiceDependency;

/** A descriptor for a field or method (member) annotated with {@link Provide}. */
final class AtProvides {

    /** An (optional) description from {@link Provide#description()}. */
    @Nullable
    final String description;

    /** The instantiation mode from {@link Provide#instantionMode()}. */
    final ServiceMode instantionMode;

    /** Whether or not the member on which the annotation is present is a static member. */
    final boolean isStaticMember;

    /** The key under which the provided service will be made available. */
    final Key<?> key;

    /** The annotated member, either an {@link FieldDescriptor} or an {@link MethodDescriptor}. */
    final MemberDescriptor member;

    /** The annotated value, is used for creating config sites. */
    final Provide provides;

    /** The dependencies (parameters) of the member. */
    final List<ServiceDependency> dependencies;

    /** An unbound method handle to the underlying field or method. */
    final MethodHandle methodHandle;

    AtProvides(MethodHandle mh, MemberDescriptor member, Key<?> key, Provide provides, List<ServiceDependency> dependencies) {
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