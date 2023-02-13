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
package app.packed.extension.sandbox;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.AnnotatedBindingHook;

// I virkeligheden er det jo en slags multi-return fra en operation...
// Som bliver brugt til at populere en bean...

// OperationTemplate.intoBean
// Alternativt er det ikke en bean, men en record?


//Alternativt, hvis man proever at injecte sig selv.. faar man en parent...



// Maaske i virkeligheden 2...

/**
 *
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedBindingHook(extension = BaseExtension.class)
public @interface AncestorBean {} // childExtension? instead

//Alternativt en ContainerLaucherContext? med context services.
//Saa kan vi ogsaa se praecis hvad der er tilgaengelig via OperationContext
//Maaske er det bare initialize with? IDK, er maaske ret at have seperat
