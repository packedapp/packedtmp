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
package app.packed.classgen;

import java.util.stream.Stream;

import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionTree;

/**
 *
 */
public class ClassgenExtensionMirror extends ExtensionMirror<ClassgenExtension> {
    final ExtensionTree<ClassgenExtension> tree;

    ClassgenExtensionMirror(ExtensionTree<ClassgenExtension> tree) {
        this.tree = tree;
    }

    /** {@return a stream containing mirrors for every generated class.} */
    public Stream<GeneratedClassMirror> generatedClasses() {
        return tree.stream().flatMap(c -> c.generated.stream());
    }
}
