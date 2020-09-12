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

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import packed.internal.container.ExtensionModel;
import packed.internal.container.PackedRealm;

/**
 *
 */
public abstract class OldPackedComponentDriver<C> implements ComponentDriver<C> {

    final int modifiers;

    protected OldPackedComponentDriver(ComponentModifier... properties) {
        this.modifiers = PackedComponentModifierSet.intOf(properties);
        if (modifiers == 0) {
            throw new IllegalStateException();
        }
    }

    public String defaultName(PackedRealm realm) {
        if (this instanceof PackedComponentDriver) {
            PackedComponentDriver<?> pcd = (PackedComponentDriver<?>) this;
            if (pcd.source instanceof ExtensionModel) {
                return ((ExtensionModel) pcd.source).defaultComponentName;
            }
        }
        if (modifiers().isContainer()) {
            // I think try and move some of this to ComponentNameWirelet
            @Nullable
            Class<?> source = realm.type();
            if (Bundle.class.isAssignableFrom(source)) {
                String nnn = source.getSimpleName();
                if (nnn.length() > 6 && nnn.endsWith("Bundle")) {
                    nnn = nnn.substring(0, nnn.length() - 6);
                }
                if (nnn.length() > 0) {
                    // checkName, if not just App
                    // TODO need prefix
                    return nnn;
                }
                if (nnn.length() == 0) {
                    return "Container";
                }
            }
            // TODO think it should be named Artifact type, for example, app, injector, ...
        }
        return "Unknown";
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    public abstract C toConfiguration(ComponentConfigurationContext cnc);
}
