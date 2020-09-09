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

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.ExtensionConfiguration;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceRegistry;
import app.packed.service.ServiceSet;
import packed.internal.component.Region;
import packed.internal.component.RegionAssembly;
import packed.internal.component.Resolver;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.dependencies.DependencyManager;
import packed.internal.service.buildtime.export.ExportManager;
import packed.internal.service.buildtime.export.ExportedBuildEntry;
import packed.internal.service.buildtime.service.ServiceProvidingManager;
import packed.internal.service.runtime.PackedInjector;
import packed.internal.service.runtime.RuntimeEntry;
import packed.internal.util.LookupUtil;

/**
 * Since the logic for the service extension is quite complex. Especially with cross-container integration. We spread it
 * over multiple classes. With this class being the main one.
 */
public final class InjectionManager {

    /** A VarHandle that can access ServiceExtension#node. */
    private static final VarHandle VH_SERVICE_EXTENSION_NODE = LookupUtil.vhPrivateOther(MethodHandles.lookup(), ServiceExtension.class, "im",
            InjectionManager.class);

    /** Any children of the extension. */
    @Nullable
    ArrayList<InjectionManager> children;

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
    InjectionManager parent;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceProvidingManager provider;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, BuildEntry<?>> resolvedEntries = new LinkedHashMap<>();

    public final ContainerAssembly ca;

    /**
     * Creates a new builder.
     * 
     * @param context
     *            the context
     */
    public InjectionManager(ContainerAssembly ca, ExtensionConfiguration context) {
        this.ca = ca;
        region = ca.component.region;
        this.configuration = context;

    }

    final RegionAssembly region;

    public void addErrorMessage() {
        hasFailed = true;
    }

    // Ideen var lidt vi kaldte ind her foerend alle boernene er initialized
    public void buildBundle() {
        // We could actually have a desired state = Hosting (No linking just hosting)
        // But I do think it would be correct to say that the desired state is hosting...
        // Don't know if it would help anything...

        // No more services or components registered in this extension instance.
        // Let's run some quick tests before we start with linking..
        // We might even
        // System.out.println("First " + (parent == null));
    }

    public void buildTree(Resolver resolver) {
        if (parent == null) {
//            TreePrinter.print(this, n -> n.children, "", n -> n.context.containerPath().toString());
        }

        HashMap<Key<?>, BuildEntry<?>> resolvedServices = provider().resolve();
        resolvedServices.values().forEach(e -> resolvedEntries.put(requireNonNull(e.key()), e));

        if (exporter != null) {
            exporter.resolve();
        }

        if (hasFailed) {
            return;
        }

        for (Injectable i : resolver.sourceInjectables) {
            i.resolve(resolver);
        }

        dependencies().analyze(this);
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
            d = dependencies = new DependencyManager();
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

    public void link(InjectionManager child) {
        child.parent = this;
        if (children == null) {
            children = new ArrayList<>(5);
        }
        children.add(child);
    }

    //
    @Nullable
    public ServiceSet newExportedServiceSet() {
        return exports().exports();
    }

    public ServiceContract newServiceContract() {
        return ServiceContract.newContract(c -> {
            if (exporter != null) {
                for (ExportedBuildEntry<?> n : exporter) {
                    c.provides(n.key());
                }
            }
            dependencies().buildContract(c);
        });
    }

    public ServiceRegistry instantiateEverything(Region region, WireletPack wc) {
        LinkedHashMap<Key<?>, RuntimeEntry<?>> runtimeEntries = new LinkedHashMap<>();
        PackedInjector publicInjector = new PackedInjector(ca.component.configSite(), runtimeEntries);

        ServiceExtensionInstantiationContext con = new ServiceExtensionInstantiationContext(region);

        exports().forEach(e -> System.out.println("Exporting " + e.key));
        for (var e : exports()) {
            runtimeEntries.put(e.key, e.toRuntimeEntry(con));
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
    public static InjectionManager fromExtension(ServiceExtension extension) {
        return (InjectionManager) VH_SERVICE_EXTENSION_NODE.get(extension);
    }
}

//System.out.println("--------------- INIT PLAN ----------");
//for (ComponentFactoryBuildEntry<?> e : provider.mustInstantiate) {
//  System.out.println(e.newInstance);
//}
//System.out.println("-----------------");

//for (SourceHolder e : provider.mustInstantiate) {
//  if (e.regionIndex > -1) {
//      MethodHandle mh = e.reducedMha;
//      // System.out.println("INST " + mh.type().returnType());
//      Object instance;
//      try {
//          instance = mh.invoke(ns);
//      } catch (Throwable e1) {
//          throw ThrowableUtil.orUndeclared(e1);
//      }
//      con.region.store(e.regionIndex, instance);
//  }
//}
//for (var e : resolvedEntries.entrySet()) {
//  if (e.getKey() != null) { // only services... should be put there
//      // runtimeEntries.put(e.getKey(), e.getValue().toRuntimeEntry(con));
//  }
//}

// Instantiate all singletons...

// Vi bliver noedt til at instantiere dem in-order
// Saa vi skal have en orderet liste... af MH(ServiceNode)->Object
//for (BuildEntry<?> node : resolvedEntries.values()) {
//    // MethodHandle mh = node.toMH(con);
//    // System.out.println(mh);
//    if (node instanceof ComponentMethodHandleBuildEntry) {
//        ComponentMethodHandleBuildEntry<?> s = (ComponentMethodHandleBuildEntry<?>) node;
//        if (s.instantiationMode() == ServiceMode.CONSTANT) {
//            s.toRuntimeEntry(con).getInstance(null);
//        }
//    }
//}
///**
//* Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Inject}.
//* 
//* @param cc
//*            the configuration of the annotated component
//* @param group
//*            a inject group object
//*/
//public void onInjectGroup(SingletonConfiguration<?> cc, AtInjectHook group) {
//  // new Exception().printStackTrace();
//  // Hvis den er instans, Singlton Factory -> Saa skal det vel med i en liste
//  // Hvis det er en ManyProvide-> Saa skal vi jo egentlig bare gemme den til den bliver instantieret.
//  // Det skal ogsaa tilfoejes requires...
//  // Delt op i 2 dele...
//  // * Tilfoej det til requirements...
//  // * Scheduler at groupen skal kaldes senere ved inject...
//  for (AtInject ai : group.members) {
//      System.out.println(ai);
//  }
//}

//for (var e : specials.entrySet()) {
//System.out.println(e);
//
//// if (e.getKey().key().typeLiteral().rawType() == ExtensionInstantiationContext.class) {
//// // DOES not really work c is the instantiation context for the service
//// // not nessesarily for the one we should inject....
////
////Class<?> pipelineClass = e.getKey().key().typeLiteral().rawType();
////
////if (wc != null) {
////  instance = wc.getWireletOrPipeline(pipelineClass);
////  if (instance instanceof WireletPipelineContext) {
////      instance = ((WireletPipelineContext) instance).instance;
////  }
////  requireNonNull(instance);
////}
////if (instance == null) {
////  instance = Optional.empty();
////} else {
////  instance = e.getKey().wrapIfOptional(instance);
////}
////BuildEntry<?> be = e.getValue();
////con.transformers.put(be, new ConstantInjectorEntry<Object>(ConfigSite.UNKNOWN, (Key) be.key, instance));
//}