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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.function.Supplier;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.container.Assembly;
import app.packed.container.ContainerMirror;
import app.packed.container.DelegatingAssembly;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Nullable;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * A container build is responsible for building containers and applications.
 * <p>
 * This class and subclasses are a bit messy.
 *
 * @implNote This class is not sealed because some of the implementations is in a public package
 */
// Hvis vi ender med separate Container og Applications links metoder.
// Saa tror vi skal have en AbstractContainerContainerBuilder, AbstractContainerApplicationBuilder.
public abstract class PackedContainerBuilder {

    /** A handle that can invoke {@link BuildableAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_ASSEMBLY_BUILD = LookupUtil.findVirtual(MethodHandles.lookup(), Assembly.class, "build", AssemblySetup.class,
            PackedContainerBuilder.class);

    /** A supplier for creating application mirrors. */
    protected Supplier<? extends ApplicationMirror> applicationMirrorSupplier;

    /** A supplier for creating container mirrors. */
    protected Supplier<? extends ContainerMirror> containerMirrorSupplier;

    /** Delegating assemblies. Empty unless any {@link DelegatingAssembly} has been used. */
    public final ArrayList<Class<? extends DelegatingAssembly>> delegatingAssemblies = new ArrayList<>();

    /** Locals that the container is initialized with. */
    public final IdentityHashMap<PackedContainerLocal<?>, Object> locals = new IdentityHashMap<>();

    // I would like to time stuff. But I have no idea on how to do it reliable with all the laziness
    long creationNanos;

    String name;

    @Nullable
    public String nameFromWirelet;

    /** The parent of the new container. Or <code>null</code> if a root container. */
    @Nullable
    ContainerSetup parent;

    /** The template for the new container. */
    public final PackedContainerTemplate template;

    public final ArrayList<Wirelet> unconsumedWirelets = new ArrayList<>();

    protected PackedContainerBuilder(PackedContainerTemplate template) {
        this.template = requireNonNull(template, "template is null");
    }

    public boolean optionBuildApplicationLazy;

    public boolean optionBuildReusableImage;

    /**
     * Builds a new container using the specified assembly
     *
     * @param assembly
     *            assembly representing the new container
     * @param wirelets
     *            optional wirelets
     * @return the new container
     */
    public ContainerSetup buildNow(Assembly assembly) {
        requireNonNull(assembly, "assembly is null");

        // Calls Assembly.build(AbstractContainerBuilder)
        AssemblySetup as;
        try {
            as = (AssemblySetup) MH_ASSEMBLY_BUILD.invokeExact(assembly, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Return the container that was just built.
        return as.container;
    }

    public FutureApplicationSetup buildLazy(Assembly assembly) {
        throw new UnsupportedOperationException();
    }

    public abstract BuildGoal goal();

    public abstract LifetimeKind lifetimeKind();

    ContainerSetup newContainer(ApplicationSetup application, AssemblySetup assembly) {
        // All wirelets have been processed when we reaches here

        String nn = nameFromWirelet;

        // Set the name of the container if it was not set by a wirelet
        if (nn == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = name;
            if (n == null) {
                // TODO Should only be used on the root container in the assembly
                Class<? extends Assembly> source = assembly.assembly.getClass();
                if (Assembly.class.isAssignableFrom(source)) {
                    String nnn = source.getSimpleName();
                    if (nnn.length() > 8 && nnn.endsWith("Assembly")) {
                        nnn = nnn.substring(0, nnn.length() - 8);
                    }
                    if (nnn.length() > 0) {
                        // checkName, if not just App
                        // TODO need prefix
                        n = nnn;
                    }
                    if (nnn.length() == 0) {
                        n = "Assembly";
                    }
                } else {
                    n = "Unknown";
                }
            }
            nn = n;
        }

        String n = nn;
        if (parent != null) {
            HashMap<String, Object> c = parent.children;
            if (c.size() == 0) {
                c.put(n, this);
            } else {
                int counter = 1;
                while (c.putIfAbsent(n, this) != null) {
                    n = n + counter++; // maybe store some kind of map<ComponentSetup, LongCounter> in BuildSetup.. for those that want to test
                                       // adding 1
                    // million of the same component type
                }
            }
        }
        this.name = n;

        // Create the new container using this builder
        ContainerSetup container = new ContainerSetup(this, application, assembly);

        // BaseExtension is automatically used by every container
        ExtensionSetup.install(BaseExtension.class, container, null);

        return container;
    }

    // Er her fordi den skal fixes paa lang sigt
    ContainerSetup newContainer(AssemblySetup assembly) {
        if (this instanceof LeafContainerOrApplicationBuilder installer) {
            return installer.newContainer(installer.parent.application, assembly);
        } else {
            return new ApplicationSetup(this, assembly).container;
        }
    }

    // Hvad vi har bagefter er en liste af ikke internal wirelets

    // Wirelets from Template
    // Wirelets from delegating assembly
    // Wirelets from build site

    public void processBuildWirelet(Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        for (Wirelet w : wirelets) {
            processWireletOnBuild(w);
        }
    }

    void processWireletOnBuild(Wirelet w) {
        requireNonNull(w);
        if (w instanceof CompositeWirelet cw) {
            for (Wirelet ww : cw.wirelets) {
                processWireletOnBuild(ww);
            }
        } else if (w instanceof InternalBuildWirelet ibw) {
            ibw.onInstall(this);
        } else {
            unconsumedWirelets.add(w);
        }
    }

}
//@Deprecated
//public void processWirelets(Wirelet[] wirelets) {
//  requireNonNull(wirelets, "wirelets is null");
//  // Most of this method is just processing wirelets
//  Wirelet prefix = template.wirelet();
//
//  // We do not current set Container.WW
//  WireletWrapper ww = null;
//
//  if (wirelets.length > 0 || prefix != null) {
//      // If it is the root
//      Wirelet[] ws;
//      if (prefix == null) {
//          ws = CompositeWirelet.flattenAll(wirelets);
//      } else {
//          ws = CompositeWirelet.flatten2(prefix, Wirelet.combine(wirelets));
//      }
//
//      ww = new WireletWrapper(ws);
//
//      // May initialize the component's name, onWire, ect
//      // Do we need to consume internal wirelets???
//      // Maybe that is what they are...
//      int unconsumed = 0;
//      for (Wirelet w : ws) {
//          if (w instanceof InternalWirelet internal) {
//              internal.onInstall(this);
//          } else {
//              unconsumed++;
//          }
//      }
//      if (unconsumed > 0) {
//          ww.unconsumed = unconsumed;
//      }
//  }
////  this.wirelets = wirelets;
//}
// BootstrapAppContainerBuilder (does not take wirelets)

// RootContainerBuilder

// ExtensionContainerBuilder (Implements ContainerBuilder)

// LinkedContainerBuilder
