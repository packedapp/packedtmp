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
import java.lang.reflect.Executable;

import app.packed.framework.Nullable;
import app.packed.operation.OperationType;

/**
 *
 */
sealed abstract class PackedOperationalExecutable<E extends Executable>
        extends PackedOperationalMember<E> permits PackedOperationalConstructor, PackedOperationalMethod {

    /** The operation type (lazily created). */
    @Nullable
    OperationType type;

    /** {@inheritDoc} */
    public boolean hasInvokeAccess() {
        return false;
    }

    /**
     * @param ce
     * @param member
     * @param annotations
     */
    PackedOperationalExecutable(ContributingExtension ce, E member, Annotation[] annotations) {
        super(ce, member, annotations);
    }

    public OperationType operationType() {
        OperationType t = type;
        if (t == null) {
            t = type = OperationType.ofExecutable(member);
        }
        return t;
    }
}
