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

/**
 *
 */
// Ideen er egentligt, at man kan installere den via en service provider. Og saa bliver den automatisk instantieret hvis
// man bruger en annotering.

public abstract class ExtensionActivator<T extends Extension<T>> {

    protected void freeze() {

    }

}
// HookFeature (Factory for a Configurator, specified by user)
// HookFeatureConfigurator (A configurator that can be used at runtime
// HookFeatureDescriptor (Implementation details about features)
// HookFeatureContract - The public contract of the bundle

// Hvordan foer vi saa man ikke tager fejl af ServiceContract og tror det gaelder en service.
// ServicesContract

// For descriptors, vi kunne tage en method handle. som vi kan sende videre til noget bruger check kode.
// Vi wrapper den selvf saa brugeren ikke kan faa en brugbar reference.
