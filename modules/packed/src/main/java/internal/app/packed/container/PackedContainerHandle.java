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
package internal.app.packed.container;

import java.util.List;
import java.util.Set;

import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerConfiguration;
import app.packed.extension.Extension;
import sandbox.extension.container.ContainerHandle;
import sandbox.extension.operation.OperationHandle;

/** Implementation of {@link ContainerHandle}. */
public record PackedContainerHandle<C extends ContainerConfiguration>(ContainerSetup container) implements ContainerHandle<C> {

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return container.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return container.isConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container.extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return container.isExtensionUsed(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public List<OperationHandle> lifetimeOperations() {
        return container.lifetimeOperations();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public C configuration() {
        return (C) container.configuration;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentKind componentKind() {
        return ComponentKind.CONTAINER;
    }
}
