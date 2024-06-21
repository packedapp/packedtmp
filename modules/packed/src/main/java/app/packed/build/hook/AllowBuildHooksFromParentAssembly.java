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
package app.packed.build.hook;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tror det er her vi define en code security model
 */
// Er det udelukkede build hooks???
// Eller er der andet der bliver styret her.
// Umiddelbart taenker jeg kun build hooks,

// Not Metaable. Syntes kun man skal bruge denne annotering
// Annoteringen kan ikke transformeres (Maaske har vi en meta annotering for det??)

// Maaske er default STRICT?
// Maaske definere Assembly den her
// Maaske definere Consumer ANYTHING_GOES


// Tror maaske vi kan bruge den andre steder (Policy.PARANOID) Saa kan en extension sige hov, vi skal fx have SSL enabled IDK


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

// Replace by AssemblySecurityModel
public @interface AllowBuildHooksFromParentAssembly {

    WhatIsAllowed policy() default WhatIsAllowed.STRICT;

    // calling modules (module system is always checked), but these will also be allowed
    String[] additionalModules() default {};

    public enum WhatIsAllowed {

        // Strictly checked accordingly to the module system
        STRICT,

        // Parent is allowed to do anything. This means the assembly instances should be guarded
        PARENT_OWNER,

        // Anyone can place build hooks
        ANYTHING_GOES;
    }
}
