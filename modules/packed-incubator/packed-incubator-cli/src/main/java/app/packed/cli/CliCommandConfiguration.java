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
 * A configuration object for a cli command.
 */
public class CliCommandConfiguration extends OperationConfiguration {

    /** The handle for the command. */
    private final CliCommandOperationHandle handle;

    /**
     * @param handle
     */
    CliCommandConfiguration(CliCommandOperationHandle handle) {
        this.handle = requireNonNull(handle);
        super(handle);
    }

    // replace or add? I think replace
    public CliCommandConfiguration names(String... names) {
        checkIsConfigurable();
        List<String> ns = List.of(names);

        for (String name : names) {
            IO.println(name);
            // handle.namespace.commands
            // check not exists
        }

        handle.names.addAll(ns);
        // handle.names = List.of(names);
        // check namespace
        //
        return this;
    }
}
