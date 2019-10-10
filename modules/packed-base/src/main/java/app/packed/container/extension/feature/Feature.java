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
package app.packed.container.extension.feature;

import java.util.Optional;

import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.container.extension.Extension;
import app.packed.lifecycle.RunState;
import app.packed.util.Key;

interface CliFeature extends Feature {}

// Would also like to have an overview of resolved items...
// How everything fits together???
interface DependantFeature extends /* ConfiguritonTimeFeature */ Feature {
    // Map<Key, List<ConfigSite>> <--- dependency = Parameter....

    //// Throws ISE exception... if wrong time??
    // Map<Key, ProvideFeature> resolved();
    // Set<Key> unresolved();
}

/**
 *
 */
// Hoerer til i .extension
// FeatureDescriptor
public interface Feature {

    // ConfigSite+FeatureType+ComponentPath?????
    // String featureId();

    ConfigSite configSite();

    // If there is no direct link to the component.
    Class<? extends Extension> extension();

    ComponentPath path();
}

interface LifecycleFeature extends Feature {
    RunState state();
    // Interference with other lifecycle features
}
//// Component vs Container vs Artifact???

interface MainFeature extends Feature {}

// Bundle...

interface ProvideFeature extends Feature {
    // Provide could also be a transformed service....
    Optional<Key<?>> exportedAs();

    Key<?> key();
}

// Features vs Contract....
/// Contract listeer alt hvad en container skal bruge...
// Og dens ene Main eller CliPoint...

/// Man kan ikke faa contrakten for en component...

/// Contracts -> From A Bundle....
/// Features -> From a Component/ComponentConfiguration/....

/// Ville dog stadig godt have API Status med i vores kontrakter.....
