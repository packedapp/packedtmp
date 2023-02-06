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

import app.packed.bean.BeanInstallationException;
import app.packed.framework.AnnotationList;

/** The super class of operational members. The inheritance hierarchy follows that of {@link Member}. */
@SuppressWarnings("rawtypes")
abstract sealed class PackedOperationalMember<M extends Member> permits PackedOperationalField, PackedOperationalExecutable {

    /** Annotations on the member. */
    private final Annotation[] annotations;

    /** The extension that can create new operations from the member. */
    final OperationalExtension extension;

    /** The member. */
    final M member;

    PackedOperationalMember(OperationalExtension ce, M member, Annotation[] annotations) {
        this.extension = ce;
        this.member = member;
        this.annotations = annotations;
    }

    public final AnnotationList annotations() {
        return new PackedAnnotationList(annotations);
    }

    /** Check that we calling from within {@link BeanIntrospector#onField(OnField).} */
    final void checkConfigurable() {
        if (!extension.extension.container.assembly.isConfigurable()) {
            throw new IllegalStateException("This method must be called before the assembly is closed");
        }
    }

    public void failWith(String postFix) {
        throw new BeanInstallationException("Field " + member + ": " + postFix);
    }

    public final int modifiers() {
        return member.getModifiers();
    }
}
