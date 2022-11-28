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

import app.packed.bean.BeanIntrospector.OnBinding;
import internal.app.packed.operation.PackedInvocationType;

/**
 *
 */
// I can't see why we should not define context here
// I think we should have a builder probably.
// So we can condense information

// InvocationSite, InvocationType, Invocation contexts

public sealed interface InvocationType permits PackedInvocationType {

    boolean requiresArena();

    int beanInstanceIndex();

    // Tror vi styrer return type her.
    // Man boer smide custom fejl beskeder

    /** {@return the method type representing the invocation.} */
    MethodType methodType();

    /**
     * @param type
     * @return
     * 
     * @see OnBinding#provideFromInvocationArgument(int)
     */
    InvocationType withArg(Class<?> type);

    default InvocationType withBeanInstance() {
        return withBeanInstance(Object.class);
    }

    default InvocationType withClassifier(Class<?> type) {
        throw new UnsupportedOperationException();
    }
    
    InvocationType withBeanInstance(Class<?> beanClass);

    InvocationType withReturnType(Class<?> type);

    default InvocationType withReturnTypeObject() {
        return withReturnType(Object.class);
    }

    // Takes EBC returns void
    static InvocationType defaults() {
        return PackedInvocationType.DEFAULTS;
    }

    static InvocationType raw() {
        return new PackedInvocationType(-1, -1, MethodType.methodType(void.class));
    }

    enum ArgumentKind {
        
        CLASSIFIER,
        
        ARGUMENT,

        /** The invocation argument is a bean instance. */
        BEAN_INSTANCE,

        /** The invocation argument is an extension bean context. */
        // Maaske noget andet end context, given dens mening
        EXTENSION_BEAN_CONTEXT; // InvocationContext
    }
}
