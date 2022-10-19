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
package app.packed.operation.bindings;

import app.packed.application.ApplicationMirror;
import app.packed.operation.BindingMirror;

/**
 *
 */
// Would be nice if could include information about why it is not present?

// Maaske er ServiceTracer mere interessant?

// T mockIfUnsatisable(); 

// public A(ServiceMock<XService>, ServiceMock<YService>) {}

// a.mockIfUnsatisfiable().

// ServiceMock <- uses ServiceExtension and MockExtension?

// ServiceStub istedet for maaske? Ja vi tester ikke interactioner..

// stub(Option... options); 

interface TraceBinding<T> {

    // is optional
    // is default
    T get();
    
    BindingMirror mirror();
}
class Usage {
    
    
    Usage(TraceBinding<ApplicationMirror> appMirror) {
        appMirror.get();
    }
}

interface ServiceStub<T> {

    T stub();
    T stub(StubOption... options);
    
    interface StubOption {
        
    }
}