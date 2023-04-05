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
package sandbox.extension.operation;

import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Set;

import app.packed.context.Context;
import internal.app.packed.context.publish.ContextTemplate;
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
//// BeanInstance, BeanPouch

//// ExtensionContext  (Or InvocationContext??? IDK bliver jo brugt across multi usage)
//// Contexts

// Reserved arguments: ExtensionContext | Wirelet[] | BeanInstance
// Free arguments available for hooks with the same extension type
// Context

//// ErrorHandling <- er paa Operationen ikke lifetimen

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

// EC? BeanInstance? [Context*] Speciel (fields)
public sealed interface OperationTemplate permits PackedOperationTemplate {

    int beanInstanceIndex();

    Set<Class<? extends Context<?>>> contexts();

    default boolean newLifetime() {
        return false;
    }

    /**
     *
     * @return the method type representing the invocation
     */
    MethodType invocationType();

    /** {@return an operation template that ignores any return value.} */
    // If you want to fail. Check return type
    OperationTemplate returnIgnore();

    OperationTemplate returnType(Class<?> type);

    default OperationTemplate returnTypeObject() {
        return returnType(Object.class);
    }

    default OperationTemplate withBeanInstance() {
        return withBeanInstance(Object.class);
    }

    OperationTemplate withBeanInstance(Class<?> beanClass);

    // Hvad sker der naar den er i andre lifetimes?
    OperationTemplate withContext(ContextTemplate context);

//    default OperationTemplate withoutContext(Class<? extends Context<?>> contextClass) {
//        // Den eneste usecase er at fjerne ContainerContext
//        return this;
//    }

    // Takes EBC returns void

    // Tror ikke laengere man kan lave dem direkte paa den her maade...
    static OperationTemplate defaults() {
        return PackedOperationTemplate.DEFAULTS;
    }

    //
//  default OperationTemplate withClassifier(Class<?> type) {
//      throw new UnsupportedOperationException();
//  }

    static OperationTemplate raw() {
        return new PackedOperationTemplate(Map.of(), -1, -1, MethodType.methodType(void.class), false);
    }
//
//    enum ArgumentKind {
//
//        ARGUMENT,
//
//        /** The invocation argument is a bean instance. */
//        BEAN_INSTANCE,
//
//        /** The invocation argument is an extension bean context. */
//        // Maaske noget andet end context, given dens mening
//        EXTENSION_BEAN_CONTEXT; // InvocationContext
//    }
}
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
