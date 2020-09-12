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
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.InjectionManager;

/**
 *
 */
// One resolver per region
public class Resolver {

    public final ArrayList<Injectable> constantServices = new ArrayList<>();

    final RegionAssembly ra;

    /** Components that contains constants that should be stored in a region. */
    final ArrayList<SourceAssembly> sourceConstants = new ArrayList<>();

    /** Everything that needs to resolved. */
    public final ArrayList<SourceAssembly> sourceInjectables = new ArrayList<>();

    public final ArrayList<Injectable> allInjectables = new ArrayList<>();

    public Resolver(RegionAssembly ra) {
        this.ra = requireNonNull(ra);
    }

    // Vi bliver noedt til at kalde ned recursivt saa vi kan finde raekkefolgen af service inst

    public DependencyProvider resolve(Injectable injectable, ServiceDependency dependency) {
        InjectionManager se = ra.configuration.container.im;
        BuildtimeService<?> e = se.resolvedEntries.get(dependency.key());

        se.dependencies().recordResolvedDependency(se, injectable, dependency, e, false);
        if (e == null) {
            return e;
        } else {
            // TODO call DependencyManager.recordResolvedDependency
            return e;
        }
    }

    public void resolveAll() {
        InjectionManager se = ra.configuration.container.im;

        se.buildTree(this);

        // check circles

        // create mhs

        // Last we find all source injectables that are registered as services
        // They will be instantiated as the last thing after all services.
    }
}

//if (Extension.class.isAssignableFrom(rawType)) {
//    if (entry instanceof ComponentMethodHandleBuildEntry) {
//        Optional<Class<? extends Extension>> op = ((ComponentMethodHandleBuildEntry) entry).component.extension();
//        if (op.isPresent()) {
//            Class<? extends Extension> cc = op.get();
//            if (cc == k.typeLiteral().type()) {
//                PackedExtensionConfiguration e = ((PackedExtensionConfiguration) node.context()).container().getContext(cc);
//                resolveTo = extensionEntries.computeIfAbsent(e.extensionType(),
//                        kk -> new RuntimeAdaptorEntry(node, new ConstantInjectorEntry<Extension>(ConfigSite.UNKNOWN,
//                                (Key) Key.of(e.extensionType()), e.instance())));
//
//            }
//        }
//    }
//}
