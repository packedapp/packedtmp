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

import app.packed.container.ComponentServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;

/**
 *
 */
public class DefaultComponentServiceConfiguration<T> extends DefaultServiceConfiguration<T> implements ComponentServiceConfiguration<T> {

    final ComponentBuildNode component;

    /**
     * @param node
     */
    public DefaultComponentServiceConfiguration(ComponentBuildNode cbn, BuildtimeServiceNode<T> node) {
        super(node);
        this.component = cbn;
        cbn.serviceNode = node;
    }

    /** {@inheritDoc} */
    @Override
    protected void onFreeze() {
        component.onFreeze();
        super.onFreeze();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getName() {
        return component.name;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> as(Class<? super T> key) {
        super.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> as(Key<? super T> key) {
        super.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> setDescription(String description) {
        checkConfigurable();
        super.setDescription(description);
        component.description = description;
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> setName(String name) {
        checkConfigurable();
        component.name = name;
        return this;
    }
}
