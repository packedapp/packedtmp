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

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Nullable;

/**
 *
 */
public final class RealmBuild {

    /** The current lookup object, updated via {@link #lookup(Lookup)} */
    RealmLookup lookup;

    /** The model of this realm. */
    private final RealmModel model;

    public RealmBuild(Class<?> type) {
        this.lookup = this.model = RealmModel.of(type);
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
