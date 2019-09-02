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
package packed.internal.access;

import app.packed.inject.Factory;
import app.packed.inject.InjectionExtension;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.factoryhandle.FunctionHandle;

/** A support class for calling package private methods in the app.packed.inject package. */
public interface AppPackedInjectAccess {

    /**
     * Extracts the internal factory from the specified factory
     * 
     * @param <T>
     *            the type of elements the factory produces
     * @param factory
     *            the factory to extract from
     * @return the internal factory
     */
    <T> FunctionHandle<T> toInternalFunction(Factory<T> factory);

    InjectorBuilder getBuilder(InjectionExtension ie);
}
