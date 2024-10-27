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
package app.packed.extension;

import app.packed.bean.scanning.BeanTrigger.OnExtensionServiceBeanTrigger;
import app.packed.context.Context;
import internal.app.packed.lifecycle.lifetime.runtime.PackedExtensionContext;

/**
 * All (container?) beans that are owned by an extension operates within an ExtensionContext.
 * <p>
 * An instance of this class is typically required when invoking operations.
 */
@OnExtensionServiceBeanTrigger(extension = BaseExtension.class)
public sealed interface ExtensionContext extends Context<BaseExtension> permits PackedExtensionContext {}


//A context or not. Hvis det er en context. Skal den med i alle metoder der ligesom siger hvad er hvad.
//Maaske er det kun med container lifetime, og maaske er det kun constructeren

//Vil mene det ikke er en Context. Syntes span fungere daarligt
//Saa maaske er Context skidt
//RuntimeExtensionHandle?
//ContainerContext

//Problemet med Context som jeg ser det. Er at man kan vaelge for beans.
//At man ikke skal vaere i ContextContext
//Maaske har vi negativ context (man kan fjerne det eksplicit via bean templates)

//Maaske er ContainerContext simpelthen bare ikke ContainerWide...