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
package packed.internal.inject.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.StringJoiner;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.AssemblyException;
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceExtension;
import app.packed.introspection.ExecutableDescriptor;
import app.packed.introspection.MethodDescriptor;
import app.packed.introspection.ParameterDescriptor;
import app.packed.introspection.VariableDescriptor;
import packed.internal.inject.InjectionManager;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.service.assembly.ServiceAssembly;

/**
 * This class manages everything to do with dependencies of components and service for an {@link ServiceExtension}.
 * 
 * @see ServiceExtension#require(Key...)
 * @see ServiceExtension#requireOptionally(Key...)
 */
public final class ServiceDependencyManager {

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
    private ArrayList<ServiceDependencyRequirement> missingDependencies;

    /** A set of all explicitly registered required service keys. */
    final HashSet<Key<?>> required = new HashSet<>();

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> requiredOptionally = new HashSet<>();

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<ServiceAssembly<?>, List<DependencyDescriptor>> unresolvedDependencies;

    public void checkForMissingDependencies(InjectionManager node) {
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (ServiceDependencyRequirement e : missingDependencies) {
                if (!e.dependency.isOptional() && manualRequirementsManagement) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    // Has at least on dependency, so a source is present
                    List<DependencyDescriptor> dependencies = e.entry.dependencies;

                    if (dependencies.size() == 1) {
                        sb.append("single ");
                    }
                    DependencyDescriptor dependency = e.dependency;
                    sb.append("parameter on ");
                    if (dependency.variable() != null) {

                        ExecutableDescriptor ed = ((ParameterDescriptor) dependency.variable().get()).getDeclaringExecutable();
                        sb.append(ed.descriptorTypeName()).append(": ");
                        sb.append(ed.getDeclaringClass().getCanonicalName());
                        if (ed instanceof MethodDescriptor) {
                            sb.append("#").append(((MethodDescriptor) ed).getName());
                        }
                        sb.append("(");
                        if (dependencies.size() > 1) {
                            StringJoiner sj = new StringJoiner(", ");
                            for (int j = 0; j < dependencies.size(); j++) {
                                VariableDescriptor vd = dependency.variable().orElse(null);
                                int pindex = vd instanceof ParameterDescriptor ? ((ParameterDescriptor) vd).index() : -1;
                                if (j == pindex) {
                                    sj.add("-> " + dependency.key().toString() + " <-");
                                } else {
                                    sj.add(dependencies.get(j).key().typeLiteral().rawType().getSimpleName());
                                }
                            }
                            sb.append(sj.toString());
                        } else {
                            sb.append(dependency.key().toString());
                            sb.append(" ");
                            sb.append(dependency.variable().get().getName());
                        }
                        sb.append(")");
                    }
                    // b.root.requiredServicesMandatory.add(e.get)
                    // System.err.println(b.root.privateNodeMap.stream().map(e -> e.key()).collect(Collectors.toList()));
                    // UnresolvedVariableException
                    throw new AssemblyException(sb.toString());
                }
            }
        }
    }

    /**
     * Requires that all requirements are explicitly added via either {@link ServiceExtension#requireOptionally(Key...)},
     * {@link ServiceExtension#require(Key...)} or via implementing a contract.
     */
    // Kan vi lave denne generisk paa tvaers af extensions...
    // disableAutomaticRequirements()
    // Jeg taenker lidt det er enten eller. Vi kan ikke goere det per component.
    // Problemet er dem der f.eks. har metoder
    //// Vil det ikke altid bliver efterfuldt af en contract?????
    // Ser ingen grund til baade at support
    // ManualRequirements management..
    // AutoExport with regards to contract???

    // Drop Management?
    // Skal vi istedet for bare specificere en Contract????

    // exactContract(Contract, forceValidate)
    // supportContract() <-- can require less dependencies, any optional dependencies, export more dependencies
    // Contract driven og manual requirements management er 2 sider af samme sag
    // Contract driven er meget staerkere... og vi gider ikke supportere begge ting...

    public void manualRequirementsManagement() {
        manualRequirementsManagement = true;
    }

    public void recordMissingDependency(ServiceAssembly<?> entry, DependencyDescriptor dependency, boolean fromParent) {

    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param entry
     * @param dependency
     */
    public void recordResolvedDependency(InjectionManager im, Injectable entry, DependencyDescriptor dependency, @Nullable DependencyProvider resolvedTo,
            boolean fromParent) {
        requireNonNull(entry);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<ServiceDependencyRequirement> m = missingDependencies;
        if (m == null) {
            m = missingDependencies = new ArrayList<>();
        }
        m.add(new ServiceDependencyRequirement(dependency, entry));

        if (!manualRequirementsManagement) {
            if (dependency.isOptional()) {
                requiredOptionally.add(dependency.key());
            } else {
                required.add(dependency.key());
            }
        }
    }

    /**
     * Adds the specified dependency.
     * 
     * @param dependency
     *            the service dependency
     * @param configSite
     *            the config site
     * 
     * @see ServiceExtension#require(Key...)
     * @see ServiceExtension#requireOptionally(Key...)
     */
    public void require(DependencyDescriptor dependency, ConfigSite configSite) {
        explicitRequirements.add(new ServiceDependencyRequirement(dependency, configSite));
    }
}
