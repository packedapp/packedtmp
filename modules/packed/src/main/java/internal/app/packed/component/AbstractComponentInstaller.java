/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.component;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.operation.PackedOperationInstaller;

/**
 *
 */
public sealed abstract class AbstractComponentInstaller<C extends ComponentSetup, I extends AbstractComponentInstaller<C, I>>
        permits PackedApplicationInstaller, PackedContainerInstaller, PackedBeanInstaller, PackedOperationInstaller {

    C componentSetup;

    private Set<String> componentTags = Set.of();

    boolean isUsed;

    /** Initial bean locals for the new bean. */
    public final IdentityHashMap<PackedBuildLocal<?, ?>, Object> locals;

    protected AbstractComponentInstaller() {
        locals = new IdentityHashMap<>();
    }

    protected AbstractComponentInstaller(Map<? extends PackedBuildLocal<?, ?>, Object> existing) {
        locals = new IdentityHashMap<>(existing);
    }

    protected AbstractComponentInstaller(Set<String> componentTags, Map<? extends PackedBuildLocal<?, ?>, Object> existing) {
        locals = new IdentityHashMap<>(existing);
        this.componentTags = componentTags;
    }

    protected abstract ApplicationSetup application(C component);


    /**
     *
     */
    // checkUnused?
    public final void checkNotUsed() {
        if (isUsed) {
            throw new IllegalStateException("This installer has already been used to install a new component");
        }
    }

    @SuppressWarnings("unchecked")
    public final I componentTag(String... tags) {
        checkNotUsed();
        this.componentTags = ComponentTagHolder.copyAndAdd(componentTags, tags);
        return (I) this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final C install(C component) {
        this.componentSetup = component;
        this.isUsed = true;
        if (!componentTags.isEmpty()) {
            application(component).componentTags.map.put(component, Set.copyOf(componentTags));
        }

        // Transfer any locals that have been set in the template or installer
        if (!locals.isEmpty()) {
            locals.forEach((l, v) -> application(component).locals().set((PackedBuildLocal) l, component, v));
        }
        return component;
    }

    @SuppressWarnings("unchecked")
    public <T> I setLocal(PackedBuildLocal<?, T> local, T value) {
        checkNotUsed();
        this.locals.put( requireNonNull(local, "local is null"), requireNonNull(value, "value is null"));
        return (I) this;
    }

    public C toSetup() {
        C c = componentSetup;
        return c;
    }
}
