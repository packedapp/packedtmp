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

import app.packed.container.Component;
import app.packed.container.Container;
import app.packed.inject.InjectionSite;
import app.packed.inject.Injector;
import app.packed.inject.Key;

/** Various common keys. */
public final class CommonKeys {

    /** The {@link Component} interface as a key. */
    public static final Key<Component> COMPONENT_KEY = Key.of(Component.class);

    /** The {@link Container} class as a key. */
    public static final Key<?> CONTAINER_KEY = Key.of(Container.class);

    /** The {@link InjectionSite} class as a key. */
    public static final Key<?> INJECTION_SITE_KEY = Key.of(InjectionSite.class);

    /** The {@link Injector} class as a key. */
    public static final Key<?> INJECTOR_KEY = Key.of(Injector.class);

    /** Cannot instantiate. */
    private CommonKeys() {}
}
