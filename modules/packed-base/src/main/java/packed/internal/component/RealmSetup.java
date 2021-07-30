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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;

import app.packed.base.Nullable;
import app.packed.build.BuildTarget;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Composer;
import app.packed.component.ComposerConfigurator;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import packed.internal.application.BuildSetup;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.PackedContainerDriver;
import packed.internal.util.LookupUtil;

/**
 * The internal configuration of realm.
 * <p>
 */
public final class RealmSetup {

    /** A handle that can invoke {@link Assembly#doBuild()}. Is here because I have no better place to put it. */
    public static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "doBuild", void.class,
            ContainerConfiguration.class);

    /** A handle that can invoke {@link Assembly#doBuild()}. Is here because I have no better place to put it. */
    public static final MethodHandle MH_COMPOSER_DO_COMPOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Composer.class, "doCompose", void.class,
            ComponentConfiguration.class, ComposerConfigurator.class);

    /** The current module accessor, updated via {@link #setLookup(Lookup)} */
    private RealmAccessor accessor;

    /** The build this realm is a part of. */
    public final BuildSetup build;

    /** The current active component in the realm. */
    private ComponentSetup current;

    @Nullable
    public final Class<? extends Extension> extensionType;

    /** Whether or not this realm is closed. */
    private boolean isClosed;

    // Hmm. Realm er en ting. Men naar vi laeser extra hooks saa er det jo ikke paa denne type
    // Vi har faktisk 2 som jeg ser det.
    private final Class<?> realmType;

    /** The root component of this realm. */
    public final ComponentSetup root;

    /**
     * We keep track of all containers that are either the root container or have a parent that is not part of this realm.
     * When we close the realm we then run through this list and recursively close each container.
     */
    // Hmm burde kunne bruge traet istedet for
    private ArrayList<ContainerSetup> rootContainers = new ArrayList<>(1);

    /**
     * Creates a new realm for an extension.
     * 
     * @param model
     *            the extension model to create a realm for
     * @parem extension the extension setup
     */
    public RealmSetup(ExtensionSetup extension) {
        this.realmType = this.extensionType = extension.extensionType;
        this.build = extension.container.build;
        this.root = null; // ??????
        // this.current = requireNonNull(extension);
    }

    public RealmSetup(PackedApplicationDriver<?> applicationDriver, BuildTarget buildTarget, Assembly<?> assembly, Wirelet[] wirelets) {
        this.realmType = assembly.getClass();
        this.build = new BuildSetup(applicationDriver, this, buildTarget, wirelets);
        this.root = build.application.container;
        this.extensionType = null;
        wireCommit(root);
    }

    public RealmSetup(PackedApplicationDriver<?> applicationDriver, ComposerConfigurator<? /* extends Composer<?> */> composer, Wirelet[] wirelets) {
        this.realmType = composer.getClass();
        this.build = new BuildSetup(applicationDriver, this, BuildTarget.INSTANCE, wirelets);
        this.root = build.application.container;
        this.extensionType = null;
        wireCommit(root);
    }

    /**
     * Creates a new realm for an assembly.
     * 
     * @param assembly
     *            the assembly to create a realm for
     */
    private RealmSetup(RealmSetup existing, PackedContainerDriver<?> driver, ComponentSetup linkTo, Assembly<?> assembly, Wirelet[] wirelets) {
        this.realmType = assembly.getClass();
        this.build = existing.build;
        this.extensionType = null;
        this.root = driver.newComponent(build, this, build.application.container.lifetime, linkTo, wirelets);
    }

    public RealmAccessor accessor() {
        RealmAccessor r = accessor;
        if (r == null) {
            this.accessor = r = RealmAccessor.defaultFor(realmType);
        }
        return r;
    }

    public void checkOpen() {
        // Tror maaske hellere vi skal kalde newOperation
        if (isClosed) {
            throw new IllegalStateException();
        }
    }

    public void close() {
        if (current != null) {
            current.onWired();
            current = null;
        }
        isClosed = true;
        for (ContainerSetup c : rootContainers) {
            c.closeRealm();
        }
        assert root.name != null;
    }

    ComponentSetup current() {
        return current;
    }

    public RealmSetup link(PackedContainerDriver<?> driver, ComponentSetup linkTo, Assembly<?> assembly, Wirelet[] wirelets) {
        // Check that the realm this component is a part of is still open
        wirePrepare();
        // Create the new realm that should be used for linking
        return new RealmSetup(this, driver, linkTo, assembly, wirelets);
    }

    public void newOperation() {
        if (current != null) {
            current.onWired();
            current = null;
        }
    }

    /**
     * Returns the type that was used to create this realm.
     * 
     * @return the type that was used to create this realm.
     */
    public Class<?> realmType() {
        return realmType;
    }

    /**
     * @param lookup
     *            the lookup to use
     * @see Extension#lookup(Lookup)
     * @see Assembly#lookup(Lookup)
     * @see Composer#lookup(Lookup)
     */
    public void setLookup(@Nullable Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        this.accessor = accessor().withLookup(lookup);
    }

    public void wireCommit(ComponentSetup component) {
        current = component;
        
        // TODO: Move to class I think
        if (component instanceof ContainerSetup container) {
            if (container.containerParent == null || container.containerParent.realm != this) {
                rootContainers.add(container);
            }
        }
    }

    public void wirePrepare() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        // We need to finish the existing wiring before adding new
        if (current != null) {
            current.onWired();
            current = null;
        }
    }
}
