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
package sandbox.service.transform;

import app.packed.service.ServiceContract;

/**
 *
 */
// 1 eller 2 interfaces

// 1
//// bare simpler
//// Vi skal alligevel altid checke contracten efter hver operation der aendrer key-settet
///// export.provide(String.class, "ssdsd") <-- cannot have a requirement on String

// 2
//// Vi beregner altid exports foerns requirements ikke?

// Alle keys er unikke. Frae

// ServiceContractTransformer
public interface ServiceTransformer {

    // Certain operations are only valid from within a container
    boolean isInContainer(); //

    ServiceContract contract();

    // optional requirement into mandatory // addRequirement (will turn optional into requirement, or create one)
    // optional requirement with default value instead of empty if missing

    // decorate // What if we take something from the incoming container

    // rekey

    // map / replace
    //// exports

    // remove | retain
    //// exports
    //// optional requirements (// optional requirements into missing)

    // peek
    //// exports
    //// requirement
    //// optional requirement????

    // provide
    //// any requirement (removed afters)
    //// exports -> we simply add it to exports (added afterwards)
}
