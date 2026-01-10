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

import java.util.function.Consumer;

import app.packed.component.ComponentRealm;
import app.packed.namespace.NamespaceBuildHook;
import app.packed.namespace.NamespaceConfiguration;

/**
 * A CLI namespace where all commands and general options are unique.
 */
public final class CliNamespaceConfiguration extends NamespaceConfiguration<CliExtension> {

    final CliNamespaceHandle handle;

    public CliNamespaceConfiguration(CliNamespaceHandle o, CliExtension cli, ComponentRealm actor) {
        super(o, cli, actor);
        this.handle = o;
    }

    public CliCommandConfiguration addCliCommand(Consumer<CliCommandContext> action) {

        // Vi skal jo nae
        // return extension().newFBean().addOp(SomeOpeationTemplate).intoNamespace(o).install().configuration();
        // exten
        throw new UnsupportedOperationException();
    }

    public static NamespaceBuildHook<CliNamespaceConfiguration> pushToNewApplication(boolean inheritExistingConfiguration, Consumer<? super CliNamespaceConfiguration> c) {
        throw new UnsupportedOperationException();
    }
}
