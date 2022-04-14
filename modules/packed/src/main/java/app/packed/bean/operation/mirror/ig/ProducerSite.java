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
package app.packed.bean.operation.mirror.ig;

import app.packed.component.Realm;

/**
 *
 */
public /*non-sealed*/ interface ProducerSite extends InjectionSite {
    
    Realm producer();

    
    interface MethodProducerSite extends ProducerSite {
        
    }
    
    interface ConstantProducerSite extends ProducerSite {

        // Er det ikke en implementerings detalje vi er ligeglade med?
        
        boolean isBuildTimeConstant();
        // no dependencies
    }
}