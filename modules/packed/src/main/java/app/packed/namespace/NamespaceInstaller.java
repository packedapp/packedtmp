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

import java.util.function.Function;

import app.packed.extension.Extension;
import internal.app.packed.namespace.PackedNamespaceInstaller;

public sealed interface NamespaceInstaller permits PackedNamespaceInstaller {

    // return value.getClass() from newHandle must match handleClass
    <E extends Extension<E>, H extends NamespaceHandle<E, ?>, C extends NamespaceConfiguration<E>> H install(
            Function<? super NamespaceInstaller, H> newHandle);
}