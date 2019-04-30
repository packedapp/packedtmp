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

// InjectionMechanism
// Support for Provides, Hook, ... ect

// Ideen er at man laver saadan en her builder...
// Laver en BootstrapFactory. som saa hver gang laver en ny
// Configurator af en eller anden type..
/// Eller bliver brugt fra AnyBundle

// DisableConfigSites... Maaske en maade at disablee

// ignore packages for stack walking when finding config site
public class BootStrapFactoryBuilder {

    public BootstrapFactory build() {
        throw new UnsupportedOperationException();
    }

    protected void disableAnnotation(Class<?> annotation) {
        // f.eks. provides, class extending AnyBundle can still use it...
        // but subclasses of that class not???? hmmm
    }

    public BootStrapFactoryBuilder newBuilder() {
        throw new UnsupportedOperationException();
    }

    public BootStrapFactoryBuilder newCleanBuilder() {
        throw new UnsupportedOperationException();
    }
}
