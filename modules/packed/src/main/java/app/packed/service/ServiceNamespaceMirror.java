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
package app.packed.service;

import java.util.Collection;
import java.util.Map;

import app.packed.container.NamespaceMirror;
import app.packed.extension.BaseExtension;
import app.packed.util.Key;

/**
 * A service namespace is namespace where every provided key is unique.
 * <p>
 * A service domain
 * <p>
 * In
 */
// 2 typer exports + main
public class ServiceNamespaceMirror extends NamespaceMirror<BaseExtension> {

    // Where is my exported services used
    public Map<Key<?>, Collection<ServiceBindingMirror>> bindings() {
        throw new UnsupportedOperationException();
    }

    /** {@return a map of all provided services in the domain.} */
    public Map<Key<?>, ProvidedServiceMirror> providers() {
        throw new UnsupportedOperationException();
    }

    public ServiceContract contract() {
        throw new UnsupportedOperationException();
    }
}
