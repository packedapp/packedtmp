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
package app.packed.artifact;

import app.packed.container.Bundle;

/**
 * A source for creating artifacts. For example, via {@link App#start(ArtifactSource, app.packed.container.Wirelet...)}.
 * <p>
 * There An source is used to create an artifact. Currently the following types of sources are supported:
 * 
 * Bundle
 * 
 * ArtifactSource -> Can be repeatable
 * 
 * This is typically either a subclass of {@link Bundle} or a pre assembled {@link ArtifactImage system image}.
 * <p>
 * TODO maybe list all the s
 * 
 * @apiNote In the future, if the Java language permits, {@link ArtifactSource} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface ArtifactSource {}
