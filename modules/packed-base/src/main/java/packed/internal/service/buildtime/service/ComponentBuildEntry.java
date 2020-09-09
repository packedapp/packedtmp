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
package packed.internal.service.buildtime.service;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.inject.resolvable.ResolvableFactory;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionNode;

/**
 *
 */
public abstract class ComponentBuildEntry<T> extends BuildEntry<T> {

    /** The configuration of the component this build entry belongs to */
    public final ComponentNodeConfiguration component;

    /**
     * @param serviceExtension
     * @param configSite
     */
    public ComponentBuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite, ComponentNodeConfiguration component, ResolvableFactory sh) {
        super(serviceExtension, configSite, sh);
        this.component = requireNonNull(component);
    }
}
