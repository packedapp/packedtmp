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
package app.packed.operation;

import java.lang.invoke.MethodType;
import java.util.OptionalInt;

import app.packed.bean.BeanIntrospector.OnBindingHook;

/**
 *
 */
public interface InvocationType {

    OptionalInt beanInstanceIndex();

    // Tror vi styrer return type her.
    // Man boer smide custom fejl beskeder

    /** {@return the method type representing the invocation.} */
    MethodType methodType();

    /**
     * @param type
     * @return
     * 
     * @see OnBindingHook#provideFromInvocationArgument(int)
     */
    InvocationType withArg(Class<?> type);

    InvocationType withBeanInstance(Class<?> beanClass);

    default InvocationType withBeanInstanceObject() {
        return withBeanInstance(Object.class);
    }

    InvocationType withReturnType(Class<?> type);

    default InvocationType withReturnTypeObject() {
        return withReturnType(Object.class);
    }

    // Takes EBC returns void
    static InvocationType defaults() {
        return new PackedInvocationType();
    }

    static InvocationType raw() {
        throw new UnsupportedOperationException();
    }

    enum ArgumentKind {
        ARGUMENT, 
        
        /** The invocation argument is a bean instance. */
        BEAN_INSTANCE, 
        
        CONTEXT, 
        
        /** The invocation argument is an extension bean context. */
        // Maaske noget andet end context, given dens mening
        EXTENSION_BEAN_CONTEXT; // InvocationContext
    }
}

class PackedInvocationType implements InvocationType {

    /** {@inheritDoc} */
    @Override
    public OptionalInt beanInstanceIndex() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public MethodType methodType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType withReturnType(Class<?> type) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType withArg(Class<?> type) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType withBeanInstance(Class<?> beanClass) {
        return null;
    }

}