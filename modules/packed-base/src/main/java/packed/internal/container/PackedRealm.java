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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.CustomConfigurator;
import packed.internal.component.ComponentModel;
import packed.internal.inject.factory.FactoryHandle;

/**
 *
 */
public final class PackedRealm {

    private final Class<?> type;

    /** The current component lookup object, updated via {@link #lookup(Lookup)} */
    // useFor future components...
    // We need to support some way to
    private ComponentLookup lookup;

    /** A container model. */
    private final ContainerModel model;

    private PackedRealm(Class<?> type) {
        this.type = requireNonNull(type);
        this.lookup = this.model = ContainerModel.of(type);
    }

    public Class<?> type() {
        return type;
    }

    public void lookup(@Nullable Lookup lookup) {
        // If user specifies null, we use whatever
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? model : model.withLookup(lookup);
    }

    public MethodHandle fromFactoryHandle(FactoryHandle<?> handle) {
        return lookup.readable(handle).toMethodHandle();
    }

    public ComponentModel componentModelOf(Class<?> componentType) {
        return lookup.componentModelOf(componentType);
    }

    /**
     * Creates a new realm for an extension.
     * 
     * @param pec
     *            the extension
     * @return a new realm
     */
    public static PackedRealm fromExtension(PackedExtensionConfiguration pec) {
        return new PackedRealm(pec.extensionType());
    }

    public static PackedRealm fromBundle(Bundle<?> bundle) {
        return new PackedRealm(bundle.getClass());
    }

    public static PackedRealm fromConfigurator(CustomConfigurator<?> consumer) {
        return new PackedRealm(consumer.getClass());
    }
}