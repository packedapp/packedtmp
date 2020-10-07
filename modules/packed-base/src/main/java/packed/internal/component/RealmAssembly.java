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
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.CustomConfigurator;
import app.packed.inject.Factory;
import packed.internal.container.ExtensionModel;

/**
 *
 */
public final class RealmAssembly {

    ComponentNodeConfiguration compConf;

    /** The current component lookup object, updated via {@link #lookup(Lookup)} */
    // useFor future components...
    // We need to support some way to
    private SourceModelLookup lookup;

    /** A container model. */
    private final RealmModel model;

    final PackedAssemblyContext pac;
    private final Class<?> type;

    private RealmAssembly(PackedAssemblyContext pac, Class<?> type) {
        this.type = requireNonNull(type);
        this.lookup = this.model = RealmModel.of(type);
        this.pac = requireNonNull(pac);
    }

    public void close() {
        compConf.onRealmClose(this);
    }

    public SourceModel componentModelOf(Class<?> componentType) {
        return lookup.modelOf(componentType);
    }

    MethodHandle toMethodHandle(Factory<?> handle) {
        return lookup.toMethodHandle(handle);
    }

    public RealmAssembly linkBundle(Bundle<?> bundle) {
        return new RealmAssembly(pac, bundle.getClass());
    }

    /**
     * Creates a new realm for an extension.
     * 
     * @param model
     *            the extension model
     * @return a new realm
     */
    public RealmAssembly linkExtension(ComponentNodeConfiguration compConf, ExtensionModel model) {
        RealmAssembly realm = new RealmAssembly(pac, model.type());
        realm.compConf = requireNonNull(compConf);
        return realm;
    }

    public void lookup(@Nullable Lookup lookup) {
        // If user specifies null, we use whatever
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? model : model.withLookup(lookup);
    }

    public Class<?> type() {
        return type;
    }

    public static RealmAssembly fromBundle(PackedAssemblyContext pac, Bundle<?> bundle) {
        return new RealmAssembly(pac, bundle.getClass());
    }

    public static RealmAssembly fromConfigurator(PackedAssemblyContext pac, CustomConfigurator<?> consumer) {
        return new RealmAssembly(pac, consumer.getClass());
    }
}
