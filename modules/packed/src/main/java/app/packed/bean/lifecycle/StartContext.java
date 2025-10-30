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
package app.packed.bean.lifecycle;

import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;

import app.packed.bean.scanning.BeanTrigger.AutoInject;
import app.packed.binding.Key;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.scanning.IntrospectorOnContextService;
import internal.app.packed.extension.BaseExtensionBeanIntrospector;

/** A context object that can be injected into methods annotated with {@link OnStart}. */

// Okay, den eneste maade vi supportere join er via OnStart
// De forskellige scheduling operationer kan ikke supportere det
// Det store problemer er phases
@AutoInject(requiresContext = StartContext.class, introspector = StartContextBeanIntrospector.class)
public interface StartContext extends Context<BaseExtension> {

    default Start.ForkMode forkMode() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param callable
     *
     * @throws UnsupportedOperationException
     *             if called outside of X
     */
    // I should be able to fork multiple
    // if used from forkMode=true. I think submit as siebling.
    // We never create more than 1 task scope
    void fork(Callable<?> callable); // what about when to join?

    void fork(Runnable runnable); // what about when to join?

    default void fork(Callable<?> callable, JoinPolicy joinAt) {} // what about when to join?

    default void fork(Runnable runnable, JoinPolicy joinAt) {} // what about when to join?

    // Will fork and join before transitioning to running. However, it will ignore
    default void forkJoinBeforeRunning(Callable<?> callable) {}
    default void forkJoinBeforeRunning(Runnable runnable) {}

    // Maaske supportere vi kun 1 mode. Og saa maa man bruge scheduleOnce
    enum JoinPolicy {
        BEFORE_DEPENDANTS, AFTER_DEPENDANTS, BEFORE_READY;
    }


}

final class StartContextBeanIntrospector extends BaseExtensionBeanIntrospector {

    @Override
    public void onExtensionService(Key<?> key, IntrospectorOnContextService service) {
        if (service.matchNoQualifiers(StartContext.class)) {
            service.binder().bindContext(StartContext.class);
        }
    }
}

// Pre-Post join er jo lidt en around interceptor...




interface Sandbox {

    /** {@return whether or not the starting method have been forked from the main startup thread} */
    default boolean isForked() {
        return false;
    }

    // Config, Joiner
    // IfForked0-> Parent scope
    // otherwise noScope

    // When do we call join???
    // spawn23Threads->GetFirst->.join()->
    // Manual Join, vs AutoJoin


    // openTaskScope

    default StructuredTaskScope<?,?> openTaskScope() {
        throw new UnsupportedOperationException();
    }

    // Maaske har vi i virkeligheden two scopes her???

    // et "parent" lifetime scope (Before the lifetime, for examples, before the application starts)
    // og en "Bean" lifecycle scope (Before all dendencies)

//  // Will keep running until it is finished
//  void forkNoAwait(Runnable runnable); // what about when to join?

    // Kan man lukke ned normalt under start?
    // Eller er det altid en cancel
//  void fail(Throwable cause); // hvorfor ikke bare smide den...

    // await()
    // StartReason?
}

//Eller har vi noget generisk AsyncDependecyContext???
//Det er jo i virkeligheden en dependency graf...
//TreeDependencyExecutionContext
//Som maaske kan bruges til nogle smart async beregninger...

//I virkeligheden er det jo et trae.. Som vi vil gerne vil kunne bevaege os op ad og hooke ind i.
//Men ikke noedvendig vis aendre.
//Vil gerne kunne sige baade naar beanen er faerdig.
//Og hvis applikationen bliver afinstalleret.

//Skal kunne forke ting.
//Kunne awaite ting
//Kunne faile
//BeanStartContext (saa kan den lidt mere tydeligt bliver brugt af andre annoteringen)