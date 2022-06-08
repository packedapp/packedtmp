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
package packed.internal.bean.hooks;

import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;

import app.packed.bean.BeanField;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.Variable;
import app.packed.operation.OperationBuilder;
import app.packed.operation.OperationTargetMirror;
import packed.internal.container.ExtensionSetup;

/**
 * Implementation of BeanField.
 * 
 * @see Extension#hookOnBeanField(BeanField)
 */
public final class PackedBeanField extends PackedBeanMember<Field> implements BeanField {

    /** Whether or not the field can be read. */
    final boolean allowGet;

    /** Whether or not the field can be written. */
    final boolean allowSet;

    PackedBeanField(BeanMemberScanner scanner, ExtensionSetup extension, Field field, boolean allowGet, boolean allowSet) {
        super(scanner, extension, field);
        this.allowGet = allowGet;
        this.allowSet = allowSet;
    }

    /** {@inheritDoc} */
    @Override
    public Field field() {
        return member;
    }

    /** {@inheritDoc} */
    @Override
    public OperationTargetMirror mirror() {
        return new BuildTimeFieldTargetMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    public OperationBuilder newOperation(ExtensionBeanConfiguration<?> operator, AccessMode accessMode) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public OperationBuilder newGetOperation(ExtensionBeanConfiguration<?> operator) {
//         return new RawOperationSetup<>(this, openClass.unreflectGetter(field));

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public OperationBuilder newSetOperation(ExtensionBeanConfiguration<?> operator) {
        // return new RawOperationSetup<>(this, openClass.unreflectSetter(field));

        return null;
    }

    public VarHandle newVarHandle() {
        return openClass.unreflectVarHandle(member);
    }
    
    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return Variable.ofField(member);
    }

    /** A mirror of {@code OperationTargetMirror.OfFieldAccess}. */
    public record BuildTimeFieldTargetMirror(PackedBeanField pbf) implements OperationTargetMirror.OfFieldAccess {

        /** {@inheritDoc} */
        @Override
        public boolean allowGet() {
            return pbf.allowGet;
        }

        /** {@inheritDoc} */
        @Override
        public boolean allowSet() {
            return pbf.allowSet;
        }

        /** {@inheritDoc} */
        @Override
        public Field field() {
            return pbf.member;
        }
    }
}
