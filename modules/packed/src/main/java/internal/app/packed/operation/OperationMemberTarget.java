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
package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import app.packed.operation.OperationTarget;
import internal.app.packed.util.StringFormatter;

/** A operation target wrapping a {@link Member}. */
public sealed abstract class OperationMemberTarget<M extends Member> {

    /** The member. */
    final M member;

    private OperationMemberTarget(M member) {
        this.member = requireNonNull(member);
    }

    /** @see Member#getModifiers(). */
    public final int modifiers() {
        return member.getModifiers();
    }

    public static final class OperationConstructorTarget extends OperationMemberTarget<Constructor<?>> implements OperationTarget.OfConstructor {

        /**
         * @param target
         */
        public OperationConstructorTarget(Constructor<?> constructor) {
            super(constructor);
        }

        /** {@inheritDoc} */
        @Override
        public Constructor<?> constructor() {
            return member;
        }

        @Override
        public String toString() {
            return "Constructor " + StringFormatter.format(member);
        }
    }

    public static final class OperationFieldTarget extends OperationMemberTarget<Field> implements OperationTarget.OfField {

        /** The field's access mode. */
        final AccessMode accessMode;

        /**
         * @param target
         */
        public OperationFieldTarget(Field field, AccessMode accessMode) {
            super(field);
            this.accessMode = accessMode;
        }

        /** {@inheritDoc} */
        @Override
        public AccessMode accessMode() {
            return accessMode;
        }

        /** {@inheritDoc} */
        @Override
        public Field field() {
            return member;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Field " + StringFormatter.format(member) + " (AccessMode + " + accessMode + ")";
        }
    }

    public static final class OperationMethodTarget extends OperationMemberTarget<Method> implements OperationTarget.OfMethod {

        /**
         * @param target
         */
        public OperationMethodTarget(Method method) {
            super(method);
        }

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return member;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Method " + StringFormatter.format(member);
        }
    }
}
