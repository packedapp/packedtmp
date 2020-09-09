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
package packed.internal.service.buildtime.dependencies;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.inject.UnsatisfiableDependencyException;
import app.packed.introspection.ExecutableDescriptor;
import app.packed.introspection.MethodDescriptor;
import app.packed.introspection.ParameterDescriptor;
import app.packed.introspection.VariableDescriptor;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import packed.internal.container.PackedExtensionConfiguration;
import packed.internal.inject.resolvable.ResolvableFactory;
import packed.internal.inject.resolvable.ServiceDependency;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.service.ComponentMethodHandleBuildEntry;
import packed.internal.service.buildtime.service.RuntimeAdaptorEntry;
import packed.internal.service.runtime.ConstantInjectorEntry;

/**
 * This class manages everything to do with dependencies of components and service for an {@link ServiceExtension}.
 * 
 * @see ServiceExtension#manualRequirementsManagement()
 * @see ServiceExtension#require(Key...)
 * @see ServiceExtension#requireOptionally(Key...)
 */
public final class DependencyManager {

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildEntry<?>> detectCyclesFor;

    /**
     * Explicit requirements, typically added via {@link ServiceExtension#require(Key...)} or
     * {@link ServiceExtension#requireOptionally(Key...)}.
     */
    final ArrayList<DependencyRequirement> explicitRequirements = new ArrayList<>();

    private final IdentityHashMap<Class<? extends Extension>, BuildEntry<? extends Extension>> extensionEntries = new IdentityHashMap<>();

    /**
     * Whether or not the user must explicitly specify all required services. Via {@link ServiceExtension#require(Key...)},
     * {@link ServiceExtension#requireOptionally(Key...)} and add contract.
     * <p>
     * In previous versions we kept this information on a per node basis. However, it does not work properly with "foreign"
     * hook methods that make use of injection. Because they may not be processed until the bitter end, so it was only
     * really services registered via the provide methods that could make use of them.
     */
    boolean manualRequirementsManagement;

    /** A list of all dependencies that have not been resolved */
    private ArrayList<DependencyRequirement> missingDependencies;

    /** A set of all explicitly registered required service keys. */
    final HashSet<Key<?>> required = new HashSet<>();

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> requiredOptionally = new HashSet<>();

    public final LinkedHashMap<ServiceDependency, BuildEntry<?>> specials = new LinkedHashMap<>();

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<BuildEntry<?>, List<ServiceDependency>> unresolvedDependencies;

    /** Also used for descriptors. */
    public void analyze(ServiceExtensionNode node) {
        // If we do not export services into a bundle. We should be able to resolver much quicker..
        resolveAllDependencies(node);
        DependencyCycleDetector.dependencyCyclesDetect(detectCyclesFor);
    }

    /**
     * Helps build an {@link ServiceContract}.
     * 
     * @param builder
     *            the contract builder
     */
    public void buildContract(ServiceContract.Builder builder) {
        if (requiredOptionally != null) {
            requiredOptionally.forEach(k -> builder.optional(k));
        }
        if (required != null) {
            required.forEach(k -> builder.requires(k));
        }
    }

