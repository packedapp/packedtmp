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
package app.packed.bean.hooks.sandboxinvoke;

import app.packed.base.Nullable;
import app.packed.inject.Factory;

/**
 *
 */
public interface VarInjector {

    /**
     * @param factory
     * 
     * @throws IllegalStateException
     *             if a bind method has already been called on this binder instance (I think it is fine to allow it to be
     *             overridden by itself)
     */
    void inject(Factory<?> factory);

    void injectConstant(Object constant);

    /**
     * Provides the same nullable constant to the variable at runtime.
     * 
     * @param constant
     *            the constant to provide to the variable
     * @throws ClassCastException
     *             if the type of the constant does not match the type of the variable
     */
    void injectExactConstant(@Nullable Object constant);

    void injectMissing();
}

enum VarInjectionOptions {
    // ??
    
    // VarArgs
}

interface VarSandbox {

    void provideVia(Factory<?> factory); // Created at runtime using @Provide

    void provideVia(Object o); // Created once at runtime, used for every call

}

/**
 * 
 */
// If DefaultValue try and fetch it.
// Otherwise null if @Nullable or Optional.empty
// Provider<@Nullable >
//void missingValue();
//// Skal saettes statisk paa bootstrappe vil jeg mene??? IDK
//enum Availability {
//    /** For example, ServiceRegistry. */
//    ALWAYS_AVAILABLE, 
//    
//    /** For example, DatabaseX */
//    KNOWN_AT_BUILDTIME,
//    
//    /** For example, Transaction (extracted from ScopeLocal) */
//    KNOWN_AT_RUNTIME // Kan den misforstaas som at naar vi er initialiseret ved vi det?? Er det i virkeligheden unknown
//}