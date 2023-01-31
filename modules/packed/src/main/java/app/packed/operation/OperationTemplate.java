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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import app.packed.bean.BeanHook.AnnotatedBindingHook;
import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.BaseExtension;
import internal.app.packed.operation.PackedOperationTemplate;

/**
 * An operation template defines the basic behaviour of an operation and is typically reused across multiple operations.
 * 
 * 
 * <p>
 */
// I can't see why we should not define context here
// I think we should have a builder probably.
// So we can condense information

// Components
//// ExtensionContext  (Or InvocationContext??? IDK bliver jo brugt across multi usage) 
//// BeanInstance
//// Wirelet[] (For containers)
//// Contexts
//// Other arguments

//// ErrorHandling

// InvocationSite, InvocationType, Invocation contexts

//InvocationType
//Context's
//Name
//Codegen Type... (MH I guess)

//Tror invocation orderen er fixed for de forskellige typer...
//Args er altid til sidst...

// OT.forBeanOperation()
// OT.forNewApplication()
// OT.forNewContainer

public sealed interface OperationTemplate permits PackedOperationTemplate {

    // return -1 instead...
    default OptionalInt indexOfBeanInstance() {
        return OptionalInt.empty();
    }

    default OptionalInt indexOfExtensionContext() {
        return OptionalInt.empty();
    }

    int beanInstanceIndex();

    // Tror vi styrer return type her.
    // Man boer smide custom fejl beskeder

    default /* Ordered */ Map<Class<? extends Context<?>>, List<Class<?>>> contexts() {
        throw new UnsupportedOperationException();
    }

    // All but noErrorHandling will install an outward interceptor
    default OperationTemplate handleErrors(ErrorHandler errorHandler) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @return the method type representing the invocation
     */
    MethodType invocationType();

    boolean requiresExtensionContext();

    /**
     * @param type
     * @return
     * 
     * @see BindableVariable#provideFromInvocationArgument(int)
     */
    // Kan man have have loese args som ikke er del af en context???
    OperationTemplate withArg(Class<?> type);

    default OperationTemplate withBeanInstance() {
        return withBeanInstance(Object.class);
    }

    OperationTemplate withBeanInstance(Class<?> beanClass);

    default OperationTemplate withClassifier(Class<?> type) {
        throw new UnsupportedOperationException();
    }

    default OperationTemplate withContext(ContextTemplate context) {
        throw new UnsupportedOperationException();
    }

    OperationTemplate withIgnoreReturnType();
    
    OperationTemplate withReturnType(Class<?> type);

    // 3 choices?
    // No ErrorHandling (Exception will propagate directly)
    // ParentHandling
    // This errorHandler

    default OperationTemplate withReturnTypeObject() {
        return withReturnType(Object.class);
    }

    // Takes EBC returns void
    static OperationTemplate defaults() {
        return PackedOperationTemplate.DEFAULTS;
    }

    static OperationTemplate raw() {
        return new PackedOperationTemplate(-1, -1, MethodType.methodType(void.class), false);
    }

    enum ArgumentKind {

        ARGUMENT,

        /** The invocation argument is a bean instance. */
        BEAN_INSTANCE,

        /** The invocation argument is an extension bean context. */
        // Maaske noget andet end context, given dens mening
        EXTENSION_BEAN_CONTEXT; // InvocationContext
    }
    

    /**
     * Move to operation template?
     */
    @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @AnnotatedBindingHook(extension = BaseExtension.class)
    public @interface InvocationArgument {
        int index() default 0;
    }
}
