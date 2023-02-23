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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import app.packed.context.Context;
import app.packed.extension.BeanVariable;
import app.packed.extension.BeanWrappedVariable;
import app.packed.extension.Extension;
import app.packed.operation.BindingMirror;
import app.packed.operation.Op;
import app.packed.util.AnnotationList;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Variable;

/**
 *
 */
public final class PackedBeanWrappedVariable implements BeanWrappedVariable {

    public final PackedBindableVariable v;

    PackedBeanWrappedVariable(PackedBindableVariable v) {
        this.v = v;
    }

    @Override
    public AnnotationList annotations() {
        return v.annotations();
    }

    @Override
    public Map<Class<? extends Context<?>>, List<Class<?>>> availableContexts() {
        return v.availableContexts();
    }

    @Override
    public List<Class<?>> availableInvocationArguments() {
        return v.availableInvocationArguments();
    }

    @Override
    public void bindConstant(@Nullable Object obj) {
        v.bindConstant(obj);
    }

    @Override
    public void bindOp(Op<?> op) {
        v.bindOp(op);
    }

    @Override
    public void bindInvocationArgument(int argumentIndex) {
        v.bindInvocationArgument(argumentIndex);
    }

    @Override
    public void bindContextValue(Class<? extends Context<?>> context) {
        v.bindContextValue(context);
    }

    @Override
    public Class<?> checkAssignableTo(Class<?>... additionalClazzes) {
        return v.checkAssignableTo(additionalClazzes);
    }

    @Override
    public boolean equals(Object obj) {
        return v.equals(obj);
    }

    @Override
    public void failWith(String postFix) {
        v.failWith(postFix);
    }

    @Override
    public int hashCode() {
        return v.hashCode();
    }

    @Override
    public Class<? extends Extension<?>> invokedBy() {
        return v.invokedBy();
    }

    @Override
    public boolean isBound() {
        return v.isBound();
    }

    @Override
    public Class<?> rawType() {
        return v.rawType();
    }

    @Override
    public BeanVariable specializeMirror(Supplier<? extends BindingMirror> supplier) {
        return v.specializeMirror(supplier);
    }

    @Override
    public String toString() {
        return v.toString();
    }

    @Override
    public Variable variable() {
        return v.variable();
    }

    @Override
    public Key<?> toKey() {
        return v.toKey();
    }

    @Override
    public BeanWrappedVariable unwrap() {
        return v.unwrap();
    }

    /** {@inheritDoc} */
    @Override
    public void bindNone() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void checkNotRequired() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void checkRequired() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasDefaults() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLazy() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNullable() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOptional() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isProvider() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRequired() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Variable originalVariable() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void bindGeneratedConstant(Supplier<?> consumer) {
        v.bindGeneratedConstant(consumer);
    }

    /** {@inheritDoc} */
    @Override
    public BeanWrappedVariable allowStaticFieldBinding() {
        v.allowStaticFieldBinding();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public int modifiers() {
        throw new UnsupportedOperationException();
    }
}
