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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;

import app.packed.application.ApplicationDescriptor.ApplicationBuildType;
import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.Composer;
import app.packed.container.ComposerAction;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionRealmSetup;
import packed.internal.container.PackedContainerDriver;

/**
 * The internal configuration of realm.
 * <p>
 */
// BuildRealm???? Is this runtime at all???
public abstract sealed class RealmSetup permits AssemblyRealmSetup, ComposerRealmSetup, ExtensionRealmSetup {

    /** The current module accessor, updated via {@link #setLookup(Lookup)} */
    private RealmAccessor accessor;

    public final ApplicationSetup application;

    // Den giver kun mening for assemblies...
    /** The root component of this realm. */
    public final ContainerSetup container;

    /** The current active component in the realm. */
    private ComponentSetup current;

    /** Whether or not this realm is closed. */
    private boolean isClosed;

    // Hmm. Realm er en ting. Men naar vi laeser extra hooks saa er det jo ikke paa denne type
    // Vi har faktisk 2 som jeg ser det.
    private final Class<?> realmType;

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
    protected RealmSetup(ApplicationSetup application, Class<?> extensionType) {
        this.realmType = extensionType;
        this.application = application;
        this.container = null; // ??????
        // this.current = requireNonNull(extension);
    }

    protected RealmSetup(PackedApplicationDriver<?> applicationDriver, ApplicationBuildType buildTarget, Assembly assembly, Wirelet[] wirelets) {
        this.realmType = assembly.getClass();
        this.application = new ApplicationSetup(applicationDriver, buildTarget, this, wirelets);
        this.container = application.container;
        wireCommit(container);
    }

    public RealmSetup(PackedApplicationDriver<?> applicationDriver, ComposerAction<? /* extends Composer<?> */> composer, Wirelet[] wirelets) {
        this.realmType = composer.getClass();
        this.application = new ApplicationSetup(applicationDriver, ApplicationBuildType.INSTANCE, this, wirelets);
        this.container = application.container;
        wireCommit(container);
    }

    /**
     * Creates a new realm for an assembly.
     * 
     * @param assembly
     *            the assembly to create a realm for
     */
    protected RealmSetup(RealmSetup existing, PackedContainerDriver driver, ContainerSetup linkTo, Assembly assembly, Wirelet[] wirelets) {
        this.realmType = assembly.getClass();
        this.application = existing.application;
        this.container = new ContainerSetup(application, this, application.container.lifetime, driver, linkTo, wirelets);
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
        assert container.name != null;
    }

    ComponentSetup current() {
        return current;
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
            if (container.parent == null || container.parent.realm != this) {
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
