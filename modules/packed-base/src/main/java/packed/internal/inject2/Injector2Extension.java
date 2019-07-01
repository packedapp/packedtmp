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
package packed.internal.inject2;

import java.util.IdentityHashMap;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.ContainerExtension;

/**
 *
 */
public final class Injector2Extension extends ContainerExtension<Injector2Extension> {

    IdentityHashMap<ComponentConfiguration, NewService> cache = new IdentityHashMap<>();

    NewService current;

    /** Creates a new injector extension. */
    Injector2Extension() {}

    void onInstall(ProvideHookProcessor builder, ComponentConfiguration cc) {
        IdentityHashMap<ComponentConfiguration, NewService> c = cache;
        if (c == null) {
            c = cache = new IdentityHashMap<>(1);
        }
        c.put(cc, new NewService(builder));
    }

    public void provide(Object o) {
        // Must make sure we never recursively call into this method

        NewService ns = current = new NewService();
        try {
            use(ComponentExtension.class).install(o);
        } finally {
            current = null;
        }

        System.out.println(ns);
    }

    public void provide2(Object instance) {
        // Eneste ulempe ved den her form, er at vi burde lave checks inden....
        ComponentConfiguration cc = use(ComponentExtension.class).install(instance);

        NewService ns = null;
        if (cache != null && (ns = cache.get(cc)) == null) {
            ns = new NewService();
        }
        System.out.println(ns.php);
    }

    static class NewService {
        ProvideHookProcessor php;

        NewService() {}

        NewService(ProvideHookProcessor php) {
            this.php = php;
        }
    }
}
