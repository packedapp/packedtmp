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
package app.packed.service;

import app.packed.bindings.Key;
import app.packed.bindings.Qualifier;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 *
 */
public class ServiceWirelets2 {
    
    static Wirelet requireTransient() {
        throw new UnsupportedOperationException();
    }
    
    static Wirelet exportTransient() {
        throw new UnsupportedOperationException();
    }
    
    // Hmm gider vi gemme de assemblys paa runtime?
    // Maaske hellere noget incremental???
    
    // Smid dem paa Qualifier?
    @Qualifier
    @interface LinkedAssemblyQualifier {
        Class<? extends Assembly> value();
    }
    
    interface ServiceBuildInfo {
        Key<?> key();
        boolean isOptional();
        boolean isSamelifetime();
    }
}
