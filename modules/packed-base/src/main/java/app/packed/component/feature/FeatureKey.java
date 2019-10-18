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
package app.packed.component.feature;

import static java.util.Objects.requireNonNull;

import app.packed.container.Extension;

/**
 *
 */
/// How many keys are we talking about???
// Could have a configure Object.
public abstract class FeatureKey<T> {

    private final Class<? extends Extension> extensionType;

    /**
     * @param extensionType
     *            the extension type this key
     */
    protected FeatureKey(Class<? extends Extension> extensionType) {
        this.extensionType = requireNonNull(extensionType, "extensionType is null");
    }

    public Class<? extends Extension> extensionType() {
        return extensionType;
    }

    // isRuntime, isBuildTime
    // Reference -> ServiceDescriptor#EXPORTS. ServiceDescriptor#PROVIDES

}
// Key design
// The actual feature class is the key...
// The key is an abstact class -> We can verify the actual type
// The key is an instance and we use an Identity HashMap

/// new Class ->
/// We can have den som en methode parameter()
/// calcFeatures(ServiceDescriptor.SERVICE_PROVISISIONS)
// Hjaelper en del naar vi ogsaa skal koere runtime...