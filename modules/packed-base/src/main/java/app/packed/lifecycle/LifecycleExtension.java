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
package app.packed.lifecycle;

import app.packed.bundle.BundleLink;
import app.packed.container.Extension;
import app.packed.util.Nullable;

/**
 * An extension
 */
public final class LifecycleExtension extends Extension<LifecycleExtension> {

    @Override
    protected void onWireChild(@Nullable LifecycleExtension child, BundleLink link) {

    }

    @Override
    protected void onWireParent(@Nullable LifecycleExtension parent, BundleLink link) {
        if (parent == null) {
            throw new /* WiringException */ RuntimeException("Cannot wiring to a bundle that does not support lifecycle...");
        }
    }

    // protected void supporting

    // void addAlias(Class<?>

    // Perhaps adding some custom annotations

    // ClassValue<Feature> -> IdentityHashMap<Feature, CachedConfiguration>>
}
