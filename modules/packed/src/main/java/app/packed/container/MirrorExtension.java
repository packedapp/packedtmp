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

/**
 * This extension is mainly here as a kind of a marker extension. Indicating that somewhere in the application someone
 * has decided to reference a mirror. In which case the whole mirror shebang is available at runtime.
 * <p>
 * Maybe at some point we will support a compact mirror mode where each extension can keep a minimal set of information
 * that is needed at runtime.
 */
public class MirrorExtension extends Extension<MirrorExtension> {

    /**
     * Create a new mirror extension.
     * 
     * @param configuration
     *            an extension configuration object.
     */
       /* package-private */ MirrorExtension() {}
}

//DebugExtension???? Clearly indicates that it is not normal usage
// However, I don't see the need two extensions.
// Do we need a DevTools extension???

//https://docs.scala-lang.org/overviews/reflection/environment-universes-mirrors.html
//reflect = build time, introspect = runtime.. IDK
enum MirrorEnvironment { // ApplicationEnvironment???
    BUILD_TIME, RUN_TIME;
}
