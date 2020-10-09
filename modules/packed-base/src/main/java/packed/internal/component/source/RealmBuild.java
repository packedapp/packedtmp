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
package packed.internal.component.source;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedBuildContext;
import packed.internal.container.ExtensionModel;

/**
 *
 */
public final class RealmBuild {

    public final PackedBuildContext buildContext;

    /** The current lookup object, updated via {@link #lookup(Lookup)} */
    RealmLookup lookup;

    /** The model of this realm. */
    private final RealmModel model;

    @Nullable
    public ComponentNodeConfiguration rootComponent;

    public RealmBuild(PackedBuildContext buildContext, Class<?> type) {
        this.buildContext = requireNonNull(buildContext);
        this.lookup = this.model = RealmModel.of(type);
    }

    public void close() {
        rootComponent.onRealmClose(this);
    }

    public RealmBuild linkBundle(Bundle<?> bundle) {
        return new RealmBuild(buildContext, bundle.getClass());
    }

    /**
     * Creates a new realm for an extension.
     * 
     * @param model
     *            the extension model
     * @return a new realm
     */
    public RealmBuild linkExtension(ComponentNodeConfiguration compConf, ExtensionModel model) {
        RealmBuild realm = new RealmBuild(buildContext, model.type());
        realm.rootComponent = requireNonNull(compConf);
        return realm;
    }

    public void lookup(@Nullable Lookup lookup) {
        // If user specifies null, we use whatever
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? model : model.withLookup(lookup);
    }

    public Class<?> type() {
        return model.type;
    }
}
