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

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnBindingHook;
import app.packed.container.Extension;
import app.packed.operation.BindingMirror;
import app.packed.operation.Op;
import app.packed.operation.Variable;
import internal.app.packed.bean.IntrospectorAnnotationReader;

/**
 *
 */
// extends BindingHandle??? Jeg taenker lidt hvordan vi kan tilfoeje
// Manuelt tilfoejet operationer. Men det er maaske kun funktioner???
public final class PackedOnBindingHook implements OnBindingHook {

    private final int index;

    private final OperationSetup operation;

    Variable variable;

    PackedOnBindingHook(OperationSetup operation, int index) {
        this.operation = operation;
        this.index = index;
        this.variable = variable();
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationReader annotations() {
        return new IntrospectorAnnotationReader(variable().getAnnotations());
    }

    /** {@inheritDoc} */
    @Override
    public void bind(@Nullable Object obj) {
        checkNotBound();
        if (obj == null) {
            if (variable.getType().isPrimitive()) {
                throw new IllegalArgumentException(variable + " is a primitive type and cannot bind to null");
            }
        } else {

        }
        // Check assignable to
        // Create a bound thing
        //
        bind(new ConstantBindingSetup(operation, index, obj));
    }

    private void bind(BindingSetup bs) {
        operation.bindings[index] = bs;
    }
    
    private void checkNotBound() {
        if (operation.bindings[index] != null) {
            throw new IllegalStateException("A binding has already been created");
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnBindingHook bindAtRuntime() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void bindMissing() {}

    /** {@inheritDoc} */
    @Override
    public void bindToInvocationArgument(int index) {}

    /** {@inheritDoc} */
    @Override
    public Class<?> hookClass() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> invokingExtension() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void provide(MethodHandle methodHandle) {}

    /** {@inheritDoc} */
    @Override
    public void provide(Op<?> fac) {}

    /** {@inheritDoc} */
    @Override
    public OnBindingHook specializeMirror(Supplier<? extends BindingMirror> supplier) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return operation.type.parameter(index);
    }

}
