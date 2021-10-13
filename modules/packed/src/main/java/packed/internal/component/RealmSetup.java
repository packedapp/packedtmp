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

import app.packed.application.ApplicationDescriptor.ApplicationDescriptorOutput;
import app.packed.base.Nullable;
import app.packed.bundle.BundleAssembly;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.Composer;
import app.packed.bundle.ComposerAction;
import app.packed.bundle.Wirelet;
import app.packed.extension.Extension;
import packed.internal.application.BuildSetup;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.bundle.BundleSetup;
import packed.internal.bundle.ExtensionSetup;
import packed.internal.bundle.PackedBundleDriver;
import packed.internal.util.LookupUtil;

/**
 * The internal configuration of realm.
 * <p>
 */
public final class RealmSetup {

    /** A handle that can invoke {@link BundleAssembly#doBuild()}. Is here because I have no better place to put it. */
    public static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BundleAssembly.class, "doBuild", void.class,
            BundleConfiguration.class);

    /** A handle that can invoke {@link BundleAssembly#doBuild()}. Is here because I have no better place to put it. */
    public static final MethodHandle MH_COMPOSER_DO_COMPOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Composer.class, "doBuild", void.class,
            BundleConfiguration.class, ComposerAction.class);

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
    private ArrayList<BundleSetup> rootContainers = new ArrayList<>(1);

    /**
     * Creates a new realm for an extension.
     * 
     * @param model
     *            the extension model to create a realm for
     * @parem extension the extension setup
     */
    public RealmSetup(ExtensionSetup extension) {
        this.realmType = this.extensionType = extension.extensionType;
        this.build = extension.bundle.application.build;
        this.root = null; // ??????
        // this.current = requireNonNull(extension);
    }

    public RealmSetup(PackedApplicationDriver<?> applicationDriver, ApplicationDescriptorOutput buildTarget, BundleAssembly  assembly, Wirelet[] wirelets) {
        this.realmType = assembly.getClass();
        this.build = new BuildSetup(applicationDriver, buildTarget, this, wirelets);
        this.root = build.application.container;
        this.extensionType = null;
        wireCommit(root);
    }

    public RealmSetup(PackedApplicationDriver<?> applicationDriver, ComposerAction<? /* extends Composer<?> */> composer, Wirelet[] wirelets) {
        this.realmType = composer.getClass();
        this.build = new BuildSetup(applicationDriver, ApplicationDescriptorOutput.INSTANCE, this, wirelets);
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
    private RealmSetup(RealmSetup existing, PackedBundleDriver driver, ComponentSetup linkTo, BundleAssembly  assembly, Wirelet[] wirelets) {
        this.realmType = assembly.getClass();
        this.build = existing.build;
        this.extensionType = null;
        this.root = new BundleSetup(build.application, this, build.application.container.lifetime, driver, linkTo, wirelets);
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
        for (BundleSetup c : rootContainers) {
            c.closeRealm();
        }
        assert root.name != null;
    }

    ComponentSetup current() {
        return current;
    }

    public RealmSetup link(PackedBundleDriver driver, ComponentSetup linkTo, BundleAssembly  assembly, Wirelet[] wirelets) {
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
     * @see BundleAssembly#lookup(Lookup)
     * @see Composer#lookup(Lookup)
     */
    public void setLookup(@Nullable Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        this.accessor = accessor().withLookup(lookup);
    }

    public void wireCommit(ComponentSetup component) {
        current = component;

        // TODO: Move to class I think
        if (component instanceof BundleSetup container) {
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
