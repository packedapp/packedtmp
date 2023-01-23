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

import app.packed.bean.BeanIntrospector.AnnotationCollection;
import app.packed.bean.BeanIntrospector.BindableBaseVariable;
import app.packed.bean.BeanIntrospector.BindableVariable;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.binding.mirror.BindingMirror;
import app.packed.context.Context;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import app.packed.operation.Op;

/**
 *
 */
public class PackedBindableBaseVariable implements BindableBaseVariable {
    public final BeanScannerBeanVariable v;

    PackedBindableBaseVariable(BeanScannerBeanVariable v) {
        this.v = v;
    }

    public AnnotationCollection annotations() {
        return v.annotations();
    }

    public Map<Class<? extends Context<?>>, List<Class<?>>> availableContexts() {
        return v.availableContexts();
    }

    public List<Class<?>> availableInvocationArguments() {
        return v.availableInvocationArguments();
    }

    public void bindCompositeRecord() {
        v.bindCompositeRecord();
    }

    public void bindConstant(@Nullable Object obj) {
        v.bindConstant(obj);
    }

    public void bindTo(Op<?> op) {
        v.bindTo(op);
    }

    public void bindToInvocationArgument(int argumentIndex) {
        v.bindToInvocationArgument(argumentIndex);
    }

    public void bindToInvocationContextArgument(Class<? extends Context<?>> context, int argumentIndex) {
        v.bindToInvocationContextArgument(context, argumentIndex);
    }

    public void checkAssignableTo(Class<?>... additionalClazzes) {
        v.checkAssignableTo(additionalClazzes);
    }

    public boolean equals(Object obj) {
        return v.equals(obj);
    }

    public void failWith(String postFix) {
        v.failWith(postFix);
    }

    public int hashCode() {
        return v.hashCode();
    }

    public Class<? extends Extension<?>> invokedBy() {
        return v.invokedBy();
    }

    public boolean isAssignable(Class<?> clazz, Class<?>... additionalClazzes) {
        return v.isAssignable(clazz, additionalClazzes);
    }

    public boolean isBound() {
        return v.isBound();
    }

    public Class<?> rawType() {
        return v.rawType();
    }

    public BindableVariable specializeMirror(Supplier<? extends BindingMirror> supplier) {
        return v.specializeMirror(supplier);
    }

    public String toString() {
        return v.toString();
    }

    public Variable variable() {
        return v.variable();
    }

    public Key<?> variableToKey() {
        return v.variableToKey();
    }

    public BindableBaseVariable wrapAsBaseBindable() {
        return v.wrapAsBaseBindable();
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
    public void bindToGeneratedConstant(Supplier<?> consumer) {
        v.bindToGeneratedConstant(consumer);
    }
}
