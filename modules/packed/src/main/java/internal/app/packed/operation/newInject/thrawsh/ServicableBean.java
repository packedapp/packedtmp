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
package internal.app.packed.operation.newInject.thrawsh;

import app.packed.base.Nullable;
import internal.app.packed.bean.BeanProps;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.operation.newInject.ProvidedService;
import internal.app.packed.operation.newInject.ServiceBindingSetup;

/**
 * A bean that provides or uses services
 */
// Taenker den bliver lavet ved den foerst providing operation
// bindings bliver foerst fyldt ud til sidst ved at loebe alt igennem

// Resolve services

// Hvis det kun drejer sig om 2 felter saa maaske smide dem paa BeanSetup
// 

// I think we extends BeanSetup
public final class ServicableBean extends BeanSetup {

    @Nullable
    ServiceBindingSetup bindings; // a linked list as well

    /** Providing operations by the bean */
    @Nullable
    ProvidedService provisions; // is a linked list

    /**
     * @param owner
     * @param props
     */
    public ServicableBean(RealmSetup owner, BeanProps props) {
        super(owner, props);
    }

    public void resolve() {
        // Evt linker vi alle beans sammen...
        // Saa bliver det super let at lave en liste List<SBS> unresolved

        // basically find
        
//        for (ServiceBindingSetup sbs = bindings; sbs != null; sbs = sbs.resolveNext) {
//            // sbs.resolve
//        }

    }
}
