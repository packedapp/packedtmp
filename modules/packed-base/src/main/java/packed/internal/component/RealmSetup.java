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
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Composer;
import app.packed.container.Extension;
import packed.internal.component.source.RealmAccessor;
import packed.internal.container.ExtensionModel;
import packed.internal.util.LookupUtil;

/** The setup of an realm */
public final class RealmSetup {

    /** A handle that can invoke {@link Assembly#doBuild()}. Is here because I have no better place to put it. */
    public static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "doBuild", void.class,
            ComponentConfiguration.class);

    /** The current module accessor, updated via {@link #setLookup(Lookup)} */
    private RealmAccessor accessor;

    ComponentSetup current;

    private final Class<?> realmType;

    public boolean isClosed;

    /**
     * Creates a new realm for an assembly.
     * 
     * @param assembly
     *            the assembly to create a realm for
     */
    public RealmSetup(Assembly<?> assembly) {
        this.realmType = assembly.getClass();
    }

    /**
     * Creates a new realm for an composer consumer
     * 
     * @param composer
     *            the composer consumer
     */
    public RealmSetup(Consumer<? /* extends Composer<?> */> composer) {
        this.realmType = composer.getClass();
    }

    public void checkOpen() {
        if (isClosed) {
            throw new IllegalStateException();
        }
    }

    public void close(WireableComponentSetup root) {
        if (current != null) {
            current.fixCurrent();
        }
        isClosed = true;
        
        root.realmClose();
    }

    /**
     * Creates a new realm for an extension.
     * 
     * @param extension
     *            the extension to create a realm for
     */
    public RealmSetup(ExtensionModel extension) {
        this.realmType = extension.extensionClass();
    }

    public RealmAccessor accessor() {
        RealmAccessor r = accessor;
        if (r == null) {
            this.accessor = r = RealmAccessor.WithModuleInfo.of(realmType);
        }
        return r;
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
}
