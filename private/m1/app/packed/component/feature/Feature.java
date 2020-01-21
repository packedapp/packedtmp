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
package app.packed.component.feature;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.component.ComponentPath;
import app.packed.component.FeatureKey;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.lifecycle.RunState;

interface CliFeature extends Feature {}

// Would also like to have an overview of resolved items...
// How everything fits together???

// Det er vel componenter....

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
///// Der er noget med Feature 
public interface Feature {

    FeatureKey<?> featureKey();

    // ConfigSite+FeatureType+ComponentPath?????
    // String featureId();

    ConfigSite configSite();

    /**
     * Returns the extension to which this feature belongs.
     * 
     * @return the extension to which this feature belongs
     */
    Class<? extends Extension> extension();

    ComponentPath path();
}
// Hav en Feature og en ManyFeature?????

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
