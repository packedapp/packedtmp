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
package app.packed.sidecar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sidecar bootstrap methods are methods that are invoked exactly once for each sidecar type. If the annotation method
 * has a non-void return type. The return type is made available for dependency injection into the sidecar.
 *
 * The method target must be static and the declaring class must be annotated by one of the sidecar annotations.
 * <p>
 * If the annotated method has a non-void return type. The return value will be converted to a constant with a key
 * representing the return value (including any qualifier)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
//or SidecarBootstrap
public @interface BootstrapSidecar {

    // Cannot be injected into the sidecar... only other consts... Dont know if usefull
    boolean onlyAvailableForOtherBootstrap() default false;

    // If the bootstrap method returns null. No sidecar will be installed
    // Method must be annotated with Nullable
    boolean disableIfNull() default false;
}
//void -> validate stuff
//non-void -> consts

// supportere dependency injection af andre consts...
// Alle void metoder foerst....
// Bagefter 

// -Dapp.packed.base.DeterministicIntrospection true -> Methods sorted by name... (maybe extensions as well)

// Long term maybe just Bootstrap and then use perhaps in components??? Or Not
