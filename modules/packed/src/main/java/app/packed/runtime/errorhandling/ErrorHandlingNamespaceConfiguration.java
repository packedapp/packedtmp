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
package app.packed.runtime.errorhandling;

import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceConfiguration;
import app.packed.namespace.NamespaceHandle;

/**
 *
 */
public final class ErrorHandlingNamespaceConfiguration extends NamespaceConfiguration<BaseExtension> {

    /**
     * @param namespace
     * @param extension
     * @param actor
     */
    protected ErrorHandlingNamespaceConfiguration(NamespaceHandle<BaseExtension, ?> namespace, BaseExtension extension, ComponentRealm actor) {
        super(namespace, extension, actor);
    }

}
