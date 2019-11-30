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
package packed.internal.host.test;

import app.packed.artifact.Host;
import app.packed.artifact.HostConfigurationContext;
import app.packed.lang.Key;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceConfiguration;

/**
 *
 */
public abstract class AbstractServiceableConfiguration<T> extends AbstractHostConfiguration implements ServiceConfiguration<T> {

    /**
     * @param wrapper
     */
    protected AbstractServiceableConfiguration(HostConfigurationContext wrapper) {
        super(wrapper);
    }

    @Override
    public AbstractServiceableConfiguration<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    @Override
    public AbstractServiceableConfiguration<T> setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public AbstractServiceableConfiguration<T> as(Class<? super T> key) {
        return this;
    }

    @Override
    public AbstractServiceableConfiguration<T> as(Key<? super T> key) {

        return this;
    }

    @Override
    public Key<?> getKey() {
        return Key.of(Host.class);
    }

    @Override
    public InstantiationMode instantiationMode() {
        return InstantiationMode.SINGLETON;
    }
}
