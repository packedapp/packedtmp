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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

import app.packed.bean.BeanKind;
import app.packed.context.ContextTemplate;
import internal.app.packed.operation.PackedOperationTemplate;

/**
 * An operation template defines the basic behaviour of an operation.
 * <p>
 *
 * and is typically reused across multiple operations.
 *
 *
 * <p>
 */

// Return types
/// Checks
/// Mappers (fx sealed record faetter)
/// Sidecar extractor

public sealed interface OperationTemplate permits PackedOperationTemplate {

    // Skal hellere vaere BeanPackaging
    int beanInstanceIndex();

    // InvocationContexts? Or all contexts
    // SessionContext kan f.eks. komme fra en ExtensionContext
    // Men det er ikke et argument noget sted

    // Replace With ContextTemplate.Descriptor
    Map<Class<?>, ContextTemplate> contexts();

    List<Class<? extends Throwable>> allowedThrowables();

    // Problemet er dynamic types. Hvor vi er interesset i at embedded den
    default List<Class<?>> returnTypes() {
        throw new UnsupportedOperationException();
    }

    default List<Class<?>> parameterTypes() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the method type representing the invocation
     */
    // Does not really work with dynamic return types();
    MethodType invocationType();

    default boolean newLifetime() {
        return false;
    }

    // Tror ikke laengere man kan lave dem direkte paa den her maade...
    static OperationTemplate defaults() {
        return PackedOperationTemplate.DEFAULTS;
    }

    static OperationTemplate ofFunction(Class<?> functionalInterface, OperationType operationType) {
        return ofFunction(MethodHandles.publicLookup(), functionalInterface, operationType);
    }

    static OperationTemplate ofFunction(MethodHandles.Lookup caller, Class<?> functionalInterface, OperationType operationType) {
        BeanKind.STATIC.template();
        throw new UnsupportedOperationException();
    }

    // requireBeanInstance() <- always 1 param
    default OperationTemplate withAppendBeanInstance() {
        return withAppendBeanInstance(Object.class);
    }

    OperationTemplate withAppendBeanInstance(Class<?> beanClass);

    // Hvad sker der naar den er i andre lifetimes?
    OperationTemplate withContext(ContextTemplate context);

    OperationTemplate withRaw();

    /** {@return an operation template that ignores any return value.} */
    // If you want to fail. Check return type
    // Isn't it just void???
    // I think this won't fail unlike returnType(void.class)
    // which requires void return type
    OperationTemplate withReturnIgnore();

    OperationTemplate withReturnType(Class<?> type);

    // Field type, Method return Type
    // The operation template will be re-adjusted before being used
    OperationTemplate withReturnTypeDynamic();

    OperationTemplate withAllowedThrowables(Class<? extends Throwable> allowed);

    /**
    *
    */

    // Source
    //// Method/Constructor
    //// Field -> Get/Set/Invoke
    //// Function / Op / MethodHandle

    // "Targets"

    // FullOp
    // ChildOp
    // DelegateTo
    // (Bounded) EmbeddedOp (Er aldrig visible...

}

interface sandBox {

    // Was argument type.
    enum BeanInstanceHowToGet {

        /** The invocation argument is a bean instance. */
        EXPLITCIT_BEAN_INSTANCE,

        /** The invocation argument is an extension bean context. */
        // Maaske noget andet end context, given dens mening
        FROM_EXTENSION_CONTEXT; // InvocationContext
    }

    enum BeanInstanceHowToGet2 {
        // Operation never takes a bean
        STATIC
    }
}
//Reserved arguments: ExtensionContext | Wirelet[] | BeanInstance
//Free arguments available for hooks with the same extension type
//Context

////ErrorHandling <- er paa Operationen ikke lifetimen

//InvocationSite, InvocationType, Invocation contexts

//InvocationType
//Context's
//Name
//Codegen Type... (MH I guess)

//Tror invocation orderen er fixed for de forskellige typer...
//Args er altid til sidst...

//OT.forBeanOperation()
//OT.forNewApplication()
//OT.forNewContainer

//IsComposite

//EC? BeanInstance? [Context*] Speciel (fields)
//
//// 3 choices?
//// No ErrorHandling (Exception will propagate directly)
//// ParentHandling
//// This errorHandler
//
//// All but noErrorHandling will install an outward interceptor
//default OperationTemplate handleErrors(ErrorHandler errorHandler) {
//    throw new UnsupportedOperationException();
//}
