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
package app.packed.service.mirror;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceMirror;
import app.packed.service.ServiceContract;
import app.packed.util.Key;

/**
 * A service namespace represents a namespace where every service has a unique {@link Key key}.
 * <p>
 * A service domain
 * <p>
 * In
 */

// 2 typer exports + main
// Maaske er det ikke et namespace...
public class ServiceNamespaceMirror extends NamespaceMirror<BaseExtension> {

    // Where is my exported services used..
    // Only local???
    // And what about Linage
    public Map<Key<?>, Collection<ServiceBindingMirror>> bindings() {
        throw new UnsupportedOperationException();
    }

    public ServiceContract contract() {
        throw new UnsupportedOperationException();
    }

    /** {@return keys for every service that is available in the namespace.} */
    public Set<Key<?>> keys() {
        return providers().keySet();
    }

    /** {@return a map with details of all the provided services in the namespace.} */
    public Map<Key<?>, ServiceProviderMirror> providers() {
        throw new UnsupportedOperationException();
    }

    public enum Kind {
        BEAN, CONTAINER, EXPORTS;
    }
}