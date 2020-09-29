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
package app.packed.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The type of the field or the return type of the method must match the type of attribute. Packed makes no check if any
 * of the types are generic.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Opens(to = { OpenMode.METHOD_INVOKE, OpenMode.FIELD_GET })
// Giver det mening at kunne injecte attributer??? Not sure

// AttributeExpose syntes jeg... Vi har nok provide
// ExposeAttribuet
public @interface ExposeAttribute {

    /** The class that declares the attribute. */
    Class<?> from();

    /**
     * The name of the attribute that the annotated member provides a value for.
     * 
     * @return the name of the attribute that the member provides a value for
     */
    // Wow hvis vi nu tager metode/field navnet...
    // Goer at vi ikke kan bruge - og andre limiting ting...
    // Men maaske er det ok

    // Maaske attributer er camel case?
    // _ bliver translatet til -

    // Maaske ignorer vi case... //equalsIgnoreCase

    // Ignore alt paa naer letters

    // Eller maaske proever vi foerst at matche paa type...
    // Er der flere end et hit skal man specificere et navn...

    // Nahh det er sgu nok fint nok, at skulle specificere et navn...

    // Men maaske hellere field name en attribute name...
    // maaske begge dele og saa kan man vaelge
    // Eller leder man efter 1 match...
    String name(); // named()???
}

//declared by extension, vi kan vel bare tage @ExtensionMember value??

//Must map the exact type of attribute
//T, Optional<T>, Optional<Supplier<T>>

// Eneste grund til vi kalder den AttributeProvide istedet for provideAttribute
// er code completion...
// No concept of permanent anymore  The specified attribute must be {@link Attribute.Option#permanent() permanent}
