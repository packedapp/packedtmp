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
package internal.app.packed.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import app.packed.bean.BeanIntrospector.AnnotationCollection;

/**
 *
 */
abstract class PackedOperationalMember<M extends Member> {

    /** Annotations on the member. */
    private final Annotation[] annotations;

    /** The extension that can create operations from the field. */
    final ContributingExtension ce;

    /** The member. */
    final M member;

    PackedOperationalMember(ContributingExtension ce, M member, Annotation[] annotations) {
        this.ce = ce;
        this.member = member;
        this.annotations = annotations;
    }

    public final AnnotationCollection annotations() {
        return new PackedAnnotationCollection(annotations);
    }

    /** Check that we calling from within {@link BeanIntrospector#onField(OnField).} */
    final void checkConfigurable() {
        if (ce.bean().container.assembly.isDone()) {
            throw new IllegalStateException("This method must be called before the assembly is closed");
        }
    }

    public final int modifiers() {
        return member.getModifiers();
    }
}
