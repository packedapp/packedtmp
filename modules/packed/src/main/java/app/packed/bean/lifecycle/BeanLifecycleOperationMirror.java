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

import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;

/**
 * An operation that is invoked doing lifecycle events on the bean. Typically from the usage of {@link Inject},
 * {@link OnInitialize}, {@link OnStart}, or {@link OnStop}.
 */
public sealed class BeanLifecycleOperationMirror extends OperationMirror
        permits InjectOperationMirror, InitializeOperationMirror, StartOperationMirror, StopOperationMirror {

    /**
     * @param handle
     */
    public BeanLifecycleOperationMirror(OperationHandle<?> handle) {
        super(handle);
    }

    /**
     * The lifetime the operation is run in.
     *
     * @return
     */
    public LifetimeMirror lifetime() {
        return bean().lifetime();
    }

    public LifecycleDependantOrder isNaturalOrder() {
        throw new UnsupportedOperationException();
    }

    /** {@return the lifetime operation this operation is a part of.} */
    // IDK, supportere vi Lifecycle events there ikke har en Lifetime operation???
    // Saa er det ikke en lifetime. Fx restart
    public BeanLifecycleOperationMirror lifetimeOperation() {
        throw new UnsupportedOperationException();
    }

}

// Should we use Initializing or Initialized, Stopping or Stopped?
// If   we need this method, it must be the transitioning runstate
//
//public RunState runState() {
//    return RunState.INITIALIZED;
//}

//public List<BeanLifecycleOperationMirror> previousLifetime() {
//    // IDeen er at kunne printe alle operationer der bliver koert foer den her operation i den samme
//    // lifetime
//
//    // Skal naturligvis ogsaa have en efter
//
//    throw new UnsupportedOperationException();
//}

///**
// * A mirror for an operation that creates a new instance of a bean.
// * <p>
// * The operator of this operation is always {@link BaseExtension}.
// */
//// IDK know if we want this
//public static class BeanInstantiationOperationMirror extends LifecycleOperationMirror {}

// Hvis jeg register en instance har min bean ikke en
// Men factory og non-static class har altid en
// En void eller static bean har aldrig en

// Operatoren er vel altid operateren af lifetimen?
// Hmm hvad med @Conf <--- Her er operatoren vel ConfigExtension
// det betyder at operatoren maa vaere BeanExtension hvilket vel er aligned
// med @OnInitialize
//// Previous on bean?
//// Previous on Lifetime
//// Maybe better to leave out
//Optional<BeanLifecycleOperationMirror> previous() {
//    return Optional.empty();
//}