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
package internal.app.packed.oldservice;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import app.packed.application.BuildException;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.service.ServiceExtension;
import internal.app.packed.oldservice.inject.DependencyNode;
import internal.app.packed.oldservice.inject.DependencyProducer;
import internal.app.packed.oldservice.inject.InternalDependency;

/**
 * This class manages everything to do with the requirements for a {@link ServiceExtension}.
 * 
 * @see ServiceExtension#require(Key...)
 * @see ServiceExtension#requireOptionally(Key...)
 */
public final class ServiceManagerRequirementsSetup {

    /**
     * Explicit requirements, typically added via {@link ServiceExtension#require(Key...)} or
     * {@link ServiceExtension#requireOptionally(Key...)}.
     */
    final ArrayList<ServiceDependencyRequirement> explicitRequirements = new ArrayList<>();

    /**
     * Whether or not the user must explicitly specify all required services. Via {@link ServiceExtension#require(Key...)},
     * {@link ServiceExtension#requireOptionally(Key...)} and add contract.
     * <p>
     * In previous versions we kept this information on a per node basis. However, it does not work properly with "foreign"
     * hook methods that make use of injection. Because they may not be processed until the bitter end, so it was only
     * really services registered via the provide methods that could make use of them.
     */
    // Skal jo erstattet af noget Contract...
    boolean manualRequirementsManagement;

    /** A list of all dependencies that have not been resolved */
    private ArrayList<ServiceDependencyRequirement> unresolvedRequirements;

    final LinkedHashMap<Key<?>, Requirement> requirements = new LinkedHashMap<>();

    public void checkForMissingDependencies() {
        if (unresolvedRequirements != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (ServiceDependencyRequirement e : unresolvedRequirements) {
                if (!e.dependency.isOptional() && manualRequirementsManagement) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    // Has at least on dependency, so a source is present
                    List<InternalDependency> dependencies = e.entry.dependencies;

                    if (dependencies.size() == 1) {
                        sb.append("single ");
                    }
                   // DependencyDescriptor dependency = e.dependency;
                    sb.append("parameter on ");
                  //  if (dependency.variable() != null) {

//                        Executable ed = ((PackedParameterDescriptor) dependency.variable().get()).unsafeExecutable();
//                        sb.append(ReflectionUtil.typeOf(ed)).append(": ");
//                        sb.append(ed.getDeclaringClass().getCanonicalName());
//                        if (ed instanceof Method met) {
//                            sb.append("#").append(met.getName());
//                        }
//                        sb.append("(");
//                        if (dependencies.size() > 1) {
//                            StringJoiner sj = new StringJoiner(", ");
//                            for (int j = 0; j < dependencies.size(); j++) {
//                                Variable vd = dependency.variable().orElse(null);
//                                int pindex = vd instanceof PackedParameterDescriptor ppd ? ppd.index() : -1;
//                                if (j == pindex) {
//                                    sj.add("-> " + dependency.key().toString() + " <-");
//                                } else {
//                                    sj.add(dependencies.get(j).key().rawType().getSimpleName());
//                                }
//                            }
//                            sb.append(sj.toString());
//                        } else {
//                            sb.append(dependency.key().toString());
//                            sb.append(" ");
//                            sb.append(dependency.variable().get().name());
//                        }
//                        sb.append(")");
                    //}
                    // b.root.requiredServicesMandatory.add(e.get)
                    // System.err.println(b.root.privateNodeMap.stream().map(e -> e.key()).collect(Collectors.toList()));
                    // UnresolvedVariableException
                    throw new BuildException(sb.toString());
                }
            }
        }
    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param entry
     * @param dependency
     */
    public void recordResolvedDependency(DependencyNode entry, int index, InternalDependency dependency, @Nullable DependencyProducer resolvedTo,
            boolean fromParent) {
        requireNonNull(entry);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        Key<?> key = dependency.key();
        ArrayList<ServiceDependencyRequirement> m = unresolvedRequirements;
        if (m == null) {
            m = unresolvedRequirements = new ArrayList<>();
        }
        m.add(new ServiceDependencyRequirement(dependency, entry));

        Requirement r = requirements.computeIfAbsent(key, Requirement::new);
        r.missingDependency(entry, index, dependency);
    }

    /**
     * Adds the specified explicit requirement.
     * 
     * @param key
     *            the key that is required
     * @param isOptional
     *            whether or not it is optional
     * 
     * @see ServiceExtension#require(Key...)
     * @see ServiceExtension#requireOptionally(Key...)
     */
    public void require(Key<?> key, boolean isOptional /* , ConfigSite configSite */) {
        // explicitRequirements.add(new ServiceDependencyRequirement(dependency, configSite));
    }

    record ServiceDependencyRequirement(InternalDependency dependency, DependencyNode entry) {}

    static class Requirement {

        // Always starts out as optional
        boolean isOptional = true;

        final Key<?> key;

        final ArrayList<FromInjectable> list = new ArrayList<>();

        Requirement(Key<?> key) {
            this.key = key;
        }

        void missingDependency(DependencyNode i, int dependencyIndex, InternalDependency d) {
            if (!d.isOptional()) {
                isOptional = false;
            }
            list.add(new FromInjectable(i, dependencyIndex, d));
        }

        record FromInjectable(DependencyNode i, int dependencyIndex, InternalDependency d) {}
    }

}
// exactContract(Contract, forceValidate)
// supportContract() <-- can require less dependencies, any optional dependencies, export more dependencies
// Contract driven og manual requirements management er 2 sider af samme sag
// Contract driven er meget staerkere... og vi gider ikke supportere begge ting...
