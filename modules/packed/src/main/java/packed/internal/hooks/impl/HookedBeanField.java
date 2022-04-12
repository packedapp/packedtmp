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
package packed.internal.hooks.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;

import app.packed.base.Variable;
import app.packed.component.Realm;
import app.packed.hooks.BeanField;
import app.packed.hooks.BeanOperation;
import packed.internal.base.variable.FieldVariable;

/**
 *
 */
public class HookedBeanField implements BeanField {

    private final BeanScanner scanner;
    
    private final Field field;

    HookedBeanField(BeanScanner scanner, Field field) {
        this.scanner = scanner;
        this.field = field;
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
    public MethodHandle methodHandleGetter() {
        return scanner.oc.unreflectGetter(field);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle methodHandleSetter() {
        return scanner.oc.unreflectSetter(field);
    }

    /** {@inheritDoc} */
    @Override
    public BeanOperation operation(AccessMode accessMode) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public BeanOperation operationGetter() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public BeanOperation operationSetter() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public VarHandle varHandle() {
        return scanner.oc.unreflectVarHandle(field);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return new FieldVariable(field);
    }

    /** {@inheritDoc} */
    @Override
    public Realm realm() {
        return scanner.bean.realm.realm();
    }

}
