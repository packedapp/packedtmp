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
package packed.internal.container;

import app.packed.component.Component;
import app.packed.container.InstantiationContext;
import app.packed.inject.Injector;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.DefaultInjector;

/** The default implementation of Container. */
final class DefaultContainer extends AbstractComponent implements Component {

    private final Injector injector;

    public DefaultContainer(@Nullable AbstractComponent parent, DefaultContainerConfiguration configuration, InstantiationContext ic) {
        super(parent, configuration, ic);
        Injector i = ic.get(configuration, DefaultInjector.class);
        if (i == null) {
            i = new DefaultInjector(configuration, new ServiceNodeMap());
        }
        this.injector = i;

    }

    public Injector injector() {
        return injector;
    }

    public <T> T use(Class<T> key) {
        return injector.use(key);
    }
}
