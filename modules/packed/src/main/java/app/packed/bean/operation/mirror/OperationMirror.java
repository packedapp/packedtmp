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
package app.packed.bean.operation.mirror;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.bean.operation.interceptor.OperationInterceptorMirror;
import app.packed.bean.operation.usage.ServiceExportMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;
import app.packed.inject.DependencyMirror;
import app.packed.inject.service.ServiceRegistry;
import app.packed.mirror.Mirror;
import packed.internal.bean.operation.OperationSetup;
import packed.internal.container.ExtensionSetup;

/**
 * A mirror for an bean operation.
 * <p>
 * This class can be extended to provide more detailed information about a particular type of operation. For example,
 * the {@link app.packed.inject.service.ServiceExtension} provides details about an exported service via
 * {@link ServiceExportMirror}.
 * <p>
 * NOTE: Subclasses of this class:
 * <ul>
 * <li>Must be located in the same module as the extension it is a member of (iff the extension is defined in a
 * module).</li>
 * </ul>
 */
//Class -> members
//Scanning class -> Hooks
//Bean -> Operation
public class OperationMirror implements Mirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)} which must be called by extension developers via
     * {@link Extension#mirrorInitialize(ExtensionMirror)}.
     */
    @Nullable
    private OperationSetup operation;

    /**
     * Create a new operation mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public OperationMirror() {}

    /** {@return the services that are available at this injection site.} */
    public final ServiceRegistry availableServices() {
        throw new UnsupportedOperationException();
    }

    /** {@return the bean the operation belongs to.} */
    public final BeanMirror bean() {
        return operation().bean.mirror();
    }
    
    /**
     * Returns whether or not a new bean instance is created every time the operation is invoked.
     * 
     * {@return true if the operation creates a new bean instance every time it is invoked, otherwise false.}
     */
    public final boolean createsNewBean() {
        // Hvad med en constructor??

        // Operation
        // Factory
        // Operation+Factory

        // Jeg tror maaske Factory ikke er operations...
        // Men saa er de irriterende i forbindelse med injection....

        return false;
    }
    
    public final boolean createsNewThread() {
        // synchronous (in calling thread)
        // Spawn (er jo en slags asynchronous...)
        // Hoere det til i noget meta data per extension???
        return false;
    }

    public final List<DependencyMirror> dependencies() {
        // ; // What are we having injected... Giver det mening for functions????

        // BiFunction(WebRequest, WebResponse) vs
        // foo(WebRequest req, WebResponse res)
        // Hvorfor ikke...
        // Ja det giver mening!

        // @WebRequst
        // (HttpRequest, HttpResponse) == (r, p) -> ....

        // Req + Response -> er jo operations variable...
        // Tjah ikke

        // Men er det dependencies??? Ja det er vel fx for @Provide
        // Skal man kunne trace hvor de kommer fra??? Det vil jeg mene
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof OperationMirror m && operation() == m.operation();
    }

    /** {@return how errors are handle when calling the operation.} */
    public final OperationErrorHandlingMirror errorHandling() {
        // field??? Unhandled?
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return operation().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param owner
     *            the internal configuration of the extension to mirror
     */
    final void initialize(OperationSetup operation) {
        if (this.operation != null) {
            throw new IllegalStateException("The specified mirror has already been initialized.");
        }
        this.operation = operation;
    }

    /** {@return any interceptors that are applied to the operation.} */
    public final List<OperationInterceptorMirror> interceptors() {
        throw new UnsupportedOperationException();
    }

    public final String name() {
        //// Vi har vel 3 interessante navne
        // Name
        // BeanExtension#name
        // BeanExtension.longClass#name

        // Kan vi have noget container path
        return "";
    }

    /**
     * {@return the mirrored extension's internal configuration.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror, or the implementation of the extension forgot to call
     *             {@link Extension#mirrorInitialize(ExtensionMirror)} from {@link Extension#mirror()}.
     */
    private OperationSetup operation() {
        OperationSetup o = operation;
        if (o == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorInitialize.");
        }
        return o;
    }

    /** {@return the extension that initiates the operation.} */
    public final Class<? extends Extension<?>> operator() {
        return operation().extension.extensionType;
    }

    /**
     * Returns the return type of the operation. If the operation does not provide {@code void.class} if the the operation
     * does not return a result.
     * <p>
     * This might not match the return type of any underlying method. For examples, you might return a completable future (a
     * wrapper) if returning the result in a wrapper. see method declaredResultType() for actual returning class??? or do we
     * just inspect operation type
     * 
     * Also with regards to return type. If the operator ignores the result, this method typically just returns void.class
     * 
     * @return the
     * 
     * @see Method#getReturnType()
     */
    // why not just return void???
    // returnType() + returnTypeToken()
    public final Class<?> resultType() {
        // declaredResultType kan include wrapperen...
        return void.class;
    }

    /**
     * @return
     */
    public final OperationTargetMirror target() {
         return operation().target.mirror();
    }

    public static void main(String[] args) throws NoSuchMethodException, SecurityException {
        Method method = OperationMirror.class.getMethod("main", String[].class);
        System.out.println(method.getReturnType());
    }
    
    public Optional<Class<? extends Annotation>> hook() {
        // Enten Provide eller ogsaa MetaAnnotation
        throw new UnsupportedOperationException();
    }

//    public enum TargetType {
//
//        /** The operation is based on invoking a {@link Constructor} */
//        CONSTRUCTOR,
//
//        /** The operation is based on accessing a {@link Field}. */
//        FIELD,
//
//        /** The operation is based on invoking a method on a {@link FunctionalInterface}. */
//        FUNCTION,
//
//        /** The operation is based on invoking a {@link Method}. */
//        METHOD,
//
//        OTHER; // Typically a MethodHandle, or an instance
//
//        // CONSTANT;
//    }
}
//Is invoked by an extension

//A bean function
//A bean method
//A bean field get/set/compute

//A bean constructor is _not_ an operation... Or maybe

//AnnotatedElement????? Nah, Det er targettt der kan vaere annoteret

//Attribute support. Det vil give mening at kunne attache noget information af lidt mere dynamisk karakter?
//Fx informations annotations?? Her taenker jeg paa OpenAPI annoteringer

//ExportServiceMirror vs ExportedServiceMirror
//ProvideServiceMirror vs ProvidedServiceMirror
//SubscribeEventMirror vs SubscribedEventMirror
//ConfigUseMirror vs ConfigUsedMirror
//LifecycleCallMirror vs LifecycleCalledMirror
//ScheduleTaskMirror vs ScheduledTaskMirror

///**
//* 
//* If the lifetime
//* 
//* {@return the lifetime of the the operation.}
//*/
//public final LifetimeMirror lifetime() {
//  return bean().lifetime().get();
//  // lifetime != bean.lifetime() -> operation lifetime
//}
//
//public Optional<Lifetime> requiresNewLifetime() {
//  // @Get may be both
//  // @OnInitialize never requires new Lifetime
//
//  // Maaske man kan kan kigge paa lifetime rooten...
//  // Om det er en operation, en
//
//  throw new UnsupportedOperationException();
//}

////Paa en eller anden maade maa den bindes til noget...
//Object boundTo();
//
///** {@return the component behind the action.} */
//ComponentMirror component();
//
//InterruptPolicy interruptPolicy();
//
//boolean isAsync();
//
//public boolean isFactory() {
//// isInitializer
//// Ideen er at constructuren ser anderledes ud
//return false;
//}