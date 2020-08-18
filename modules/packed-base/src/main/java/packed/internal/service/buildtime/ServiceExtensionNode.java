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
package packed.internal.service.buildtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerDescriptor;
import app.packed.container.ExtensionConfiguration;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.component.wirelet.WireletPipelineContext;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.buildtime.dependencies.DependencyManager;
import packed.internal.service.buildtime.export.ExportManager;
import packed.internal.service.buildtime.export.ExportedBuildEntry;
import packed.internal.service.buildtime.service.ComponentFactoryBuildEntry;
import packed.internal.service.buildtime.service.ServiceProvidingManager;
import packed.internal.service.buildtime.wirelets.ServiceWireletPipeline;
import packed.internal.service.runtime.ConstantInjectorEntry;
import packed.internal.service.runtime.InjectorEntry;
import packed.internal.service.runtime.PackedInjector;
import packed.internal.util.LookupUtil;

/**
 * Since the logic for the service extension is quite complex. Especially with cross-container integration. We spread it
 * over multiple classes. With this class being the main one.
 */
public final class ServiceExtensionNode {

    /** A VarHandle that can access ServiceExtension#node. */
    private static final VarHandle VH_SERVICE_EXTENSION_NODE = LookupUtil.vhPrivateOther(MethodHandles.lookup(), ServiceExtension.class, "node",
            ServiceExtensionNode.class);

    /** Any children of the extension. */
    @Nullable
    ArrayList<ServiceExtensionNode> children;

    /** The configuration of the extension. */
    private final ExtensionConfiguration configuration;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public DependencyManager dependencies;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    private ExportManager exporter;

    private boolean hasFailed;

    /** Any parent of the node. */
    @Nullable
    ServiceExtensionNode parent;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceProvidingManager provider;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, BuildEntry<?>> resolvedEntries = new LinkedHashMap<>();

    public final LinkedHashMap<ServiceDependency, BuildEntry<?>> specials = new LinkedHashMap<>();

    /**
     * Creates a new builder.
     * 
     * @param context
     *            the context
     */
    public ServiceExtensionNode(ExtensionConfiguration context) {
        this.configuration = requireNonNull(context, "context is null");

    }

    public void addErrorMessage() {
        hasFailed = true;
    }

    public void buildBundle() {
        // We could actually have a desired state = Hosting (No linking just hosting)
        // But I do think it would be correct to say that the desired state is hosting...
        // Don't know if it would help anything...

        // No more services or components registered in this extension instance.
        // Let's run some quick tests before we start with linking..
        // We might even
        // System.out.println("First " + (parent == null));
    }

    public void buildDescriptor(ContainerDescriptor.Builder builder) {
        // need to have resolved successfully
        // TODO we should only have build entries here...
        for (BuildEntry<?> n : resolvedEntries.values()) {
            builder.addServiceDescriptor(((BuildEntry<?>) n).toDescriptor());
        }
    }

    public void buildTree() {
        if (parent == null) {
//            TreePrinter.print(this, n -> n.children, "", n -> n.context.containerPath().toString());
        }
        // System.out.println("Childre " + children);
        HashMap<Key<?>, BuildEntry<?>> resolvedServices = provider().resolve();
        resolvedServices.values().forEach(e -> resolvedEntries.put(requireNonNull(e.key()), e));

        if (exporter != null) {
            exporter.resolve();
        }

        if (hasFailed) {
            return;
        }
        dependencies().analyze();

    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    public final ExtensionConfiguration context() {
        return configuration;
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public DependencyManager dependencies() {
        DependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new DependencyManager(this);
        }
        return d;
    }

    /**
     * Returns the {@link ExportManager} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ExportManager exports() {
        ExportManager e = exporter;
        if (e == null) {
            e = exporter = new ExportManager(this);
        }
        return e;
    }

    public void link(ServiceExtensionNode child) {
        child.parent = this;
        if (children == null) {
            children = new ArrayList<>(5);
        }
        children.add(child);
    }

    public ServiceContract newServiceContract(ServiceWireletPipeline swp) {
        // requireNonNull(context);
        return ServiceContract.newContract(c -> {
            if (exporter != null) {
                for (ExportedBuildEntry<?> n : exporter) {
                    c.addProvides(n.key());
                }
            }
            dependencies().buildContract(c);
        });
    }

//    /**
//     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Inject}.
//     * 
//     * @param cc
//     *            the configuration of the annotated component
//     * @param group
//     *            a inject group object
//     */
//    public void onInjectGroup(SingletonConfiguration<?> cc, AtInjectHook group) {
//        // new Exception().printStackTrace();
//        // Hvis den er instans, Singlton Factory -> Saa skal det vel med i en liste
//        // Hvis det er en ManyProvide-> Saa skal vi jo egentlig bare gemme den til den bliver instantieret.
//        // Det skal ogsaa tilfoejes requires...
//        // Delt op i 2 dele...
//        // * Tilfoej det til requirements...
//        // * Scheduler at groupen skal kaldes senere ved inject...
//        for (AtInject ai : group.members) {
//            System.out.println(ai);
//        }
//    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public PackedInjector onInstantiate(WireletPack wc) {
        LinkedHashMap<Key<?>, InjectorEntry<?>> snm = new LinkedHashMap<>();
        PackedInjector publicInjector = new PackedInjector(context().containerConfigSite(), snm);

        ServiceExtensionInstantiationContext con = new ServiceExtensionInstantiationContext();
        for (var e : specials.entrySet()) {
            Object instance = null;

            // if (e.getKey().key().typeLiteral().rawType() == ExtensionInstantiationContext.class) {
            // // DOES not really work c is the instantiation context for the service
            // // not nessesarily for the one we should inject....
            //
            Class<?> pipelineClass = e.getKey().key().typeLiteral().rawType();

            if (wc != null) {
                instance = wc.getWireletOrPipeline(pipelineClass);
                if (instance instanceof WireletPipelineContext) {
                    instance = ((WireletPipelineContext) instance).instance;
                }
                requireNonNull(instance);
            }
            if (instance == null) {
                instance = Optional.empty();
            } else {
                instance = e.getKey().wrapIfOptional(instance);
            }
            BuildEntry<?> be = e.getValue();
            con.transformers.put(be, new ConstantInjectorEntry<Object>(ConfigSite.UNKNOWN, (Key) be.key, instance));
        }

        for (var e : resolvedEntries.entrySet()) {
            if (e.getKey() != null) { // only services... should be put there
                snm.put(e.getKey(), e.getValue().toRuntimeEntry(con));
            }
        }

        // Instantiate all singletons...
        for (BuildEntry<?> node : resolvedEntries.values()) {
            if (node instanceof ComponentFactoryBuildEntry) {
                ComponentFactoryBuildEntry<?> s = (ComponentFactoryBuildEntry<?>) node;
                if (s.instantiationMode() == ServiceMode.SINGLETON) {
                    s.toRuntimeEntry(con).getInstance(null);
                }
            }
        }

        return publicInjector;
    }

    public ServiceProvidingManager provider() {
        ServiceProvidingManager p = provider;
        if (p == null) {
            p = provider = new ServiceProvidingManager(this);
        }
        return p;
    }

    /**
     * Extracts the service node from a service extension.
     * 
     * @param extension
     *            the extension to extract from
     * @return the service node
     */
    public static ServiceExtensionNode fromExtension(ServiceExtension extension) {
        return (ServiceExtensionNode) VH_SERVICE_EXTENSION_NODE.get(extension);
    }
}
