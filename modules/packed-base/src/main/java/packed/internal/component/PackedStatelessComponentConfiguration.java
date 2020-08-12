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

import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.StatelessConfiguration;

/**
 *
 */
public class PackedStatelessComponentConfiguration extends AbstractComponentConfiguration implements StatelessConfiguration {

    private final PackedStatelessComponentConfigurationContext context;

    public PackedStatelessComponentConfiguration(PackedStatelessComponentConfigurationContext context) {
        super(context);
        this.context = context;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> definition() {
        return context.componentModel.type();
    }

    /** {@inheritDoc} */
    @Override
    public StatelessConfiguration setDescription(String description) {
        context.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public StatelessConfiguration setName(String name) {
        context.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    protected String initializeNameDefaultName() {
        return context.initializeNameDefaultName();
    }
}
