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
package app.packed.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 *
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
// Taenker det er paa raw type den tester.
//Hvis jeg depender paa ServiceRegistry...
//Skal det saa vaere inklusiv "private services"
// VariableInjectable?
// AutoService

// Taenker ikke man kan registere AutoServices...
// Det er maaske det der interessant..
public @interface AutoService {

    Class<?>[] bootstrap() default {};
    // Tror det kan blive lidt problematisk...
    // Hvis der er flere i et nedarvnings hiraki..
    // isaer med interfaces

    // Tror faktisk

    // boolean includeSubclasses(); <-- only works on abstract classes???

    // Hvis den er generic.. Sa

    // Har vi behov for bootstrap...
    // Altsaa man kan jo bruge den til at lave en klasse
    // Repo<FooBat>, men typisk taenker jeg man bruger en
    // specifik metode til at lave saadan en

    @Target({ ElementType.METHOD })
    @Retention(RUNTIME)
    @Documented
    @interface Maker {

    }

    abstract class Bootstrap {

        // Kunne vaere rart maaske at kunne tilfoeje noget der havde lifecycles...
        // Men taenker at det maa kalde noget med livscykles i foerste omgang..
        // Altsaa det wrapper f.eks. en FileSystemManager og et id...
        // F.eks. et fil system...
    }
}
// ServiceRegistry
// ServiceLocator <--- Maaske ikke...
