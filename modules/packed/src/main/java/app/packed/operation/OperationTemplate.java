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
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.bean.BeanKind;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.namespace.NamespaceHandle;
import internal.app.packed.context.publish.ContextTemplate;
import internal.app.packed.operation.PackedOperationInstaller;
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

    OperationTemplate.Descriptor descriptor();

    OperationTemplate reconfigure(Consumer<? super Configurator> configure);

    // Tror ikke laengere man kan lave dem direkte paa den her maade...
    static OperationTemplate defaults() {
        return PackedOperationTemplate.DEFAULTS;
    }

    static OperationTemplate delegateTo(ExtensionUseSite useSite) {
        return PackedOperationTemplate.DEFAULTS;
    }

    //
//  default OperationTemplate withClassifier(Class<?> type) {
//      throw new UnsupportedOperationException();
//  }

    static OperationTemplate ofFunction(Class<?> functionalInterface, OperationType operationType) {
        return ofFunction(MethodHandles.publicLookup(), functionalInterface, operationType);
    }

    static OperationTemplate ofFunction(MethodHandles.Lookup caller, Class<?> functionalInterface, OperationType operationType) {
        BeanKind.STATIC.template();
        throw new UnsupportedOperationException();
    }

    static OperationTemplate raw() {
        return PackedOperationTemplate.RAW;
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

    interface Configurator {

        default Configurator appendBeanInstance() {
            return appendBeanInstance(Object.class);
        }

        Configurator appendBeanInstance(Class<?> beanClass);

        // Hvad sker der naar den er i andre lifetimes?
        Configurator inContext(ContextTemplate context);

        /** {@return an operation template that ignores any return value.} */
        // If you want to fail. Check return type
        // Isn't it just void???
        Configurator returnIgnore();

        Configurator returnType(Class<?> type);

        default Configurator returnTypeObject() {
            return returnType(Object.class);
        }

//        default OperationTemplate withoutContext(Class<? extends Context<?>> contextClass) {
//            // Den eneste usecase er at fjerne ContainerContext
//            return this;
//        }

        // Takes EBC returns void

        default Configurator withInvocationArgument(Class<?> argumentClass) {
            // Kan man bruge alle argumenterne? eller kun dem her?
            // Vil mene alle... Hmm, idk.
            // Det er lettere at indexere hvis man det kun er dem man har tilfoejet her
            return this;
        }
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

    /**
     * An installer for operations.
     * <p>
     * An installer can only be used once. After an operation has been installed, all methods will throw
     * {@link IllegalStateException}.
     */
    sealed interface Installer permits PackedOperationInstaller {

        // redelegate(ExtensionPoint.UseSite extension, OperationTemplate);
        Installer delegateTo(ExtensionPoint.ExtensionUseSite extension);

        /**
         * Creates the operation.
         *
         * @param <H>
         *            the type of operation handle to represent the operation
         * @param factory
         *            a factory responsible for creating the operation handle to represent the operation
         * @return the operation handle for the operation
         *
         * @throws IllegalStateException
         *             if the installer has already been used
         */
        <H extends OperationHandle<?>> H install(Function<? super OperationTemplate.Installer, H> factory);

        /**
         * Creates the operation and installs it into the specified namespace.
         *
         * @param <H>
         *            the type of operation handle to represent the operation
         * @param <N>
         *            the type of namespace we are installing the operation into
         * @param namespace
         *            the namespace we are installing the operation into
         * @param factory
         *            a factory responsible for creating the operation handle to represent the operation
         * @return the operation handle for the operation
         *
         * @throws IllegalStateException
         *             if the installer has already been used
         */
        <H extends OperationHandle<?>, N extends NamespaceHandle<?, ?>> H install(N namespace, BiFunction<? super OperationTemplate.Installer, N, H> factory);
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
