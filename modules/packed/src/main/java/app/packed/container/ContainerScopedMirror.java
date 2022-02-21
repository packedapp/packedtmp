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
package app.packed.container;

import java.util.Set;

import app.packed.component.UserOrExtension;
import app.packed.extension.Extension;

/**
 *
 */
// CrossContainerScope (Det er vel der den er interessant

// A scope can be known under different id's in an application
public interface ContainerScopedMirror {

    Set<ContainerMirror> containers();
    
    Set<UserOrExtension> participants();
    
    Class<? extends Extension<?>> owner();
    
    ContainerScopedMirror aliasOf();
    
    Object id();

    // Rooted??? // Vi har 2 sub container der bruger CLI...
    /////////////// Kan ikke se at den skulle vaere rooted
    /// Jooo, men lur mig om den ikke er tilgaengelig i containere
    /// der ikke bruger den... tror ikke vi har ikke rooted ting

    // more than 1 container

    // more than 1 application
}

interface EventBusScopedMirror extends ContainerScopedMirror {}

interface ConfigScopedMirror extends ContainerScopedMirror {}

interface CliScopeMirror extends ContainerScopedMirror {}

interface MetricsRepositortyScopedMirror extends ContainerScopedMirror {}

interface WebServerScopedMirror extends ContainerScopedMirror {}

// ConfigNamespace -> A place where .path is unique
// CliNamespace 
// MetricsRepository

// ConfigSourceMirror