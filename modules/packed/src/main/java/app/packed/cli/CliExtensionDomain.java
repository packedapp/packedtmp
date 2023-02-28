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

import java.util.LinkedHashMap;

import app.packed.extension.domain.ExtensionDomain;
import app.packed.extension.operation.OperationHandle;

/**
 * A cli domain is a domain where all cli commands are unique. Typically there is never more than one per application.
 */
class CliExtensionDomain extends ExtensionDomain<CliExtension> {

    /** All the commands within the domain. */
    final LinkedHashMap<String, CliC> commands = new LinkedHashMap<>();

    record CliC(CliCommand command, OperationHandle operation) {}
}
