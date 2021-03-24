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
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Composer;
import app.packed.container.Extension;
import packed.internal.component.source.ModuleAccessor;
import packed.internal.container.ExtensionModel;

/** The setup of an realm */
public final class RealmSetup {

    /** The current module accessor, updated via {@link #setLookup(Lookup)} */
    private ModuleAccessor accessor;

    ComponentSetup current;

    private final Class<?> realmType;

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

    /**
     * Creates a new realm for an extension.
     * 
     * @param extension
     *            the extension to create a realm for
     */
    public RealmSetup(ExtensionModel extension) {
        this.realmType = extension.extensionClass();
    }

    public ModuleAccessor accessor() {
        ModuleAccessor r = accessor;
        if (r == null) {
            this.accessor = r = ModuleAccessor.WithModuleInfo.of(realmType);
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