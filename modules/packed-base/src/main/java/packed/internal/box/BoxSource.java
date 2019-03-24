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
package packed.internal.box;

/**
 *
 */
public enum BoxSource {

    INJECTOR_VIA_BUNDLE, INJECTOR_VIA_CONFIGURATOR;

    public boolean privateServices() {
        return this != INJECTOR_VIA_CONFIGURATOR;
    }

    public boolean unresolvedServicesAllowed() {
        return this == INJECTOR_VIA_BUNDLE;
    }

    // HOST
    // INJECTOR - via InjectorConfigurator
    // INJECTOR - via Bundle
    // INJECTOR - via existing injector
    // BundleContract - of
    // BundleDescriptor - of
    // App - via AppConfigurator
    // App - via Bundle

    // App - via existing app

    // En app kan godt have forskellige. F.eks. En Injector fra en InjectorConfigurator der wire en Injector fra en bundle
}
