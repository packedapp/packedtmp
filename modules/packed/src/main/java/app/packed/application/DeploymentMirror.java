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
package app.packed.application;

import java.util.Set;

import app.packed.container.AssemblyTreeMirror;
import app.packed.container.ContainerTreeMirror;
import app.packed.extension.Extension;
import app.packed.lifetime.ContainerLifetimeTreeMirror;

/**
 *
 */
// A deployment is basically a tree of applications.

// We may have multiple deployments

public interface DeploymentMirror {

    ApplicationTreeMirror applications();

    AssemblyTreeMirror assemblies();

    ContainerTreeMirror containers();

    /** {@return an unmodifiable {@link Set} view of every extension type that has been used in the deployment.} */
    Set<Class<? extends Extension<?>>> extensionTypes();

    ContainerLifetimeTreeMirror lifetimes();

    // Er som udgangspunkt "syntetisk" og alt information er ikke med

    ApplicationMirror hostApplication();
}
