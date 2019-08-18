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
package packed.internal.container.extension;

import java.util.IdentityHashMap;

import app.packed.container.extension.Extension;

/**
 *
 */
public class DefCon {
    // Det her med at finde ud af hvordan extensions er relateret....

    static final Module m = DefCon.class.getModule();

    // Eneste problem med Base Extensions er at vi skal predetermind en order
    // Og denne order skal maaske ogsaa vise sig i cc.extension()
    // Eftersom det er et view. Kraever det en lille smule extra kode...

    IdentityHashMap<Class<?>, Extension> baseExtensions;

    IdentityHashMap<Class<?>, Extension> externalExtensions;

    public <T> T use(Class<T> t) {
        if (t.getModule() == m) {
            // get From baseExtensions
        } else {
            // getFrom External Dependencies..
        }
        // We can actually remove all modules that do not implement #configure()

        return null;
        // ideen er selvfolgelig
    }
}
