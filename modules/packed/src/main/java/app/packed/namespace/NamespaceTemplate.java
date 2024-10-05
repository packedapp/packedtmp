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
package app.packed.namespace;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.extension.Extension;
import app.packed.namespace.sandbox.BuildPermission;
import internal.app.packed.namespace.PackedNamespaceInstaller;
import internal.app.packed.namespace.PackedNamespaceTemplate;

/**
 * <p>
 * Unless otherwise specified the scope
 */

// A default domain is applicationWide...
public sealed interface NamespaceTemplate permits PackedNamespaceTemplate {

    // Er ikke sikker paa vi har behov for handler klassen...
    Class<? extends NamespaceHandle<?, ?>> handleClass();

    static NamespaceTemplate of(Class<? extends NamespaceHandle<?, ?>> handleClass, Consumer<? super Configurator> configure) {
        return new PackedNamespaceTemplate(handleClass);
    }

    interface Configurator {

        //Logging/Config er manuelt enabled
        // Forstaaet paa den maade at annoteringerne stadig kan bruges af extensions.
        // Men hvis namespaces ikke bliver enabled er det ikke aktivt...
        // IDK maaske er det okay.
        // Logging og Config er ihvertfald en del af BaseExtension... ellers biver det weird med used extensions
        // Maaske ogsaa metrics. Men metrics er > 1.0

        // IDK maaske er namespaces der stadig.. Men er bare ikke enabled
        Configurator manualEnabled();

        // Taenker maaske man skal kunne foersporge paa det.
        // Give me all domains of typeX

        // Magic Initializer for Extension + Authority

        // Igen man skal kunne iterere over dem
        // Or directly on the operator...
        // NamespaceConfiguration<?> Extension.newNamespace(Operator a, Class<? extends NamespaceConfiguration<?>> c, Authority
        // a)
        // void addConfigure(Function<Object, NamespaceConfiguration<?>> a);

        @SuppressWarnings("exports")
        default void addPermission(BuildPermission permissions) {
            // Default values??? for example, root only
        }

    }

   sealed interface Installer permits PackedNamespaceInstaller {

        // return value.getClass() from newHandle must match handleClass
        <E extends Extension<E>, H extends NamespaceHandle<E, ?>, C extends NamespaceConfiguration<E>> H install(
                Function<? super NamespaceTemplate.Installer, H> newHandle);
    }
}
