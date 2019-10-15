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
package packed.internal.service.build.service;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.service.Dependency;
import app.packed.service.Provide;
import app.packed.util.Nullable;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionNode;

/**
 *
 */
public abstract class AbstractComponentBuildEntry<T> extends BuildEntry<T> {

    /** The configuration of the component this build entry belongs to */
    public final ComponentConfiguration<?> componentConfiguration;

    /** The parent, if this node is the result of a member annotated with {@link Provide}. */
    @Nullable
    final AbstractComponentBuildEntry<?> declaringEntry;

    /**
     * @param serviceExtension
     * @param configSite
     * @param dependencies
     */
    public AbstractComponentBuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite, List<Dependency> dependencies,
            AbstractComponentBuildEntry<?> declaringEntry, ComponentConfiguration<?> componentConfiguration) {
        super(serviceExtension, configSite, dependencies);
        this.declaringEntry = declaringEntry;
        this.componentConfiguration = requireNonNull(componentConfiguration);
    }

}
