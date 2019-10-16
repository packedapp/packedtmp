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
package packed.internal.service.build.dependencies;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import app.packed.config.ConfigSite;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionInstantiationContext;
import app.packed.reflect.ExecutableDescriptor;
import app.packed.reflect.MethodDescriptor;
import app.packed.reflect.ParameterDescriptor;
import app.packed.service.Dependency;
import app.packed.service.InjectionException;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.extension.ExtensionWireletPipelineModel;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.build.service.ComponentFactoryBuildEntry;
import packed.internal.service.build.service.RuntimeAdaptorEntry;
import packed.internal.service.run.SingletonInjectorEntry;

/**
 * This class manages everything to do with dependencies of components and service for an {@link ServiceExtension}.
 * 
 * @see ServiceExtension#manualRequirementsManagement()
 * @see ServiceExtension#require(Key)
 * @see ServiceExtension#requireOptionally(Key)
 */
public final class DependencyManager {

    /** The injector builder this manager belongs to. */
    private final ServiceExtensionNode node;

    /**
     * Explicit requirements, typically added via {@link ServiceExtension#require(Key)} or
     * {@link ServiceExtension#requireOptionally(Key)}.
     */
    final ArrayList<DependencyRequirement> explicitRequirements = new ArrayList<>();

    /**
     * Whether or not the user must explicitly specify all required services. Via {@link ServiceExtension#require(Key)},
     * {@link ServiceExtension#requireOptionally(Key)} and add contract.
     * <p>
     * In previous versions we kept this information on a per node basis. However, it does not work properly with "foreign"
     * hook methods that make use of injection. Because they may not be processed until the bitter end, so it was only
     * really services registered via the provide methods that could make use of them.
     */
    boolean manualRequirementsManagement;

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildEntry<?>> detectCyclesFor;

    /** A set of all explicitly registered required service keys. */
    final HashSet<Key<?>> required = new HashSet<>();

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> requiredOptionally = new HashSet<>();

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<BuildEntry<?>, List<Dependency>> unresolvedDependencies;

    /** A list of all dependencies that have not been resolved */
    private ArrayList<DependencyRequirement> missingDependencies;

    public DependencyManager(ServiceExtensionNode node) {
        this.node = requireNonNull(node);
    }

    /**
     * Enables manual requirements management.
     * 
     * @see ServiceExtension#manualRequirementsManagement()
     */
    public void manualRequirementsManagement() {
        manualRequirementsManagement = true;
    }

    /**
     * Adds the specified dependency.
     * 
     * @param dependency
     *            the service dependency
     * @param configSite
     *            the config site
     * 
     * @see ServiceExtension#require(Key)
     * @see ServiceExtension#requireOptionally(Key)
     */
    public void require(Dependency dependency, ConfigSite configSite) {
        explicitRequirements.add(new DependencyRequirement(dependency, configSite));
    }

