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

import app.packed.artifact.ArtifactSource;
import app.packed.component.Bundle;
import app.packed.component.CustomConfigurator;
import packed.internal.container.PackedExtensionConfiguration;

/**
 *
 */
public final class PackedRealm {

    private final Object source;

    private PackedRealm(Object source) {
        this.source = source;
    }

    public boolean isBundle() {
        return source instanceof Bundle;
    }

    public Bundle<?> asBundle() {
        return (Bundle<?>) source;
    }

    public Class<?> type() {
        return source.getClass();
    }

    /**
     * Creates a new realm for an extension.
     * 
     * @param pec
     *            the extension
     * @return a new realm
     */
    public static PackedRealm fromExtension(PackedExtensionConfiguration pec) {
        return new PackedRealm(pec);
    }

    public static PackedRealm fromBundle(Bundle<?> bundle) {
        return new PackedRealm(bundle);
    }

    public static PackedRealm fromAS(ArtifactSource source) {
        return new PackedRealm(source);
    }

    public static PackedRealm fromConfigurator(CustomConfigurator<?> consumer) {
        return new PackedRealm(consumer);
    }

}
