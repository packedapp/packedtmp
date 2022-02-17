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
package app.packed.bean.mirror;

import java.util.List;
import java.util.Optional;

import app.packed.base.TypeToken;
import app.packed.bean.BeanMirror;
import app.packed.bean.operation.BeanOperationErrorHandlingMirror;
import app.packed.extension.Extension;
import app.packed.inject.mirror.Dependency;
import app.packed.mirror.Mirror;

/**
 *
 */
// Is invoked by an extension

// A bean function
// A bean method
// A bean field get/set/compute

// A bean constructor is _not_ an operation...

// AnnotatedElement????? Nah, hmm...

public abstract class BeanOperationMirror implements Mirror /* , AnnotatedElement */ {

    public final List<Dependency> dependencies() {
        // ; // What are we having injected... Giver det mening for functions????

        // BiFunction(WebRequest, WebResponse) vs
        // foo(WebRequest req, WebResponse res)
        // Hvorfor ikke...

        // Men er det dependencies??? Ja det er vel fx for @Provide
        // Skal man kunne trace hvor de kommer fra??? Det vil jeg mene
        return List.of();
    }

    /** {@return the bean that declares the operation.} */
    public final BeanMirror bean() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the invocation of the operation creates a new bean instance. Returns false otherwise.
     * 
     * {@return whether or not a new bean is created when the operation starts.}
     */
    public final boolean createsNewBean() {
        // Hvad med en constructor??
        return false;
    }

    public final BeanOperationErrorHandlingMirror errorHandling() {
        // field??? Unhandled?
        throw new UnsupportedOperationException();
    }

    public final List<Object> interceptors() {
        // decorators???
        throw new UnsupportedOperationException();
    }

    /** {@return the extension that initiates the operation.} */
    public final Class<? extends Extension<?>> operator() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * This might not match the return type of any underlying method. For exa
     * 
     * @return the
     */
    public final Optional<TypeToken<?>> resultType() {
        return Optional.empty();
    }

    public final BeanOperationTargetMirror source() {
        throw new UnsupportedOperationException();
    }
}
//ExportServiceMirror vs ExportedServiceMirror
//ProvideServiceMirror vs ProvidedServiceMirror
//SubscribeEventMirror vs SubscribedEventMirror
//ConfigUseMirror vs ConfigUsedMirror
//LifecycleCallMirror vs LifecycleCalledMirror
//ScheduleTaskMirror vs ScheduledTaskMirror

///** {@return the application the operation is a part of.} */
//public final ApplicationMirror application() {
//  return bean().application();
//}

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