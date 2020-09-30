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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Stream;

import app.packed.service.Service;
import app.packed.service.ServiceSet;

/**
 *
 */
public class SimpleServiceSet implements ServiceSet {

    final List<Service> services;

    public SimpleServiceSet(List<Service> services) {
        this.services = requireNonNull(services);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<Service> stream() {
        return services.stream();
    }

    @Override
    public String toString() {
        return services.toString();
    }
}
