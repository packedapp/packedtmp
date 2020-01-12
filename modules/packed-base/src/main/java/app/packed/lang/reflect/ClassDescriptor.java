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
package app.packed.lang.reflect;

import java.lang.invoke.MethodHandles.Lookup;

/**
 *
 */
// http://cr.openjdk.java.net/~mcimadamore/reflection-manifesto.html
// http://cr.openjdk.java.net/~mcimadamore/x-reflection/index.html?valhalla/reflect/runtime/RuntimeMirror.Kind.html
public interface ClassDescriptor<T> extends MetaAnnotatedElement {
    Class<?> reflect();

    // Den gamle hook..
    // Naah lav den som statisk function taenker jeg...
    T analyze(Lookup caller, Class<?> target);
}
