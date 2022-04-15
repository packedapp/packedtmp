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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles.Lookup.ClassOption;

import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPointContext;

/**
 *
 */
public class ClassgenExtensionPoint extends ExtensionPoint<ClassgenExtension> {

    final ClassgenExtension classgen;

    final ExtensionPointContext context;

    ClassgenExtensionPoint(ClassgenExtension classgen, ExtensionPointContext context) {
        this.classgen = classgen;
        this.context = context;
    }

    public Lookup defineHiddenClass(Lookup caller, byte[] bytes, boolean initialize, ClassOption... options) throws IllegalAccessException {
        return classgen.defineHiddenClass(context.realm(), caller, bytes, initialize, options);
    }
}