    /** Also used for descriptors. */
    public void analyze() {
        // If we do not export services into a bundle. We should be able to resolver much quicker..
        resolveAllDependencies();
        DependencyCycleDetector.dependencyCyclesDetect(detectCyclesFor);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void resolveAllDependencies() {
        detectCyclesFor = new ArrayList<>();

        for (BuildEntry<?> entry : node.resolvedEntries.values()) {
            if (entry.hasUnresolvedDependencies()) {
                detectCyclesFor.add(entry);
                List<Dependency> dependencies = entry.dependencies;
                for (int i = entry.offset; i < dependencies.size(); i++) {
                    Dependency dependency = dependencies.get(i);
                    BuildEntry<?> resolveTo = node.resolvedEntries.get(dependency.key());
                    if (resolveTo == null) {
                        Key<?> k = dependency.key();
                        if (!k.hasQualifier() && Extension.class.isAssignableFrom(k.typeLiteral().rawType())) {
                            if (entry instanceof ComponentFactoryBuildEntry) {
                                Optional<Class<? extends Extension>> op = ((ComponentFactoryBuildEntry) entry).componentConfiguration.extension();
                                if (op.isPresent()) {
                                    Class<? extends Extension> cc = op.get();
                                    if (cc == k.typeLiteral().type()) {
                                        PackedExtensionContext e = ((PackedExtensionContext) node.context()).pcc.getExtension(cc);
                                        resolveTo = e.serviceEntry(node);
                                    }
                                }
                            }
                        }
                        if (!k.hasQualifier() && ExtensionInstantiationContext.class.isAssignableFrom(k.typeLiteral().rawType())) {
                            if (entry instanceof ComponentFactoryBuildEntry) {
                                Optional<Class<? extends Extension>> op = ((ComponentFactoryBuildEntry) entry).componentConfiguration.extension();
                                if (op.isPresent()) {
                                    BuildEntry<String> ben = new RuntimeAdaptorEntry<String>(node,
                                            new SingletonInjectorEntry<String>(ConfigSite.UNKNOWN, (Key) k, "foo", "Ignore"));
                                    resolveTo = ben;
                                    node.specials.put(k.typeLiteral().rawType(), ben);
                                }
                            }
                        }

                        if (!k.hasQualifier() && ExtensionWireletPipelineModel.class.isAssignableFrom(k.typeLiteral().rawType())) {
                            // Den virker ikke super godt med det optional....

                            if (entry instanceof ComponentFactoryBuildEntry) {
                                Optional<Class<? extends Extension>> op = ((ComponentFactoryBuildEntry) entry).componentConfiguration.extension();
                                if (op.isPresent()) {
                                    Class<? extends Extension> cc = op.get();
                                    if (cc == k.typeLiteral().type()) {
                                        // PackedExtensionContext e = ((PackedExtensionContext) node.context()).pcc.getExtension(cc);
                                        // resolveTo = e.serviceEntry(node);
                                    }
                                }
                            }
                        }
                    }
                    recordResolvedDependency(entry, dependency, resolveTo, false);
                    entry.resolvedDependencies[i] = resolveTo;
                }
            }
        }
        checkForMissingDependencies();
    }

    /**
     * Helps build an {@link ServiceContract}.
     * 
     * @param builder
     *            the contract builder
     */
    public void buildContract(ServiceContract.Builder builder) {
        if (requiredOptionally != null) {
            requiredOptionally.forEach(k -> builder.addOptional(k));
        }
        if (required != null) {
            required.forEach(k -> builder.addRequires(k));
        }
    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param entry
     * @param dependency
     */
    public void recordResolvedDependency(BuildEntry<?> entry, Dependency dependency, @Nullable BuildEntry<?> resolvedTo, boolean fromParent) {
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

    public void checkForMissingDependencies() {
        boolean manualRequirementsManagement = node.dependencies != null && node.dependencies.manualRequirementsManagement;
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (DependencyRequirement e : missingDependencies) {
                if (!e.dependency.isOptional() && manualRequirementsManagement) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    List<Dependency> dependencies = e.entry.dependencies;

                    if (dependencies.size() == 1) {
                        sb.append("single ");
                    }
                    Dependency dependency = e.dependency;
                    sb.append("parameter on ");
                    if (dependency.variable() != null) {

                        ExecutableDescriptor ed = ((ParameterDescriptor) dependency.variable().get()).declaringExecutable();
                        sb.append(ed.descriptorTypeName()).append(": ");
                        sb.append(ed.getDeclaringClass().getCanonicalName());
                        if (ed instanceof MethodDescriptor) {
                            sb.append("#").append(((MethodDescriptor) ed).getName());
                        }
                        sb.append("(");
                        if (dependencies.size() > 1) {
                            StringJoiner sj = new StringJoiner(", ");
                            for (int j = 0; j < dependencies.size(); j++) {
                                if (j == dependency.parameterIndex()) {
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
                    throw new InjectionException(sb.toString());
                }
            }
        }
    }

    public void recordMissingDependency(BuildEntry<?> entry, Dependency dependency, boolean fromParent) {

    }
}
