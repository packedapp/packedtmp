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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import packed.internal.inject.resolvable.DependencyProvider;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.inject.resolvable.ServiceDependency;

/**
 *
 */
// One resolver per region
public class Resolver {

    public final ArrayList<Injectable> nonServiceNonPrototypeInjectables = new ArrayList<>();

    final RegionAssembly ra;

    /** Components that contains constants that should be stored in a region. */
    public final ArrayList<SourceAssembly> sourceConstants = new ArrayList<>();

    /** Everything that needs to resolved. */
    public final ArrayList<Injectable> sourceInjectables = new ArrayList<>();

    public Resolver(RegionAssembly ra) {
        this.ra = requireNonNull(ra);
    }

    public DependencyProvider resolve(Injectable injectable, ServiceDependency dependency) {
        return null;
    }

    public void resolveAll() {
        for (Injectable i : sourceInjectables) {
            i.resolve(this);
        }
        // check circles

        // create mhs

        // Last we find all source injectables that are registered as services
        // They will be instantiated as the last thing after all services.
        for (Injectable i : sourceInjectables) {
            if (i.source().service == null) {
                nonServiceNonPrototypeInjectables.add(i);
            }
        }
    }

    public void instantiate() {

    }
}
