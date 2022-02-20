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
package app.packed.hooks.sandbox2;

import app.packed.extension.Extension;
import app.packed.hooks.BeanField;
import app.packed.inject.service.ServiceExtension;

/**
 *
 */
public interface Doo<E extends Extension<?>> {

    default void build(E extension) {}
}


// Nej record virker ikke... Fordi vi gerne vil have nogle flere felter, som vi selv saetter...
record myProc(BeanField f) implements Doo<ServiceExtension> {
    myProc {
        f.varHandle();
    }
};