    public void checkForMissingDependencies(ServiceExtensionNode node) {
        boolean manualRequirementsManagement = node.dependencies != null && node.dependencies.manualRequirementsManagement;
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (DependencyRequirement e : missingDependencies) {
                if (!e.dependency.isOptional() && manualRequirementsManagement) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    // Has at least on dependency, so a source is present
                    List<ServiceDependency> dependencies = e.entry.source.dependencies;

                    if (dependencies.size() == 1) {
                        sb.append("single ");
                    }
                    ServiceDependency dependency = e.dependency;
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
                    throw new UnsatisfiableDependencyException(sb.toString());
                }
            }
        }
    }

    /**
     * Enables manual requirements management.
     * 
     * @see ServiceExtension#manualRequirementsManagement()
     */
    public void manualRequirementsManagement() {
        manualRequirementsManagement = true;
    }

    public void recordMissingDependency(BuildEntry<?> entry, ServiceDependency dependency, boolean fromParent) {

    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param entry
     * @param dependency
     */
    public void recordResolvedDependency(ServiceExtensionNode node, BuildEntry<?> entry, ServiceDependency dependency, @Nullable BuildEntry<?> resolvedTo,
            boolean fromParent) {
        requireNonNull(entry);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<DependencyRequirement> m = missingDependencies;
        if (m == null) {
            m = missingDependencies = new ArrayList<>();
        }
        m.add(new DependencyRequirement(dependency, entry));

        if (node.dependencies == null || !node.dependencies.manualRequirementsManagement) {
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
    public void require(ServiceDependency dependency, ConfigSite configSite) {
        explicitRequirements.add(new DependencyRequirement(dependency, configSite));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void resolveAllDependencies(ServiceExtensionNode node) {
        detectCyclesFor = new ArrayList<>();

        for (BuildEntry<?> entry : node.resolvedEntries.values()) {
            ResolvableFactory sh = entry.source;

            if (sh != null) {
                if (entry.hasUnresolvedDependencies()) {
                    detectCyclesFor.add(entry);
                    List<ServiceDependency> dependencies = sh.dependencies;
                    for (int i = sh.dependencyOffset; i < dependencies.size(); i++) {
                        ServiceDependency dependency = dependencies.get(i);
                        BuildEntry<?> resolveTo = node.resolvedEntries.get(dependency.key());
                        if (resolveTo == null) {
                            Key<?> k = dependency.key();
                            if (!k.hasQualifier()) {
                                Class<?> rawType = k.typeLiteral().rawType();
                                if (Wirelet.class.isAssignableFrom(rawType)) {
                                    // Fail if pipelined wirelet...
                                    BuildEntry<String> ben = new RuntimeAdaptorEntry<String>(node,
                                            new ConstantInjectorEntry<String>(ConfigSite.UNKNOWN, (Key) k, "Ignore"));
                                    resolveTo = ben;
                                    specials.put(dependency, ben);
                                }
                                if (Extension.class.isAssignableFrom(rawType)) {
                                    if (entry instanceof ComponentMethodHandleBuildEntry) {
                                        Optional<Class<? extends Extension>> op = ((ComponentMethodHandleBuildEntry) entry).component.extension();
                                        if (op.isPresent()) {
                                            Class<? extends Extension> cc = op.get();
                                            if (cc == k.typeLiteral().type()) {
                                                PackedExtensionConfiguration e = ((PackedExtensionConfiguration) node.context()).container().getContext(cc);
                                                resolveTo = extensionEntries.computeIfAbsent(e.extensionType(),
                                                        kk -> new RuntimeAdaptorEntry(node, new ConstantInjectorEntry<Extension>(ConfigSite.UNKNOWN,
                                                                (Key) Key.of(e.extensionType()), e.instance())));

                                            }
                                        }
                                    }
                                }

                            }
                        }
                        recordResolvedDependency(node, entry, dependency, resolveTo, false);
                        entry.source.resolvedDependencies[i] = resolveTo;
                    }
                }
            }
        }
        checkForMissingDependencies(node);
    }
}
//if (WireletPipeline.class.isAssignableFrom(rawType)) {
//WireletPipelineModel wpc = WireletPipelineModel.of((Class<? extends WireletPipeline<?, ?>>) rawType);
//if (wpc.extension() == null) {
//  // Fail if pipelined wirelet...
//  BuildEntry<String> ben = new RuntimeAdaptorEntry<String>(node,
//          new ConstantInjectorEntry<String>(ConfigSite.UNKNOWN, (Key) k, "Ignore"));
//  resolveTo = ben;
//  node.specials.put(dependency, ben);
//} else {
//  if (entry instanceof ComponentFactoryBuildEntry) {
//      Optional<Class<? extends Extension>> op = ((ComponentFactoryBuildEntry) entry).componentConfiguration.extension();
//      if (op.isPresent()) {
//          BuildEntry<String> ben = new RuntimeAdaptorEntry<String>(node,
//                  new ConstantInjectorEntry<String>(ConfigSite.UNKNOWN, (Key) k, "Ignore"));
//          resolveTo = ben;
//          node.specials.put(dependency, ben);
//      }
//  }
//}
//}