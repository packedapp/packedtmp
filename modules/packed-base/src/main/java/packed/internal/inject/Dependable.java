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
package packed.internal.inject;

import java.lang.module.ModuleDescriptor.Provides;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.component.PackedSingletonConfiguration;

/**
 *
 */
public interface Dependable extends WithDependencies {

    /**
     * Non-static methods or fields annotated with {@link Provides} may have a component that needs instantiating before the
     * member can provide services.
     * 
     * @return stuff
     */
    @Nullable
    // Hmm, en prototype @Provides kan jo ogsaa have det...
    // Saa maaske skal den ikke vaere paa dette interface
    // Nej det er vel mere Dependable....
    PackedSingletonConfiguration<?> declaringComponent();

    Key<?> key();
}
