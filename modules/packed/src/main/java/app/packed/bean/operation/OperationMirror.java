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
package app.packed.bean.operation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.bean.BeanMirror;
import app.packed.bean.operation.sandbox.OperationErrorHandlingMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.inject.mirror.Dependency;
import app.packed.mirror.Mirror;
import packed.internal.container.ExtensionSetup;

/**
 *
 */
// Is invoked by an extension

// A bean function
// A bean method
// A bean field get/set/compute

// A bean constructor is _not_ an operation... Or maybe

// AnnotatedElement????? Nah, Det er targettt der kan vaere annoteret

// Attribute support. Det vil give mening at kunne attache noget information af lidt mere dynamisk karakter?
// Fx informations annotations?? Her taenker jeg paa OpenAPI annoteringer
public class OperationMirror implements Mirror {

    /**
     * The internal configuration of the extension we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)} which must be called by extension developers via
     * {@link Extension#mirrorInitialize(ExtensionMirror)}.
     */
    @Nullable
    private ExtensionSetup extension;

    /**
     * Create a new operation mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    protected OperationMirror() {}

    public final List<Dependency> dependencies() {
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
        return List.of();
    }

    /** {@return the bean the operation belongs to.} */
    public final BeanMirror bean() {
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

    /** {@return how errors are handle when calling the operation.} */
    public final OperationErrorHandlingMirror errorHandling() {
        // field??? Unhandled?
        throw new UnsupportedOperationException();
    }

    /** {@return any operation interceptors that are registered.} */
    public final List<Object> interceptors() {
        // decorators???
        throw new UnsupportedOperationException();
    }

    /** {@return the extension that initiates the operation.} */
    public final Class<? extends Extension<?>> operator() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) throws NoSuchMethodException, SecurityException {
        Method method = OperationMirror.class.getMethod("main", String[].class);
        System.out.println(method.getReturnType());
    }

    /**
     * <p>
     * This might not match the return type of any underlying method. For exa
     * 
     * @return the
     */
    // why not just return void???
    // returnType() + returnTypeToken()
    public final Optional<TypeToken<?>> resultType() {
        return Optional.empty();
    }

    /**
     * @return
     */
    public final TargetMirror target() {
        throw new UnsupportedOperationException();
    }

    public interface TargetMirror {
        TargetType type();
    }

    public enum TargetType {

        /** The operation is based on accessing a {@link Field}. */
        FIELD,

        /** The operation is based on invoking a {@link Method}. */
        METHOD,

        /** The operation is based on invoking a {@link Constructor} */
        CONSTRUCTOR,

        /** The operation is based on invoking a method on a {@link FunctionalInterface}. */
        FUNCTION,

        OTHER; // Typically a MethodHandle, or an instance
    }

}
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