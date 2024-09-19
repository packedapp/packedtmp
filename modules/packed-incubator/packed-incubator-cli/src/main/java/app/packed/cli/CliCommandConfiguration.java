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
package app.packed.cli;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.operation.OperationConfiguration;

/**
 *
 */
public class CliCommandConfiguration extends OperationConfiguration {

    private final CliCommandHandle handle;

    /**
     * @param handle
     */
    CliCommandConfiguration(CliCommandHandle handle) {
        super(handle);
        this.handle = requireNonNull(handle);
    }

    public CliCommandConfiguration names(String... names) {
        checkIsConfigurable();
        handle.names.addAll(List.of(names));
        // handle.names = List.of(names);
        // check namespace
        //
        throw new UnsupportedOperationException();
    }
}
