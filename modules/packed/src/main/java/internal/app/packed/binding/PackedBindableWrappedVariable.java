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
package internal.app.packed.binding;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.binding.BindableVariable;
import app.packed.binding.BindingMirror;
import app.packed.binding.Key;
import app.packed.binding.UnwrappedBindableVariable;
import app.packed.binding.Variable;
import app.packed.context.Context;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;

/**
 *
 */
public record PackedBindableWrappedVariable(PackedBindableVariable var) implements UnwrappedBindableVariable {

    /** {@inheritDoc} */
    @Override
    public UnwrappedBindableVariable allowStaticFieldBinding() {
        var.allowStaticFieldBinding();
        return this;
    }

    @Override
    public AnnotationList annotations() {
        return var.annotations();
    }

    @Override
    public Set<Class<? extends Context<?>>> availableContexts() {
        return var.availableContexts();
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
        var.bindComputedConstant(consumer);
        return this;
    }

    @Override
    public PackedBindableWrappedVariable bindInstance(@Nullable Object obj) {
        var.bindInstance(obj);
        return this;
    }

    @Override
    public PackedBindableWrappedVariable bindContext(Class<? extends Context<?>> context) {
        var.bindContext(context);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BindableVariable bindInvocationArgument(int index) {
        return var.bindInvocationArgument(index);
    }

    /** {@inheritDoc} */
    @Override
    public void bindNone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackedBindableWrappedVariable bindOp(Op<?> op) {
        var.bindOp(op);
        return this;
    }

    @Override
    public Class<?> checkAssignableTo(Class<?>... additionalClazzes) {
        return var.checkAssignableTo(additionalClazzes);
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
        return var.equals(obj);
    }

    @Override
    public void failWith(String postFix) {
        var.failWith(postFix);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasDefaults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return var.hashCode();
    }

    @Override
    public Class<? extends Extension<?>> invokedBy() {
        return var.invokedBy();
    }

    @Override
    public boolean isBound() {
        return var.isBound();
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
    public Variable originalVariable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> rawType() {
        return var.rawType();
    }

    @Override
    public BindableVariable specializeMirror(Supplier<? extends BindingMirror> supplier) {
        return var.specializeMirror(supplier);
    }

    @Override
    public Key<?> toKey() {
        return var.toKey();
    }

    @Override
    public String toString() {
        return var.toString();
    }

    @Override
    public UnwrappedBindableVariable unwrap() {
        return var.unwrap();
    }

    @Override
    public Variable variable() {
        return var.variable();
    }
}
