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
package app.packed.lang.invoke;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * <p>
 * Opens usage sites of the annotated annotation for access by #{@link OpensFor#modules()}.
 * <p>
 * This annotation never applies transitively.
 * 
 */
// If this annotation is not used, the annotation can only be used in its declaring module.
// Or any other modules that are open to it.
@Documented
@Retention(RUNTIME)
@Target({ ANNOTATION_TYPE, TYPE })
// Kan vi used OpensFor(modules = "*" ) <- on Abstract classes???? To say, yeah just do what you want...
// Do we allow it on members???? To say, you now what. Anyone do what you want with this method...

// Ahh hvad med transitive.... Saa bliver vi vel noedt til at specificere module navnet...
// Hvis folk saa omnavngiver modulet... Saa odelaegger det alle applicationer... der refererer til det
// Tror sgu vi bliver noedt til at se man ogsaa arver det...
// Folk bruger jo vores sikkerheds application....
// Spoergmaalet er om folk ville se det alligevel hvis det bliv aendret..
// Maaske skal vi have en @MetaAnnotation(inheritAccess = true) or
// @MetaAnnotation(inheritAccess = Field_Get)... IDK...
// Hvad hvis vi har flere annotationer??? f.eks. @RunOnStart @RunOnStop...
// Det fungere ikke.. taenker ogsaa det ville vaere daarligt design
// -- Men vi er jo stadig stuck i en situation hvor meta annoteringer ikke vil virke laengere
// Hvis man f.eks. tilfojere FIELD_SET og folk bruger den transitive annotering... IDK

// AllowAccess? Altså det eneste er om vi vil bruge det senere
public @interface OpensFor {

    /**
     * Returns the module(s) that are given access. The default value is {@literal "."} which represents the module of the
     * annotated target type.
     * 
     * @return the module(s) that are given access
     */
    String[] modules() default { "." };

    /**
     * The type of access rights that are needed.
     * 
     * @return the type of access rights that are needed
     */
    AccessType[] value();
}
