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

/**
 *
 */

// Supports sharing across containers...

// Som udgangspunkt supportere vi kun class keys.
public interface CodegenCache<K> {
    Class<?> generate(K key, Classgen classgen);
}

// som alternativ

// static {
//  CodegenCache.installClassCache(MethodHandles.lookup(), "RepositoryGen");
//  CodegenCache.provideStatic(MethodHandles.lookup(), "RepositoryGen");
//}

// Den er saa tilgaengelig for alle extension klasser...


// ComponentData <---- statisk paa componenten ligesom classdata