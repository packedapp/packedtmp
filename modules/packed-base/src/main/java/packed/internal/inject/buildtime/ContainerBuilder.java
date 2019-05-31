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
package packed.internal.inject.buildtime;

import app.packed.component.Install;
import app.packed.container.AnyBundle;
import app.packed.container.Container;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.InjectorExtension;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.InternalContainer;

/**
 * A builder of {@link Injector injectors}. Is both used via {@link InjectorConfigurator}.
 */
public class ContainerBuilder extends DefaultContainerConfiguration {

    public ContainerBuilder(InternalConfigurationSite configurationSite, @Nullable AnyBundle bundle, Wirelet... wirelets) {
        super(configurationSite, bundle, wirelets);
        use(InjectorExtension.class);
    }

    public Container build() {
        if (bundle != null) {
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                install(bundle);
            }
            bundle.doConfigure(this);
        }

        InternalContainer container = new InternalContainer(this, buildInjector());
        return container;
    }

    public Injector buildInjector() {
        finish();
        new DependencyGraph(this).instantiate();
        return use(InjectorExtension.class).ib.publicInjector;
    }
}
