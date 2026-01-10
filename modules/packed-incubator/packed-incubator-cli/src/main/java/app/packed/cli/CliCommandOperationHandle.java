/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.cli;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;

/**
 *
 */
final class CliCommandOperationHandle extends OperationHandle<CliCommandConfiguration> {

    final List<String> names = new ArrayList<>();

    final CliNamespaceHandle namespace;

    /**
     * @param installer
     */
    CliCommandOperationHandle(OperationInstaller installer, CliNamespaceHandle namespace) {
        super(installer);
        this.namespace = requireNonNull(namespace);
    }

    /** {@inheritDoc} */
    @Override
    protected CliCommandConfiguration newOperationConfiguration() {
        return new CliCommandConfiguration(this);
    }

    /** {@inheritDoc} */
    @Override
    public CliCommandMirror newOperationMirror() {
        return new CliCommandMirror(this);
    }
}
