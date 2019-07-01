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
package packed.internal.componentcache2;

import java.util.IdentityHashMap;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.ContainerExtension;
import packed.internal.inject2.ProvideHookProcessor;

/**
 *
 */
public final class Injector2Extension extends ContainerExtension<Injector2Extension> {

    /** Creates a new injector extension. */
    Injector2Extension() {}

    public void provide(Object o) {
        // Must make sure we never recursively call into this method
        NewService ns = new NewService();

        ComponentExtension ce = configuration().use(ComponentExtension.class);
        current = ns;
        try {
            ce.install(o);
        } finally {
            current = null;
        }

        System.out.println(ns);
    }

    public void provide2(Object o) {
        ComponentExtension ce = configuration().use(ComponentExtension.class);
        ComponentConfiguration i = ce.install(o);
        NewService ns = cache.get(i);
        if (ns != null) {

        }
    }

    IdentityHashMap<ComponentConfiguration, NewService> cache = new IdentityHashMap<>();

    NewService current;

    void onInstall(ProvideHookProcessor builder, ComponentConfiguration cc) {

    }

    static class NewService {

    }
}
