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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import app.packed.bean.BeanKind;
import app.packed.operation.OperationType;
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

// IsComposite

// EC? BeanInstance? [Context*] Speciel (fields)
public sealed interface OperationTemplate permits PackedOperationTemplate {

    default OperationTemplate appendBeanInstance() {
        return appendBeanInstance(Object.class);
    }

    OperationTemplate appendBeanInstance(Class<?> beanClass);

    OperationTemplate.Descriptor descriptor();

    /** {@return an operation template that ignores any return value.} */
    // If you want to fail. Check return type
    // Isn't it just void???
    OperationTemplate returnIgnore();

    OperationTemplate returnType(Class<?> type);

    default OperationTemplate returnTypeObject() {
        return returnType(Object.class);
    }

    default OperationTemplate withInvocationArgument(Class<?> argumentClass) {
        // Kan man bruge alle argumenterne? eller kun dem her?
        // Vil mene alle... Hmm, idk.
        // Det er lettere at indexere hvis man det kun er dem man har tilfoejet her
        return this;
    }

//    default OperationTemplate withoutContext(Class<? extends Context<?>> contextClass) {
//        // Den eneste usecase er at fjerne ContainerContext
//        return this;
//    }

    // Takes EBC returns void

    // Hvad sker der naar den er i andre lifetimes?
    OperationTemplate withContext(ContextTemplate context);

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

    static OperationTemplate ofFunction(Class<?> functionalInterface, OperationType operationType) {
        return ofFunction(MethodHandles.publicLookup(), functionalInterface, operationType);
    }

    static OperationTemplate ofFunction(MethodHandles.Lookup caller, Class<?> functionalInterface, OperationType operationType) {
        BeanKind.STATIC.template();
        throw new UnsupportedOperationException();
    }


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

    /**
     * An immutable descriptor for an {@link OperationTemplate}. Acquired by calling {@link OperationTemplate#descriptor()}.
     */
    interface Descriptor {

        // Skal hellere vaere BeanPackaging
        int beanInstanceIndex();

        // InvocationContexts? Or all contexts
        // SessionContext kan f.eks. komme fra en ExtensionContext
        // Men det er ikke et argument noget sted

        // Replace With ContextTemplate.Descriptor
        Map<Class<?>, ContextTemplate.Descriptor> contexts();

        /**
         *
         * @return the method type representing the invocation
         */
        MethodType invocationType();

        default boolean newLifetime() {
            return false;
        }

        // Contexts
    }
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
