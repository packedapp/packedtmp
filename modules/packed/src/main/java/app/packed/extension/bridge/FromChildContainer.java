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
package app.packed.extension.bridge;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanExtension;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.container.ContainerExtension;
import app.packed.context.Context;
import app.packed.extension.ExtensionContext;

/**
 *
 */

@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented

// Alternativt, hvis man proever at injecte sig selv.. faar man en parent...
@BindingHook(extension = BeanExtension.class, requiresContext = ExtensionContext.class)
public @interface FromChildContainer {} // childExtension? instead

//Alternativt en ContainerLaucherContext? med context services.
//Saa kan vi ogsaa se praecis hvad der er tilgaengelig via OperationContext
//Maaske er det bare initialize with? IDK, er maaske ret at have seperat

interface ContainerLaunchContext extends Context<ContainerExtension> {}