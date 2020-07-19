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
 *
 */

// Maaske lazy set istedet for at kraeve en constructor 
public abstract class Subtension {

    Class<? extends Extension> extension;

    protected final Class<? extends Extension> extension() {
        if (extension == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of " + getClass().getSimpleName());
        }
        return extension;
    }
}
