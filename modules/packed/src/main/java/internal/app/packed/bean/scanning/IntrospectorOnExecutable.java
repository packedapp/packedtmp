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
package internal.app.packed.bean.scanning;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;

import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.util.PackedAnnotationList;

/**
 *
 */
sealed abstract class IntrospectorOnExecutable<E extends Executable> extends IntrospectorOnMember<E> permits IntrospectorOnConstructor, IntrospectorOnMethod {

    /** The operation type (lazily created). */
    // Do we really want to create it lazily?
    //
    @Nullable
    final OperationType type;

    /**
     * @param ce
     * @param member
     * @param annotations
     */
    IntrospectorOnExecutable(BeanIntrospectorSetup ce, E member, Annotation[] annotations) {
        super(ce, member, new PackedAnnotationList(annotations));
        this.type = OperationType.fromExecutable(member);
    }

    /** {@inheritDoc} */
    public boolean hasInvokeAccess() {
        return false;
    }

    public OperationType operationType() {
        return type;
    }
}
