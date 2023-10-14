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
import java.util.Set;
import java.util.function.Supplier;

import app.packed.context.Context;
import app.packed.extension.BindableVariable;
import app.packed.extension.Extension;
import app.packed.extension.UnwrappedBindableVariable;
import app.packed.operation.BindingMirror;
import app.packed.operation.Op;
import app.packed.util.AnnotationList;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Variable;
import sandbox.extension.operation.OperationTemplate.Descriptor;

/**
 *
 */
public final class PackedBindableWrappedVariable implements UnwrappedBindableVariable {

    public final PackedBindableVariable v;

    PackedBindableWrappedVariable(PackedBindableVariable v) {
        this.v = v;
    }

    /** {@inheritDoc} */
    @Override
    public UnwrappedBindableVariable allowStaticFieldBinding() {
        v.allowStaticFieldBinding();
        return this;
    }

    @Override
    public AnnotationList annotations() {
        return v.annotations();
    }

    @Override
    public Set<Class<? extends Context<?>>> availableContexts() {
        return v.availableContexts();
    }

//    @Override
//    public List<Class<?>> availableInvocationArguments() {
//        return v.availableInvocationArguments();
//    }

    /** {@inheritDoc} */
    @Override
    public List<Class<?>> availableInvocationArguments() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBindableWrappedVariable bindComputedConstant(Supplier<?> consumer) {
        v.bindComputedConstant(consumer);
        return this;
    }

    @Override
    public PackedBindableWrappedVariable bindConstant(@Nullable Object obj) {
        v.bindConstant(obj);
        return this;
    }

    @Override
    public PackedBindableWrappedVariable bindContext(Class<? extends Context<?>> context) {
        v.bindContext(context);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BindableVariable bindInvocationArgument(int index) {
        return v.bindInvocationArgument(index);
    }

    /** {@inheritDoc} */
    @Override
    public void bindNone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackedBindableWrappedVariable bindOp(Op<?> op) {
        v.bindOp(op);
        return this;
    }

    @Override
    public Class<?> checkAssignableTo(Class<?>... additionalClazzes) {
        return v.checkAssignableTo(additionalClazzes);
    }

    /** {@inheritDoc} */
    @Override
    public void checkNoneable() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void checkNotNoneable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        return v.equals(obj);
    }

    @Override
    public void failWith(String postFix) {
        v.failWith(postFix);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasDefaults() {
        throw new UnsupportedOperationException();
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

    /** {@inheritDoc} */
    @Override
    public boolean isLazy() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNoneable() {
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
    public int modifiers() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor operation() {
        return v.operation();
    }

    /** {@inheritDoc} */
    @Override
    public Variable originalVariable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> rawType() {
        return v.rawType();
    }

    @Override
    public BindableVariable specializeMirror(Supplier<? extends BindingMirror> supplier) {
        return v.specializeMirror(supplier);
    }

    @Override
    public Key<?> toKey() {
        return v.toKey();
    }

    @Override
    public String toString() {
        return v.toString();
    }

    @Override
    public UnwrappedBindableVariable unwrap() {
        return v.unwrap();
    }

    @Override
    public Variable variable() {
        return v.variable();
    }
}
