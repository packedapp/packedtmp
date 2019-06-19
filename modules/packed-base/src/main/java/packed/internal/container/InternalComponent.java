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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import app.packed.component.Component;
import app.packed.component.ComponentStream;

/**
 *
 */
final class InternalComponent extends AbstractComponent implements Component {

    /** The container in which this component lives. */
    final InternalContainer container;

    InternalComponent(InternalContainer container, DefaultComponentConfiguration configuration) {
        super(container, configuration);
        this.container = container;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Component> children() {
        return Collections.emptySet();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream() {
        return new InternalComponentStream(Stream.of(this));
    }
}
