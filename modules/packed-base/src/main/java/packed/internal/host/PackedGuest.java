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
package packed.internal.host;

import app.packed.component.ComponentType;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.component.AbstractComponent;

/**
 *
 */
public final class PackedGuest extends AbstractComponent {

    private final PackedGuestConfiguration configuration;

    /**
     * @param parent
     * @param configuration
     * @param ic
     */
    protected PackedGuest(AbstractComponent parent, PackedGuestConfiguration configuration, PackedArtifactInstantiationContext ic) {
        super(parent, configuration, ic);
        this.configuration = configuration;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentType type() {
        return configuration.delegate.type();
    }
}
