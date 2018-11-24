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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import app.packed.inject.AbstractInjectorStage;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.InjectorImportStage;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * An abstract class for the various injector bind methods such as
 * {@link InjectorConfiguration#injectorBind(Class, AbstractInjectorStage...)} and
 * {@link InjectorConfiguration#injectorBind(Injector, InjectorImportStage...)}.
 */
public abstract class BindInjector {

    /** The configuration site where the injector was imported. */
    final InternalConfigurationSite configurationSite;

    /** The configuration of the injector. */
    final InternalInjectorConfiguration injectorConfiguration;

    public BindInjector(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
    }

    void importInto(InternalInjectorConfiguration configuration) {

    }
}
