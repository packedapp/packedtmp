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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;

import app.packed.base.Variable;
import app.packed.bean.hooks.BeanField;
import app.packed.bean.operation.InjectableOperation;
import app.packed.bean.operation.RawOperation;
import app.packed.bean.operation.mirror.OperationTargetMirror;
import app.packed.component.Realm;
import app.packed.extension.Extension;
import packed.internal.bean.operation.RawOperationSetup;
import packed.internal.container.ExtensionSetup;

/**
 * Implementation of BeanField.
 * 
 * @see Extension#hookOnBeanField(BeanField)
 */
public final class PackedBeanField extends PackedBeanMember implements BeanField {

    final boolean allowGet;

    final boolean allowSet;
    
    /** */
    private final Field field;

    PackedBeanField(BeanScanner scanner, ExtensionSetup extension, Field field, boolean allowGet, boolean allowSet) {
        super(scanner, extension);
        this.field = field;
        this.allowGet = allowGet;
        this.allowSet = allowSet;
    }

    /** {@inheritDoc} */
    @Override
    public Field field() {
        return field;
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return field.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public OperationTargetMirror mirror() {
        return new BuildTimeFieldTargetMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    public InjectableOperation operation(AccessMode accessMode) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InjectableOperation operationGetter() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InjectableOperation operationSetter() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RawOperation<MethodHandle> rawGetterOperation() {
        return new RawOperationSetup<>(this, openClass.unreflectGetter(field));
    }

    /** {@inheritDoc} */
    @Override
    public RawOperation<VarHandle> rawOperation() {
        return new RawOperationSetup<>(this, openClass.unreflectVarHandle(field));
    }

    /** {@inheritDoc} */
    @Override
    public RawOperation<MethodHandle> rawSetterOperation() {
        return new RawOperationSetup<>(this, openClass.unreflectSetter(field));
    }

    /** {@inheritDoc} */
    @Override
    public Realm realm() {
        return bean.realm.realm();
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return Variable.ofField(field);
    }

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
            return pbf.field;
        }
    }
}
