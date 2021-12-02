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
package app.packed.inject.variable;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.inject.Factory;

/**
 *
 */
// ConstantInjector

// Injector
// MonoInjctor
// BiInjector
// TriInjector

public interface VarInjector {

    /**
     * Provides the same nullable constant to the variable at runtime.
     * 
     * @param constant
     *            the constant to provide to the variable
     * @throws ClassCastException
     *             if the type of the constant does not match the type of the variable
     */
    //
    void inject(Object constant);

    // Must be assignable to var.getType();
    void injectExact(@Nullable Object constant); // injectRaw

    void injectMissing();

    /**
     * @param factory
     * 
     * @throws IllegalStateException
     *             if a bind method has already been called on this binder instance (I think it is fine to allow it to be
     *             overriden by itself)
     */
    void injectDynamic(Factory<?> factory);

    // Det return type of the method handle must match (be assignable to) variable.getType();
    void injectDynamic(MethodHandle methodHandle);
    
    // Skal saettes statisk paa bootstrappe vil jeg mene??? IDK
    enum Availability {
        /** For example, ServiceRegistry. */
        ALWAYS_AVAILABLE, 
        
        /** For example, DatabaseX */
        KNOWN_AT_BUILDTIME,
        
        /** For example, Transaction (extracted from ScopeLocal) */
        KNOWN_AT_RUNTIME // Kan den misforstaas som at naar vi er initialiseret ved vi det?? Er det i virkeligheden unknown
    }
}

// Service s = get(Key);
// if (s==null){
// if (var.allowMissing()) {
// injectMissing(); } else {
// injectMissing("A service with the key <<Key>> could not be found");
// }

// alternative: injectMissing(()->"A service with the key <<Key>> could not be found");
// }

// injectWrappedMissing("A service with the specified key does not exist);

/**
 * 
 */
// If DefaultValue try and fetch it.
// Otherwise null if @Nullable or Optional.empty
// Provider<@Nullable >
//
//
//void injectWrappedMissing(Supplier<String> message);
//
//void injectWrappedMissing(String message);
