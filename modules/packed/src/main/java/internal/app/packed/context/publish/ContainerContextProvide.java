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
package internal.app.packed.context.publish;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanTrigger.AnnotatedFieldBeanTrigger;
import app.packed.bean.BeanTrigger.AnnotatedMethodBeanTrigger;
import app.packed.extension.BaseExtension;

/**
 * Ideen er at provide context fra en bean. Typisk container brug.
 *
 * Ved ikke hvordan vi ellers skal provide context info. Fra en non-lifetime root container. Hvor vi ikke tager argument
 * med
 * <p>
 * Tror ikke man baade kan specificere context arguments og bruge denne annotering.
 * <p>
 * Also
 */

// En maade er at alle metoder skal have et context object med som parameter
// En anden maade er at en metode provider et context object naar man forespoerger paa det
// (Containers only)

// Hvordan peger man paa bean'en? kan vel kun via en pouch

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedMethodBeanTrigger(extension = BaseExtension.class, allowInvoke = true)
@AnnotatedFieldBeanTrigger(extension = BaseExtension.class, allowGet = true)
// Skal returnere implementationen
public @interface ContainerContextProvide {
    // Det kan vi vel extracte fra metode/field signaturen
    // Class<? extends Context<?>> context(); // context.extension must be identical to owner
}
